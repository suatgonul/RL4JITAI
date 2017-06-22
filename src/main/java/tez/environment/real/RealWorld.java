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
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import tez.domain.*;
import tez.domain.SelfManagementDomainGenerator;
import tez.environment.SelfManagementEnvironment;
import tez.environment.context.Context;

import static tez.domain.SelfManagementDomainGenerator.*;
import static tez.util.LogUtil.log_info;

/**
 * Created by suat on 26-May-17.
 */
public class RealWorld extends SelfManagementEnvironment {
    private static final Logger log = Logger.getLogger(RealWorld.class);

    private boolean lastUserReaction;
    private boolean terminateAtSeven;

    private Context currentContext;
    private String deviceIdentifier;

    private boolean firstStateInTheMorning = false;

    public RealWorld(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, Object... params) {
        super(domain, rf, tf, stateChangeFrequency, params);
        currentTime = DateTime.now();
        currentContext = new Context(deviceIdentifier);
        terminateAtSeven = false;
        this.curState = stateGenerator.generateState();
    }

    public static void main(String[] args) {
        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new SelfManagementRewardFunction();

        new RealWorld(domain, rf, tf, 1, "", "");
    }

    @Override
    protected void initParameters(Object... params) {
        deviceIdentifier = (String) params[0];
    }

    @Override
    public EnvironmentOutcome executeAction(GroundedAction ga) {

        GroundedAction simGA = (GroundedAction) ga.copy();
        simGA.action = this.domain.getAction(ga.actionName());
        if (simGA.action == null) {
            throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
        }

        for (EnvironmentObserver observer : this.observers) {
            observer.observeEnvironmentActionInitiation(this.getCurrentObservation(), ga);
        }

        State nextState;
        if (this.allowActionFromTerminalStates || !this.isInTerminalState()) {
            if (simGA.actionName().equals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
                NotificationManager.getInstance().sendNotificaitonToUser(deviceIdentifier);
            }

            // advances the time plan
            nextState = simGA.executeIn(this.curState);
            log_info(log, deviceIdentifier, "Action " + simGA.actionName() + " executed in environment");

            lastUserReaction = userReacted();

            // generates the reward based on the reaction of the user
            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
            log_info(log, deviceIdentifier, "Reward: " + this.lastReward);
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
        //NotificationManager.NotificationMetadata notificationMetadata = NotificationManager.getInstance().getLastNotification(deviceIdentifier);
        //String reaction = notificationMetadata.getReaction();
        //log_info(log_info, deviceIdentifier, " Result for notification " + notificationMetadata.getNotificationId() + ": " + reaction);
        //if(reaction.contentEquals("Sent") || reaction.contentEquals("Negative") || reaction.contentEquals("Dismissed")) {
        boolean recentReaction = NotificationManager.getInstance().checkRecentReaction(deviceIdentifier);
        log_info(log, deviceIdentifier, "Recent reaction: " + recentReaction);
        if (recentReaction) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public State getNextState() {
        currentContext = ContextChangeListener.getInstance().getUpdatedContext(deviceIdentifier);
        log_info(log, deviceIdentifier, "New context, time: " + currentContext.getTime() + ", loc: " + currentContext.getLocation() + ", activity: " + currentContext.getPhysicalActivity() + ", phone: " + currentContext.getPhoneUsage());
        State s = getStateFromCurrentContext();
        if(!(s instanceof TerminalState)) {
            log_info(log, deviceIdentifier, "State for the context: " + SelfManagementState.transformToCSV(s));
        } else {
            log_info(log, deviceIdentifier, "Environment in terminal state");
        }
        return s;
    }

    @Override
    public State getStateFromCurrentContext() {
        State s;
        SelfManagementDomain smdomain = (SelfManagementDomain) domain;

        boolean terminate = false;
        int hour = currentContext.getTime().hourOfDay().get();
        //int minute = currentContext.getTime().minuteOfHour().get();

        if(hour == 5) {
            if(terminateAtSeven == true) {
                // handles the first encounter with a 7 o'clock state
                terminate = true;
                terminateAtSeven = false;
                log_info(log, deviceIdentifier, "First encounter with a 7 o'clock state. Will terminate the episode");

            } else {
                // handles the state with 7 o'clock other than the initial one
            }
        } else {
            if(hour != 5) {
                log_info(log, deviceIdentifier, "Termination paramater is reset to true");
                terminateAtSeven = true;
            }
        }

        if(!terminate) {
            s = new MutableState();
            s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

            ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_DAY_TYPE, currentTime.getDayOfWeek() == DateTimeConstants.SATURDAY || currentTime.getDayOfWeek() == DateTimeConstants.SUNDAY ? 1 : 0);
            o.setValue(ATT_LOCATION, currentContext.getLocation().ordinal());
            o.setValue(ATT_ACTIVITY_TIME, currentContext.getTime().getHourOfDay() + ":" + currentContext.getTime().getMinuteOfHour());
            o.setValue(ATT_ACTIVITY, currentContext.getPhysicalActivity().ordinal());
            o.setValue(ATT_PHONE_USAGE, currentContext.getPhoneUsage().ordinal());
            o.setValue(ATT_EMOTIONAL_STATUS, currentContext.getEmotionalStatus().ordinal());
            o.setValue(ATT_STATE_OF_MIND, currentContext.getStateOfMind().ordinal());
        } else {
            s = new TerminalState();
        }

        return s;
    }

    @Override
    public void resetEnvironment(){
        super.resetEnvironment();
        log_info(log, deviceIdentifier, "End of episode. Resetting the environment");
    }

    public boolean getLastUserReaction() {
        return lastUserReaction;
    }

    public String getDeviceIdentifier() {
        return this.deviceIdentifier;
    }
}
