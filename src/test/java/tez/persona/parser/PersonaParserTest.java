package tez.persona.parser;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tez.persona.Activity;
import tez.persona.TimePlan;
import tez.simulator.context.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by suatgonul on 12/12/2016.
 */
public class PersonaParserTest {

    private Activity activity;

    private DateTime getReferenceDate() {
        return new DateTime().withDate(2016, 12, 17).withTime(10, 0, 0, 0);
    }

    @Before
    public void initialize() {
        activity = new Activity("test", getReferenceDate(), 60, new Context());
    }

    @Test
    public void testAddingPhoneCheckActivity_inTheMiddle() {
        try {
            PersonaParser parser = new PersonaParser();
            Method method = parser.getClass().getDeclaredMethod("addPhoneCheckActivity", Activity.class, int.class, int.class);
            method.setAccessible(true);

            int nextPhoneCheck = 20;
            int phoneCheckDuration = 7;
            Object results = method.invoke(parser, activity, nextPhoneCheck, phoneCheckDuration);
            List<Activity> activities = (List<Activity>) results;

            Assert.assertEquals("Did not have the correct number of activities", 3, activities.size());
            Assert.assertEquals("Did not have the correct duration for the first part of the initial activity", 20, activities.get(0).getDuration());
            Assert.assertEquals("Did not have the correct duration for the phone check activity", 7, activities.get(1).getDuration());
            Assert.assertEquals("Did not have the correct duration for the second part of the initial activity", 33, activities.get(2).getDuration());

            DateTime expectedStartTime = getReferenceDate();
            Assert.assertEquals("Did not have the correct start time for the first part of the initial activity", expectedStartTime, activities.get(0).getStart());
            expectedStartTime = expectedStartTime.plusMinutes(nextPhoneCheck);
            Assert.assertEquals("Did not have the correct start time for the phone check activity", expectedStartTime, activities.get(1).getStart());
            expectedStartTime = expectedStartTime.plusMinutes(phoneCheckDuration);
            Assert.assertEquals("Did not have the correct start time for the second part of the initial  activity", expectedStartTime, activities.get(2).getStart());

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.out.println("Error in calling the \"addPhoneCheckActivity\" method\n");
            e.printStackTrace();
            Assert.fail("Could not find the function");
        }
    }

    @Test
    public void testAddingPhoneCheckActivity_atTheEnd() {
        try {
            PersonaParser parser = new PersonaParser();
            Method method = parser.getClass().getDeclaredMethod("addPhoneCheckActivity", Activity.class, int.class, int.class);
            method.setAccessible(true);

            int nextPhoneCheck = 55;
            int phoneCheckDuration = 7;
            Object results = method.invoke(parser, activity, nextPhoneCheck, phoneCheckDuration);
            List<Activity> activities = (List<Activity>) results;

            Assert.assertEquals("Did not have the correct number of activities", 2, activities.size());
            Assert.assertEquals("Did not have the correct duration for the first part of the initial activity", 55, activities.get(0).getDuration());
            Assert.assertEquals("Did not have the correct duration for the phone check activity", 5, activities.get(1).getDuration());

            DateTime expectedStartTime = getReferenceDate();
            Assert.assertEquals("Did not have the correct start time for the first part of the initial activity", expectedStartTime, activities.get(0).getStart());
            expectedStartTime = expectedStartTime.plusMinutes(nextPhoneCheck);
            Assert.assertEquals("Did not have the correct start time for the phone check activity", expectedStartTime, activities.get(1).getStart());

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.out.println("Error in calling the \"addPhoneCheckActivity\" method\n");
            e.printStackTrace();
            Assert.fail("Could not find the function");
        }
    }

    @Test
    public void testGeneratedActivities_randomlyGenerated() {
        try {
            PersonaParser parser = new PersonaParser();
            Method method = parser.getClass().getDeclaredMethod("getTimePlanForPersona", String.class);
            method.setAccessible(true);

            TimePlan timePlan = (TimePlan) method.invoke(parser, "src/test/resources/test-persona-random.csv");
            List<Activity> activities = timePlan.getActivities();
            for (int i = 0; i < activities.size() - 1; i++) {
                Assert.assertEquals("Inconsistent start time", activities.get(i + 1).getStart(), activities.get(i).getEndTime());
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.out.println("Error in calling the \"generateActivitiesForTimePlan\" method\n");
            e.printStackTrace();
            Assert.fail("Could not find the function");
        }
    }

    @Test
    public void testGeneratedActivities_() {
        String filePath = "src/test/resources/test-persona-relative.csv";
        testGeneratedActivities(filePath);
    }

    @Test
    public void testGeneratedActivities_fixedStartingTimesAndDurations() {
        String filePath = "src/test/resources/test-persona-static.csv";
        testGeneratedActivities(filePath);
    }

    @Test
    public void testGeneratedActivities_randomlyStartingTimesAndDurations() {
        String filePath = "src/test/resources/test-persona-random.csv";
        testGeneratedActivities(filePath);
    }

    private void testGeneratedActivities(String filePath) {
        try {
            PersonaParser parser = new PersonaParser();
            Method method = parser.getClass().getDeclaredMethod("getTimePlanForPersona", String.class);
            method.setAccessible(true);

            TimePlan timePlan = (TimePlan) method.invoke(parser, filePath);
            List<Activity> activities = timePlan.getActivities();

            for (int i = 0; i < activities.size() - 1; i++) {
                Assert.assertEquals("Inconsistent start time", activities.get(i + 1).getStart(), activities.get(i).getEndTime());
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.out.println("Error in calling the \"generateActivitiesForTimePlan\" method\n");
            e.printStackTrace();
            Assert.fail("Could not find the function");
        }
    }
}
