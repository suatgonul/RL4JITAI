package tez.environment.real;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.EnvironmentObserver;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalTime;
import org.json.JSONObject;
import tez.domain.*;
import tez.environment.SelfManagementEnvironment;
import tez.environment.context.*;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static tez.domain.SelfManagementDomainGenerator.*;
import static tez.domain.SelfManagementDomainGenerator.ATT_STATE_OF_MIND;

/**
 * Created by suat on 26-May-17.
 */
public class RealWorld extends SelfManagementEnvironment {
    private DateTime trialStartDate;
    private int trialLength;
    private boolean lastUserReaction;


    private Context currentContext;
    private String deviceIdentifier;

    public static void main(String[] args) {
        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new SelfManagementRewardFunction();

        new RealWorld(domain, rf, tf, 1, "", 1000, "");
    }

    public RealWorld(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, Object... params) {
        super(domain, rf, tf, stateChangeFrequency, params);
        currentTime = DateTime.now();
    }

    @Override
    protected void initParameters(Object... params) {
        trialLength = (Integer) params[0];
        deviceIdentifier = (String) params[1];
        trialStartDate = DateTime.now();
    }

    @Override
    public EnvironmentOutcome executeAction(GroundedAction ga) {

        GroundedAction simGA = (GroundedAction) ga.copy();
        simGA.action = this.domain.getAction(ga.actionName());
        if (simGA.action == null) {
            throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
        }

        NotificationManager.getInstance().sendNotificaitonToUser(deviceIdentifier);

        for (EnvironmentObserver observer : this.observers) {
            observer.observeEnvironmentActionInitiation(this.getCurrentObservation(), ga);
        }

        State nextState;
        if (this.allowActionFromTerminalStates || !this.isInTerminalState()) {
            // advances the time plan
            nextState = simGA.executeIn(this.curState);

            lastUserReaction = userReacted();

            // generates the reward based on the reaction of the user
            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
        } else {
            nextState = this.curState;
            this.lastReward = 0.;
        }

        EnvironmentOutcome eo = new ExtendedEnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState), currentContext.copy(), lastUserReaction);

        this.curState = nextState;

        for (EnvironmentObserver observer : this.observers) {
            observer.observeEnvironmentInteraction(eo);
        }

        return eo;
    }

    private boolean userReacted() {
        String reaction = NotificationManager.getInstance().getLastNotificationResult(deviceIdentifier);
        System.out.println("Result for " + deviceIdentifier + ": " + reaction);
        if(reaction.contentEquals("Sent") || reaction.contentEquals("Negative") || reaction.contentEquals("Dismissed")) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public State getNextState() {
        currentContext = ContextChangeListener.getInstance().getUpdatedContext(deviceIdentifier);
        System.out.println("Polled context");
        State s = getStateFromCurrentContext();
        return s;
    }

    @Override
    public State getStateFromCurrentContext() {
        State s;
        SelfManagementDomain smdomain = (SelfManagementDomain) domain;

        int daysElapsed = Days.daysBetween(trialStartDate, DateTime.now()).getDays();
        if (daysElapsed <= trialLength) {
            s = new MutableState();
            s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

            ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_DAY_TYPE, currentTime.getDayOfWeek() == DateTimeConstants.SATURDAY || currentTime.getDayOfWeek() == DateTimeConstants.SUNDAY ? 1 : 0);
            o.setValue(ATT_LOCATION, currentContext.getLocation().ordinal());

            if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.EASY) {
                o.setValue(ATT_HOUR_OF_DAY, currentTime.getHourOfDay());

            } else if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.MEDIUM) {
                o.setValue(ATT_QUARTER_HOUR_OF_DAY, getQuarterStateRepresentation());
                o.setValue(ATT_ACTIVITY, currentContext.getPhysicalActivity().ordinal());

            } else if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.HARD) {
                o.setValue(ATT_ACTIVITY_TIME, currentTime.getHourOfDay() + ":" + currentTime.getMinuteOfHour());
                o.setValue(ATT_ACTIVITY, currentContext.getPhysicalActivity().ordinal());
                o.setValue(ATT_PHONE_USAGE, currentContext.getPhoneUsage().ordinal());
                o.setValue(ATT_EMOTIONAL_STATUS, currentContext.getEmotionalStatus().ordinal());
                o.setValue(ATT_STATE_OF_MIND, currentContext.getStateOfMind().ordinal());
            }

        } else {
            s = new TerminalState();
        }

        return s;
    }

    public boolean getLastUserReaction() {
        return lastUserReaction;
    }
}
