package tez.environment.simulator;

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
import org.joda.time.LocalTime;
import tez.domain.*;
import tez.environment.context.*;
import tez.persona.Activity;
import tez.persona.TimePlan;
import tez.persona.parser.PersonaParser;
import tez.persona.parser.PersonaParserException;

import java.util.ArrayList;
import java.util.List;

import static tez.domain.SelfManagementDomainGenerator.*;

/**
 * Created by suatgonul on 12/2/2016.
 */
public class SimulatedWorld extends SimulatedEnvironment {
    private String personaFolder;
    private int stateChangeFrequency;

    private int dayOffset;
    private DateTime previousTime;
    private Activity previousActivity;
    private TimePlan currentTimePlan;
    private DateTime currentTime;
    private Activity currentActivity;
    private boolean lastActivity;
    private boolean lastUserReaction;
    private DateTime lastInterventionCheckTime;

    public SimulatedWorld(Domain domain, RewardFunction rf, TerminalFunction tf, String personaFolder, int stateChangeFrequency) {
        super(domain, rf, tf);
        ((SelfManagementRewardFunction) rf).setEnvironment(this);
        this.personaFolder = personaFolder;
        this.stateChangeFrequency = stateChangeFrequency;
        dayOffset = 1;
        episodeInit(dayOffset);
        //stateGenerator = new ConstantStateGenerator(getStateFromCurrentContext());
        stateGenerator = new SelfManagementStateGenerator(this);
        super.resetEnvironment();
    }

    public SelfManagementDomain getDomain() {
        return (SelfManagementDomain) domain;
    }


    /**
     * Main orchestrator call for executing a single step of an episode. Within this method, by calling the executeIn
     * method of the environment state is set to the next state and afterwards reward is obtained by calling the
     * reward function's reward method.
     *
     * @param ga
     * @return
     */
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
            // advances the time plan
            nextState = simGA.executeIn(this.curState);

            lastUserReaction = userReacted();

            // generates the reward based on the reaction of the user
            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
        } else {
            nextState = this.curState;
            this.lastReward = 0.;
        }

        EnvironmentOutcome eo = new ExtendedEnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState), previousActivity.getContext().copy(), lastUserReaction);

        this.curState = nextState;

        for (EnvironmentObserver observer : this.observers) {
            observer.observeEnvironmentInteraction(eo);
        }

        return eo;
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
                o.setValue(ATT_ACTIVITY_TIME, currentTime.getHourOfDay() + ":" + currentTime.getMinuteOfHour());
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

    /**
     * This method return whether the user has reacted to an intervention if at least there is one invetervention delivered.
     * It should be called after the environment variables are updated after calling {@code advanceTimePlan}.
     *
     * @return
     */
    private boolean userReacted() {
        return checkUserReaction(previousActivity, previousTime);
    }

    private boolean checkUserReaction(Activity activity, DateTime time) {
        DayType dayType = getDayType(dayOffset);
        Location location = activity.getContext().getLocation();
        PhysicalActivity physicalActivity = activity.getContext().getPhysicalActivity();
        int hourOfDay = time.getHourOfDay();
        int minuteOfHour = time.getMinuteOfHour();
        StateOfMind stateOfMind = activity.getContext().getStateOfMind();
        EmotionalStatus emotionalStatus = activity.getContext().getEmotionalStatus();
        PhoneUsage phoneUsage = activity.getContext().getPhoneUsage();

        //if (stateOfMind == StateOfMind.CALM && emotionalStatus == EmotionalStatus.NEUTRAL && phoneUsage == PhoneUsage.APPS_ACTIVE && location == Location.HOME) {
//        if (location == Location.HOME && ((dayType == DayType.WEEKDAY && hourOfDay > 20 || dayType == DayType.WEEKEND)) && stateOfMind == StateOfMind.CALM && emotionalStatus == EmotionalStatus.NEUTRAL) {
//            return true;
//        }
        if (activity.getContext().getPhoneCheckSuitability() == true) {
            boolean contextSuitable = false;
            boolean timeSuitable = false;

            if (dayType == DayType.WEEKDAY) {
                List<LocalTime> startTimes = new ArrayList<>();
                List<LocalTime> endTimes = new ArrayList<>();
                startTimes.add(new LocalTime(6, 0));
                endTimes.add(new LocalTime(10, 0));
                startTimes.add(new LocalTime(11, 0));
                endTimes.add(new LocalTime(12, 30));
                startTimes.add(new LocalTime(13, 30));
                endTimes.add(new LocalTime(14, 15));
                startTimes.add(new LocalTime(15, 0));
                endTimes.add(new LocalTime(16, 30));
                startTimes.add(new LocalTime(17, 20));
                endTimes.add(new LocalTime(18, 30));
                startTimes.add(new LocalTime(19, 30));
                endTimes.add(new LocalTime(23, 00));

                // check timing
                for (int i = 0; i < startTimes.size(); i++) {
                    LocalTime localTime = time.toLocalTime();
                    if (localTime.isAfter(startTimes.get(i)) && localTime.isBefore(endTimes.get(i))) {
                        timeSuitable = true;
                        break;
                    }
                }
            } else {
                timeSuitable = true;
            }

            // before going to work and at the beginning of the working day
            if (timeSuitable &&
                    (stateOfMind == StateOfMind.CALM || stateOfMind == StateOfMind.FOCUS) &&
                    (emotionalStatus == EmotionalStatus.NEUTRAL || emotionalStatus == EmotionalStatus.RELAXED || emotionalStatus == EmotionalStatus.HAPPY) &&
                    physicalActivity == PhysicalActivity.SITTING_IN_CAR || physicalActivity == PhysicalActivity.SEDENTARY) {
                contextSuitable = true;
            }

            // check other heuristics related to reaction to a delivered intervention
            if (contextSuitable) {
                // check the time between two reactions is less than 3 hours
                if (lastInterventionCheckTime != null && time.getMillis() - lastInterventionCheckTime.getMillis() < 3 * 60 * 60 * 1000) {
                    return false;
                } else {
                    lastInterventionCheckTime = time;
                    return true;
                }
                //return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

   /* private boolean checkUserReaction(Activity activity, DateTime time) {
            DayType dayType = getDayType(dayOffset);
        Location location = activity.getContext().getLocation();
        PhysicalActivity physicalActivity = activity.getContext().getPhysicalActivity();
        int hourOfDay = time.getHourOfDay();
        int minuteOfHour = time.getMinuteOfHour();
        StateOfMind stateOfMind = activity.getContext().getStateOfMind();
        EmotionalStatus emotionalStatus = activity.getContext().getEmotionalStatus();
        PhoneUsage phoneUsage = activity.getContext().getPhoneUsage();

        //if (stateOfMind == StateOfMind.CALM && emotionalStatus == EmotionalStatus.NEUTRAL && phoneUsage == PhoneUsage.APPS_ACTIVE && location == Location.HOME) {
        if (location == Location.HOME && ((dayType == DayType.WEEKDAY && hourOfDay > 20 || dayType == DayType.WEEKEND)) && stateOfMind == StateOfMind.CALM && emotionalStatus == EmotionalStatus.NEUTRAL) {
            return true;
        } else {
            return false;
        }

  //      return activity.getContext().getPhoneCheckSuitability();
    }*/

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
        // reset the time for reaction to the last intervention
        lastInterventionCheckTime = null;
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

    public boolean getLastUserReaction() {
        return lastUserReaction;
    }
}
