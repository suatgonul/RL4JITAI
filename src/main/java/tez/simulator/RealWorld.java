package tez.simulator;

import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import org.joda.time.DateTime;
import tez.algorithm.ReactionRewardFunction;
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
        stateGenerator = new ConstantStateGenerator(getState());
        resetEnvironment();
    }

    public DateTime getCurrentTime() {
        return currentTime;
    }

    /**
     * Increase the time in real world by considering the start of the next activity and state time period
     */
    public State getNextState() {
        int currentActivityIndex = currentTimePlan.getActivities().indexOf(currentActivity);
        advanceTimePlan(currentActivityIndex);
        return getState();
    }

    private void advanceTimePlan(int currentActivityIndex) {
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
        DayType dayType = getDayType(dayOffset);
        Location location = currentActivity.getContext().getLocation();
        int hourOfDay = currentTime.getHourOfDay();
        StateOfMind stateOfMind = currentActivity.getContext().getStateOfMind();
        EmotionalStatus emotionalStatus = currentActivity.getContext().getEmotionalStatus();
        PhoneUsage phoneUsage = currentActivity.getContext().getPhoneUsage();

        if (stateOfMind == StateOfMind.CALM && emotionalStatus == EmotionalStatus.NEUTRAL && phoneUsage == PhoneUsage.APPS_ACTIVE && location == Location.HOME) {
            return true;
        }
        return false;
    }

    private State getState() {
        if (!lastActivity) {
            State s = new MutableState();
            s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

            ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_HOUR_OF_DAY, currentTime.getHourOfDay());
            o.setValue(ATT_DAY_TYPE, getDayType(dayOffset).ordinal());
            o.setValue(ATT_LOCATION, currentActivity.getContext().getLocation().ordinal());

            return s;
        } else {
            return new TerminalState();
        }
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
    }

    private DayType getDayType(int dayOffset) {
        if (dayOffset % 7 < 5) {
            return DayType.WEEKDAY;
        } else {
            return DayType.WEEKEND;
        }
    }
}
