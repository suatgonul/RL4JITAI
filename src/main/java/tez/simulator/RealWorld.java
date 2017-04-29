package tez.simulator;

import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
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
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import org.joda.time.DateTime;
import tez.algorithm.ExtendedEnvironmentOutcome;
import tez.algorithm.ReactionRewardFunction;
import tez.algorithm.StateInfo;
import tez.algorithm.TerminalState;
import tez.persona.Activity;
import tez.persona.TimePlan;
import tez.persona.parser.PersonaParser;
import tez.persona.parser.PersonaParserException;
import tez.simulator.context.*;

import static tez.algorithm.SelfManagementDomainGenerator.*;

/**
 * Created by suatgonul on 12/2/2016.
 */
public class RealWorld extends SimulatedEnvironment {
    private String personaFolder;
    private int stateChangeFrequency;

    private int dayOffset;
    private DateTime previousTime;
    private Activity previousActivity;
    private TimePlan currentTimePlan;
    private DateTime currentTime;
    private Activity currentActivity;
    boolean lastActivity;

    public RealWorld(Domain domain, RewardFunction rf, TerminalFunction tf, String personaFolder, int stateChangeFrequency) {
        super(domain, rf, tf);
        ((ReactionRewardFunction) rf).setEnvironment(this);
        this.personaFolder = personaFolder;
        this.stateChangeFrequency = stateChangeFrequency;
        dayOffset = 1;
        episodeInit(dayOffset);
        stateGenerator = new ConstantStateGenerator(getStateFromCurrentContext());
        super.resetEnvironment();
    }

    @Override
    public EnvironmentOutcome executeAction(GroundedAction ga) {

        GroundedAction simGA = (GroundedAction)ga.copy();
        simGA.action = this.domain.getAction(ga.actionName());
        if(simGA.action == null){
            throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
        }

        for(EnvironmentObserver observer : this.observers){
            observer.observeEnvironmentActionInitiation(this.getCurrentObservation(), ga);
        }

        State nextState;
        if(this.allowActionFromTerminalStates || !this.isInTerminalState()) {
            nextState = simGA.executeIn(this.curState);
            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
        }
        else{
            nextState = this.curState;
            this.lastReward = 0.;
        }

        EnvironmentOutcome eo = new ExtendedEnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState), previousActivity.getContext().copy(), userReacted());

        this.curState = nextState;

        for(EnvironmentObserver observer : this.observers){
            observer.observeEnvironmentInteraction(eo);
        }

        return eo;
    }


    /**
     * Increase the time in real world by considering the start of the next activity and state time period
     */
    public StateInfo getNextState() {
        advanceTimePlan();
        boolean userReacted = userReacted();
        State state = getStateFromCurrentContext();
        return new StateInfo(state, userReacted);
    }

    private void advanceTimePlan() {
        previousTime = currentTime;
        previousActivity = currentActivity;

        int currentActivityIndex = currentTimePlan.getActivities().indexOf(currentActivity);
        DateTime activityEndTime = currentActivity.getEndTime();
        if (activityEndTime.isAfter(currentTime.plusMinutes(stateChangeFrequency))) {
            currentTime = currentTime.plusMinutes(stateChangeFrequency);
        } else {
            currentTime = activityEndTime;
            // update activity
            lastActivity = currentActivityIndex == currentTimePlan.getActivities().size() - 1;
            if (!lastActivity) {
                currentActivity = currentTimePlan.getActivities().get(++currentActivityIndex);
            }
        }
    }

    public boolean userReacted() {
        return checkUserReaction(previousActivity, previousTime);
    }

    private State getStateFromCurrentContext() {
        State s;
        if (!lastActivity) {
            s = new MutableState();
            s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

            ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_HOUR_OF_DAY, currentTime.getHourOfDay());
            o.setValue(ATT_DAY_TYPE, getDayType(dayOffset).ordinal());
            o.setValue(ATT_LOCATION, currentActivity.getContext().getLocation().ordinal());

        } else {
            s = new TerminalState();
        }

        return s;
    }

    private boolean checkUserReaction(Activity activity, DateTime time) {
        DayType dayType = getDayType(dayOffset);
        Location location = activity.getContext().getLocation();
        int hourOfDay = time.getHourOfDay();
        StateOfMind stateOfMind = activity.getContext().getStateOfMind();
        EmotionalStatus emotionalStatus = activity.getContext().getEmotionalStatus();
        PhoneUsage phoneUsage = activity.getContext().getPhoneUsage();

        //if (stateOfMind == StateOfMind.CALM && emotionalStatus == EmotionalStatus.NEUTRAL && phoneUsage == PhoneUsage.APPS_ACTIVE && location == Location.HOME) {
        if (location == Location.HOME && hourOfDay > 20) {
            return true;
        }
        return false;
    }

    @Override
    public void resetEnvironment() {
        super.resetEnvironment();
        episodeInit(++dayOffset);
    }

    private void episodeInit(int dayOffset) throws WorldSimulationException {
        // initialize time plan
        PersonaParser personaParser = new PersonaParser();
        String personaPath;
        if (getDayType(dayOffset) == DayType.WEEKDAY) {
            personaPath = personaFolder + "/weekday.csv";
        } else {
            personaPath = personaFolder + "/weekend.csv";
        }

        try {
            currentTimePlan = personaParser.getTimePlanForPersona(personaPath);
        } catch (PersonaParserException e) {
            System.out.println("Could get time plan for day of week: " + dayOffset);
            throw new WorldSimulationException("Could get time plan for day of week: " + dayOffset, e);
        }

        // initialize time
        currentTime = currentTimePlan.getStart();
        // initialize activity
        currentActivity = currentTimePlan.getActivities().get(0);
        lastActivity = false;
    }

    private DayType getDayType(int dayOffset) {
        if (dayOffset % 7 < 5) {
            return DayType.WEEKDAY;
        } else {
            return DayType.WEEKEND;
        }
    }
}
