package tez.simulator;

import org.joda.time.DateTime;
import tez.model.StateDTO;
import tez.persona.Activity;
import tez.persona.TimePlan;
import tez.persona.parser.PersonaParser;

import java.io.IOException;

/**
 * Created by suatgonul on 12/2/2016.
 */
public class RealWorld {
    private int dayOffset;
    private TimePlan currentTimePlan;
    private DateTime currentTime;
    private Activity currentActivity;

    private String personaFolder;
    private int stateChangeFrequency;

    public DateTime getCurrentTime() {
        return currentTime;
    }

    public void reset() {
        dayOffset = 0;
        currentTimePlan = null;
        currentTime = null;
        currentActivity = null;
        stateChangeFrequency = 0;
    }

    public void init(String personaFolder, int stateChangeFrequency) throws WorldSimulationException {
        this.personaFolder = personaFolder;
        this.stateChangeFrequency = stateChangeFrequency;

        // initialize day offset
        dayOffset = DateTime.now().getDayOfWeek();
        episodeInit(dayOffset);
    }

    public void advanceNextEpisode() throws WorldSimulationException {
        dayOffset++;
        episodeInit(dayOffset);
    }

    private void episodeInit(int dayOffset) throws WorldSimulationException {
        // initialize time plan
        PersonaParser personaParser = new PersonaParser();
        String personaPath;
        if (dayOffset % 7 >= 1 && dayOffset <= 5) {
            personaPath = personaFolder + "/weekday.csv";
        } else {
            personaPath = personaFolder + "/weekend.csv";
        }

        try {
            currentTimePlan = personaParser.getTimePlanForPersona(personaPath);
        } catch (IOException e) {
            System.out.println("Could get time plan for day of week: " + dayOffset);
            throw new WorldSimulationException("Could get time plan for day of week: " + dayOffset, e);
        }

        // initialize time
        currentTime = currentTimePlan.getStart();
    }

    /**
     * Increase the time in real world by considering the start of the next activity and state time period
     */
    public StateDTO getStateAndAdvanceTimePlan() {
        int currentActivityIndex = currentTimePlan.getActivities().indexOf(currentActivity);
        boolean lastActivity = currentActivityIndex == currentTimePlan.getActivities().size() - 1;

        StateDTO state = createStateDTO(lastActivity);
        advanceTimePlan(currentActivityIndex, lastActivity);

        return state;
    }

    private void advanceTimePlan(int currentActivityIndex, boolean lastActivity) {
        DateTime activityEndTime = currentActivity.getEndTime();
        if (activityEndTime.isAfter(currentTime.plusMinutes(stateChangeFrequency))) {
            currentTime = currentTime.plus(stateChangeFrequency);
        } else {
            currentTime = activityEndTime;
            // update activity
            if (!lastActivity) {
                currentActivity = currentTimePlan.getActivities().get(++currentActivityIndex);
            }
        }
    }

    private StateDTO createStateDTO(boolean lastActivity) {
        StateDTO state = new StateDTO();
        state.setTime(currentTime);
        state.setTerminal(lastActivity);
        return state;
    }
}
