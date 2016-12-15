package tez.persona;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suatgonul on 12/4/2016.
 */
public class PersonaLoader {
    public enum Persona {

    }

    private Map<Persona, List<TimePlan>> timePlans = new HashMap<Persona, List<TimePlan>>();

    public static void initPersonas() {

    }

    /**
     * <p>
     * Creates a persona for an engineer working mainly in office
     * </p>
     * <p>Characteristics for persona:
     * <br><br>Weekdays
     * <ul>
     * <b>Alternative 1 until getting to the office</b>
     * <li>wakes up around 07:00 - 07:30</li>
     * <li>about 15-20 minutes bathroom time</li>
     * <li>leaves home around 8:00</li>
     * <li>uses the service bus to get to the office</li>
     * <li>gets on the bus around 8:10</li>
     * <li>arrives at the office around 8:45</li>
     * <li>checks daily news, social media while eating his breakfast until he starts working</li>
     * <br><b>Alternative 2 until getting to the office</b>
     * <li>wakes up around 07:00 - 07:30</li>
     * <li>about 15-20 minutes bathroom time</li>
     * <li>eats his breakfast at home in 15-20 minutes</li>
     * <li>leaves home around 8:30</li>
     * <li>gets to the office by his car</li>
     * <li>arrives office around 8:50 - 9:00</li>
     * <li>if still has time, checks daily news, social media while eating his breakfast until he starts working</li>
     * <br>
     * <li>starts work at 9:00</li>
     * <li>works until lunch time (12:30)</li>
     * <li>eats his lunch in about 20 minutes</li>
     * <li>hangs at at a coffee shop about 30 minutes with his friends</li>
     * <br><b>Alternative 1 for afternoon work sessions</b>
     * <li>starts working at 13:30</li>
     * <li>works 1-2 hours until a 5-10 min break</li>
     * <li>works 1-2 hours until grabbing a cup of coffee</li>
     * <li>works until the end of working hours (18:00)</li>
     * <br><b>Alternative 2 for afternoon work sessions</b>
     * <li>starts working at 13:30</li>
     * <li>works 1-2 hours until leaving the office for a meeting</li>
     * <li>either drives the car by himself or just sits in the car</li>
     * <li>arrives at the meeting venue approximately in 30 minutes</li>
     * <li>meeting takes 1-2 hours</li>
     * <li>turns back to office in the same way getting to the meeting venue</li>
     * <li>works until the end of working hours (18:00)</li>
     * <br>
     * <b>Alternative 1 for evening</b>
     * <li>returns home using the mean used for getting to the office</li>
     * <li>plays with his daughter until dinner</li>
     * <li>dinner starts around 19:30 - 20:00</li>
     * <li>finishes eating around 20:30 - 21:00</li>
     * <li>takes a 30 minutes break sitting in the couch and watching TV</li>
     * <li>either he or his wife puts their daughter into bed taking around 10 to 30 minutes</li>
     * <li>checks news, social media around 20-30 minutes</li>
     * <li>either reads a book, an article of interest on computer; or plays guitar</li>
     * <li>usually checks his phone before sleeping</li>
     * <br>
     * <b>Alternative 2 for evening</b>
     * <li>returns home using the mean used for getting to the office (~18:40 by car, ~19:00 by bus)</li>
     * <li>grabs his family to dine outside</li>
     * <li>waits 5-10 minutes family members to be ready, checking his phone in the meantime</li>
     * <li>drives to restaurant for 30-40 minutes</li>
     * <li>orders and waits for food to be served about 20-30 minutes</li>
     * <li>dinner lasts about 1 hour</li>
     * <li>drives back to home</li>
     * <li>either he or his wife puts their daughter into bed taking around 10 to 30 minutes</li>
     * <li>either reads a book, an article of interest on computer; or plays guitar</li>
     * <li>usually checks his phone before sleeping</li>
     * </ul>
     * </p>
     */
    private static void createOfficeJobPersona() {

    }
}
