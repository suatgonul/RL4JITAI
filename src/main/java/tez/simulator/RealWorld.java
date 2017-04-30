package tez.simulator;

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
import tez.algorithm.*;
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
        //stateGenerator = new ConstantStateGenerator(getStateFromCurrentContext());
        stateGenerator = new SelfManagementStateGenerator(this);
        super.resetEnvironment();
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
            nextState = simGA.executeIn(this.curState);
            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
        } else {
            nextState = this.curState;
            this.lastReward = 0.;
        }

        EnvironmentOutcome eo = new ExtendedEnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState), previousActivity.getContext().copy(), userReacted());

        this.curState = nextState;

        for (EnvironmentObserver observer : this.observers) {
            observer.observeEnvironmentInteraction(eo);
        }

        return eo;
    }

    public SelfManagementDomain getDomain() {
        return (SelfManagementDomain) domain;
    }

    /**
     * Increase the time in real world by considering the start of the next activity and state time period
     */
    public State getNextState() {
        advanceTimePlan();
        State state = getStateFromCurrentContext();
        return state;
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

    public State getStateFromCurrentContext() {
        SelfManagementDomain smdomain = (SelfManagementDomain) domain;
        State s;
        if (!lastActivity) {
            s = new MutableState();
            s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

            ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_DAY_TYPE, getDayType(dayOffset).ordinal());
            o.setValue(ATT_LOCATION, currentActivity.getContext().getLocation().ordinal());

            if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.EASY) {
                o.setValue(ATT_HOUR_OF_DAY, currentTime.getHourOfDay());

            } else if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.MEDIUM) {
                o.setValue(ATT_QUARTER_HOUR_OF_DAY, getQuarterStateRepresentation());
                o.setValue(ATT_ACTIVITY, currentActivity.getContext().getPhysicalActivity().ordinal());

            } else if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.HARD) {
                o.setValue(ATT_ACTIVITY_TIME, currentTime.getHourOfDay() + "" + currentTime.getMinuteOfHour());
                o.setValue(ATT_ACTIVITY, currentActivity.getContext().getPhysicalActivity().ordinal());
                o.setValue(ATT_PHONE_USAGE, currentActivity.getContext().getPhoneUsage().ordinal());
                o.setValue(ATT_EMOTIONAL_STATUS, currentActivity.getContext().getEmotionalStatus().ordinal());
                o.setValue(ATT_STATE_OF_MIND, currentActivity.getContext().getStateOfMind().ordinal());
            }

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
//        if (location == Location.HOME && ((dayType == DayType.WEEKDAY && hourOfDay > 20 || dayType == DayType.WEEKEND)) && stateOfMind == StateOfMind.CALM && emotionalStatus == EmotionalStatus.NEUTRAL) {
//            return true;
//        }
        return activity.getContext().getPhoneCheckSuitability();
        //return false;
    }

    @Override
    public void resetEnvironment() {
        episodeInit(++dayOffset);
        super.resetEnvironment();
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
        if (dayOffset - 1 % 7 < 5) {
            return DayType.WEEKDAY;
        } else {
            return DayType.WEEKEND;
        }
    }

    private String getQuarterStateRepresentation() {
        int minute = currentTime.getMinuteOfHour();
        int quarterIndex = minute / 15;
        int quarterOffset = minute % 15;
        if (quarterOffset > 7) {
            quarterIndex++;
        }
        return currentTime.getHourOfDay() + "" + quarterIndex;
    }
}
