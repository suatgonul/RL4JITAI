package tez.simulator;

import org.joda.time.DateTime;
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
    private int stateTimePeriod;

    public void reset() {
        dayOffset = 0;
        currentTimePlan = null;
        currentTime = null;
        stateTimePeriod = 0;
    }

    public void init(String stateTimePeriod, String persona) throws WorldSimulationException {
        // initialize day offset
        dayOffset = DateTime.now().getDayOfWeek();

        // initialize time plan
        PersonaParser personaParser;
        if (dayOffset % 7 >= 1 && dayOffset <= 5) {
            personaParser = new PersonaParser();
            try {
                currentTimePlan = personaParser.getTimePlanForPersona(persona + "/weekday.csv");
            } catch (IOException e) {
                System.out.println("Could get time plan for day of week: " + dayOffset);
                throw new WorldSimulationException("Could get time plan for day of week: " + dayOffset, e);
            }
        }

        // initialize time
        currentTime = currentTimePlan.getStart();
    }

    public void simulateTime() {

    }

    public void simulatePerson() {

    }

    /**
     * Increase the time in real world by considering the start of the next activity and state time period
     */
    public void increaseMinute() {

    }
}
