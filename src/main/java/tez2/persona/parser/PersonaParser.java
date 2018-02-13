package tez2.persona.parser;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import tez2.persona.Activity;
import tez2.persona.TimePlan;
import tez2.util.LogUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Arrays.asList;
import static tez2.persona.parser.Constant.*;

/**
 * Created by suatgonul on 12/10/2016.
 * Implements the parsing logic used to create the persona time plans
 */
public class PersonaParser {


    //Persona properties
    private DateTime middayTime;
    private DateTime eveningTime;

    // Parameters related to parsing
    private Alternative parsedAlternative;
    private AlternativeStatus alternativeStatus = AlternativeStatus.NONE;
    private int remainingProbability = 100;
    private List<String> chosenActivities;
    private int lineIndex;
    private ParsedActivityPool parsedActivityPool;

    private enum AlternativeStatus {
        NONE, IDENTIFIED, ACTIVE, REJECTED, COMPLETED;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 50; i++)
            new PersonaParser().generateActivitiesForTimePlan("src/main/resources/persona/officejob/weekday.csv");
    }

    public TimePlan getTimePlanForPersona(String filePath) throws PersonaParserException {
        generateActivitiesForTimePlan(filePath);
        TimePlan timePlan = new TimePlan();
        timePlan.setActivities(parsedActivityPool.getParsedActivities());
        printGeneratedTimePlan(timePlan);
        return timePlan;
    }

    private void generateActivitiesForTimePlan(String filePath) throws PersonaParserException {
        List<String> lines;
        try {
            Path p = Paths.get(filePath);
            lines = Files.readAllLines(p);
        } catch (IOException e) {
            PersonaParserException ppe = new PersonaParserException("Could not read lines from the specified path", e);
            //System.out.println("Could not read lines from the specified path");
            throw ppe;
        }
        // Parse persona-specific properties
        parsePersonaProperties(lines);
        // Identify the alternatives to be selected by gathering the activities within them
        selectActivities(lines);
        // Set the timing and durations of the selected activities
        setActivityTimings();
        // Set phone check activities for activities that are suitable for phone checking
        setPhoneChecks();
        // Parse remaning context values for activities
        parseActivityContexts();
        // Parse suitability for behavior
        parseBehaviorSuitability();

        //TODO delete the for below
//        for (int i = 0; i < parsedActivityPool.getParsedActivities().size(); i++) {
//            System.out.println(parsedActivityPool.getParsedActivities().get(i).getName() + "\t" + parsedActivityPool.getParsedActivities().get(i).getStart() + "\t" + parsedActivityPool.getParsedActivities().get(i).getDuration());
//        }
    }

    private void parsePersonaProperties(List<String> lines) {
        int propertyNumber = 0;
        for (String line : lines) {
            if (!line.startsWith(KEYWORD_PERSONA_PROPERTY)) {
                break;
            } else {
                propertyNumber++;
                String[] words = line.split(",")[1].split("=");
                if (words[0].startsWith(ATTRIBUTE_MIDDAY_TIME)) {
                    LocalTime parsedMiddayTime = LocalTime.parse(words[1]);
                    middayTime = DateTime.now().withTime(parsedMiddayTime.getHourOfDay(), parsedMiddayTime.getMinuteOfHour(), 0, 0);
                } else if (words[0].startsWith(ATTRIBUTE_EVENING_TIME)) {
                    LocalTime parsedMiddayTime = LocalTime.parse(words[1]);
                    eveningTime = DateTime.now().withTime(parsedMiddayTime.getHourOfDay(), parsedMiddayTime.getMinuteOfHour(), 0, 0);
                }
            }
        }

        // Delete the persona properties from the list of parsed lines
        for (int i = 0; i < propertyNumber; i++) {
            lines.remove(0);
        }
    }

    /**
     * Traverses the persona from beginning to end. During the traversal chooses particular alternatives and collects
     * the associated activities inside the global {@code chosenActivities} list.
     *
     * @param lines
     */

    private void selectActivities(List<String> lines) {
        chosenActivities = new ArrayList<String>();

        Map<Integer, ParsedGroupMetadata> parsedGroups = new HashMap<Integer, ParsedGroupMetadata>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);

            // Skip the line if it is empty or comment
            line = line.trim();
            if (line.startsWith("#") || line.equals("")) {
                continue;
            }

            // Initialize the parameters for parsing the alternative
            updateAlternativeEndpoint(line);

            if (alternativeStatus == AlternativeStatus.COMPLETED) {
                // If the parsed alternative is the last one within the group and no alternative from the group has
                // not been activated yet, return to the first alternative of the group and try to parse it again.
                if (parsedAlternative.isLast()) {
                    ParsedGroupMetadata parsedGroupMetadata = parsedGroups.get(parsedAlternative.getGroupId());
                    if (parsedGroupMetadata.getActivatedAlternative() == null) {
                        i = parsedGroupMetadata.getFirstAlternativeLineNumber() - 1;
                    }

                    remainingProbability = 100;
                }
            } else if (alternativeStatus == AlternativeStatus.IDENTIFIED) {
                ParsedGroupMetadata parsedGroupMetadata = parsedGroups.get(parsedAlternative.getGroupId());

                // Record the checkpoint for the first alternative of the group
                if (parsedGroupMetadata == null) {
                    parsedGroupMetadata = new ParsedGroupMetadata();
                    parsedGroupMetadata.setFirstAlternativeLineNumber(i);
                    parsedGroups.put(parsedAlternative.getGroupId(), parsedGroupMetadata);
                }

                // We can activate the alternative as long as another alternative in the same group has not already
                // been processed and it is activated by random chance.
                if (parsedGroupMetadata.getActivatedAlternative() == null) {
                    boolean alternativeActivated = activateAlternative();
                    if (alternativeActivated) {
                        alternativeStatus = AlternativeStatus.ACTIVE;
                        parsedGroupMetadata.setActivatedAlternative(parsedAlternative);
                    } else {
                        alternativeStatus = AlternativeStatus.REJECTED;
                    }
                } else {
                    // If the alternative is not a continuation reject it as it is another option of an alternative
                    // that was already parsed for a particular group.
                    if (!parsedAlternative.isContinuation()) {
                        alternativeStatus = AlternativeStatus.REJECTED;

                        // If the alternative is a continuation, check whether its order intersects with the
                        // parsed one.
                    } else {
                        List<Integer> orderList1 = parsedGroupMetadata.getActivatedAlternative().getOrder();
                        List<Integer> orderList2 = parsedAlternative.getOrder();
                        for (int oi1 = 0; oi1 < orderList1.size(); oi1++) {
                            for (int oi2 = 0; oi2 < orderList2.size(); oi2++) {
                                if (orderList1.get(oi1) == orderList2.get(oi2)) {
                                    alternativeStatus = AlternativeStatus.ACTIVE;
                                }
                            }
                        }
                    }
                }

                // keep the activities of the active alternative or activities that do not belong to a alternative
            } else if (alternativeStatus == AlternativeStatus.ACTIVE || alternativeStatus == AlternativeStatus.NONE) {
                chosenActivities.add(lines.get(i));
            }
        }
    }

    /**
     * Generates a random number between 0-100 and checks whether the generated number is larger than the probability
     * of {@code parsedAlternative}
     *
     * @return
     */
    private boolean activateAlternative() {
        Random randGen = new Random();
        int randProb = randGen.nextInt(100) + 1;
        if ((randProb * 100 / remainingProbability) <= parsedAlternative.getProbability()) {
            return true;
        } else {
            remainingProbability = remainingProbability - parsedAlternative.getProbability();
            return false;
        }
    }

    /**
     * Manages the status of {@link Alternative} parsing by checking the beginning and ending tags.
     *
     * @param line
     */
    private void updateAlternativeEndpoint(String line) {
        line = line.trim();
        if (line.startsWith(KEYWORD_ALTERNATIVE)) {
            parseAlternativeParameters(asList(line.split(",")));
            alternativeStatus = AlternativeStatus.IDENTIFIED;
        } else if (line.startsWith(KEYWORD_END_ALTERNATIVE)) {
            alternativeStatus = AlternativeStatus.COMPLETED;
        } else {
            // We set the status of alternative to none only if an alternative is completed.
            if (alternativeStatus == AlternativeStatus.COMPLETED) {
                alternativeStatus = AlternativeStatus.NONE;
            }
        }
    }

    private void setActivityTimings() {
        parsedActivityPool = new ParsedActivityPool();
        lineIndex = 0;
        // The line index is incremented in the setActivityTiming function below. In case the setActivityTiming function is
        // called recursively the line index is incremented by the number times the function is called recursively.
        for (; lineIndex < chosenActivities.size(); ) {
            setActivityTiming(chosenActivities.get(lineIndex));
        }
    }

    private void setActivityTiming(String activityLine) {
        //Keep the track of the index of the activity to be parsed
        lineIndex++;

        List<Activity> parsedActivities = parsedActivityPool.getParsedActivities();
        List<String> parsedLines = parsedActivityPool.getLinesForActivities();
        List<String> words = asList(activityLine.split(","));
        Activity activity = new Activity();
        parsedActivities.add(activity);
        parsedLines.add(activityLine);

        // First check whether there is a prerequisite that must be satisfied for selection of this activity
        boolean hasCondition = activityLine.contains(KEYWORD_CONDITION);
        if (hasCondition) {
            boolean conditionSatisfied = checkCondition(words, Arrays.asList(new String[]{CONDITION_CURRENT_TIME, CONDITION_RANDOM}));
            if (!conditionSatisfied) {
                //System.out.println("Condition not satisfied");
                parsedActivities.remove(parsedActivities.size() - 1);
                parsedLines.remove(parsedLines.size() - 1);
                return;
            } else {
                //System.out.println("Condition satisfied");
            }
        }

        activity.setName(words.get(OFFSET_ACTIVITY).trim());
        //System.out.println(activity.getName());

        // If the activity does not have a static start time, we consider the end time of the last activity.
        // Having relative start time implies no deviation in the start time. In other words, start time has some
        // deviation if and only if it is fixed at a certain time.
        int currentActivityIndex = parsedActivities.size() - 1;
        DateTime startTime;
        if (words.get(OFFSET_TIME).trim().equals(KEYWORD_RELATIVE)) {
            startTime = parsedActivities.get(currentActivityIndex - 1).getEndTime();

        } else {
            LocalTime parsedTime = LocalTime.parse(words.get(OFFSET_TIME).trim());
            DateTime now = DateTime.now();
            startTime = now.withHourOfDay(parsedTime.getHourOfDay()).withMinuteOfHour(parsedTime.getMinuteOfHour()).withSecondOfMinute(0).withMillisOfSecond(0);
            if (parsedTime.getHourOfDay() == 0) {
                startTime = startTime.plusDays(1);
            }

            Random r = new Random();
            int timingDeviation = Integer.valueOf(words.get(OFFSET_TIME_VARIATION).trim());
            int temp = timingDeviation;
            timingDeviation = (int) (r.nextGaussian() * timingDeviation);
            timingDeviation = timingDeviation > 0 ? Math.min(timingDeviation, temp) : Math.max(-1 * temp, timingDeviation);
            startTime = startTime.plusMinutes(timingDeviation);
        }
        activity.setStart(startTime);

        // If the duration has a relative value, the next activity should be starting at a particular time. In this
        // case we should get the starting time of the next activity.
        int duration = 0;
        if (words.get(OFFSET_DURATION).trim().equals(KEYWORD_RELATIVE)) {
            // get start time of the next activity
            int lineIndex = chosenActivities.indexOf(activityLine);
            setActivityTiming(chosenActivities.get(lineIndex + 1));
            //System.out.println("called rec parse activity");
            Activity nextActivity = parsedActivities.get(currentActivityIndex + 1);

            //
            DateTime nextStart = nextActivity.getStart();
            if (startTime.isAfter(nextStart)) {
                parsedActivities.remove(currentActivityIndex);
                parsedLines.remove(currentActivityIndex);
                //System.out.println("start time shifted");
                return;
            } else {
                DateTime durationPeriod = nextStart.minusMinutes(startTime.getHourOfDay() * 60 + startTime.getMinuteOfHour());
                duration = durationPeriod.getHourOfDay() * 60 + durationPeriod.getMinuteOfHour();
            }

        } else {
            duration = Integer.valueOf(words.get(OFFSET_DURATION).trim());
            Random r = new Random();
            int durationDeviation = Integer.valueOf(words.get(OFFSET_DURATION_VARIATION).trim());
            int temp = durationDeviation;
            durationDeviation = (int) r.nextGaussian() * durationDeviation;
            durationDeviation = durationDeviation > 0 ? Math.min(durationDeviation, temp) : Math.max(-1 * temp, durationDeviation);
            duration = duration + durationDeviation;
        }

        // check is there a maximum end time specified. If so, adjust the duration accordingly
        if (hasCondition && checkCondition(words, Arrays.asList(new String[]{CONDITION_MAX_TIME}))) {
            LocalTime parsedTime = LocalTime.parse(words.get(OFFSET_CONDITION + 1).split("=")[1].trim());
            DateTime now = DateTime.now();
            DateTime maxEnd = now.withHourOfDay(parsedTime.getHourOfDay()).withMinuteOfHour(parsedTime.getMinuteOfHour()).withSecondOfMinute(0).withMillisOfSecond(0);
            if (activity.getEndTime().isAfter(maxEnd)) {
                if (activity.getStart().isAfter(maxEnd)) {
                    System.out.println("****************************** activity's start time is after than the max end time: " + activity.getName() + ", " + activity.getStart());
                }
                DateTime difference = maxEnd.minusHours(activity.getEndTime().getHourOfDay()).minus(activity.getEndTime().getMinuteOfHour());
                System.out.println("First duration in max end check: " + duration);
                duration = duration - difference.getHourOfDay() * 60 - difference.getMinuteOfHour();
                System.out.println("Last duration in max end check: " + duration);
            }

        }
        activity.setDuration(duration);

        // Check whether we need to shift times because of inconsistencies between start and end time
        for (int i = currentActivityIndex; (i + 1) < parsedActivityPool.parsedActivities.size(); i++) {
            Activity currentActivity = parsedActivities.get(i);
            Activity nextActivity = parsedActivities.get(i + 1);
            if (currentActivity.getStart().isAfter(nextActivity.getStart())) {
                nextActivity.setStart(currentActivity.getEndTime());
                System.out.println("current start: " + currentActivity.getStart() + ", next start: " + nextActivity.getStart() + ", new next start: " + currentActivity.getEndTime());
            }
        }

        //System.out.println("at the end for index: " + lineIndex);
    }

    private void parseActivityContexts() throws PersonaParserException {
        List<Activity> parsedActivities = parsedActivityPool.getParsedActivities();
        List<String> activityLines = parsedActivityPool.getLinesForActivities();
        ContextParser contextParser = new ContextParser();
        for (int i = 0; i < parsedActivities.size(); i++) {
            contextParser.parseContextValues(activityLines.get(i), parsedActivities.get(i));
        }
    }

    /**
     * This function embeds some phone checks into the long-lasting activities.
     */
    private void setPhoneChecks() {
        List<Activity> parsedActivities = parsedActivityPool.getParsedActivities();
        List<String> parsedActivityLines = parsedActivityPool.getLinesForActivities();

        for (int i = 0; i < parsedActivities.size(); i++) {
            String line = parsedActivityLines.get(i);
            List<String> words = asList(line.split(","));
            Activity activity = parsedActivities.get(i);

            // If there are specific emotional context values for the phone usage activity
            if (!words.get(OFFSET_PHONE_CHECK).trim().equals("0") && !words.get(OFFSET_PHONE_CHECK).trim().equals("1")) {
                while (true) {
                    Random rand = new Random();
                    int nextPhoneCheck = rand.nextInt(10) + 10;
                    DateTime phoneCheckOffset = activity.getStart().plusMinutes(nextPhoneCheck);
                    DateTime activityEnd = activity.getEndTime();

                    // If the next time when a phone check occurs is after than the end time of the activity, we should
                    // stop adding anymore phone checks
                    if (phoneCheckOffset.equals(activityEnd) || phoneCheckOffset.isAfter(activityEnd)) {
                        break;
                    }

                    int phoneCheckDuration = getPhoneCheckDuration(activity);

                    // End of the phone check is within the main activity boundaries. So, split the main activity into three
                    // such that the phone check activity divides the main activity into two.
                    List<Activity> splittedActivities = addPhoneCheckActivity(activity, nextPhoneCheck, phoneCheckDuration);
                    parsedActivities.remove(i);
                    parsedActivityLines.remove(i);
                    parsedActivities.addAll(i, splittedActivities);
                    // Add the same line for each activity after splitting operation
                    for (int j = 0; j < splittedActivities.size(); j++) {
                        parsedActivityLines.add(i, line);
                    }

                    i += splittedActivities.size() - 1;
                    //System.out.println("i: " + i);

                    // Continue splitting with the remaining part of the initial activity
                    if (splittedActivities.size() == 3) {
                        activity = splittedActivities.get(2);
                    } else {
                        break;
                    }
                }

                // if the activity is suitable for phone check
            } else if (words.get(OFFSET_PHONE_CHECK).trim().equals("1")) {
                activity.getContext().setPhoneCheckSuitability(true);
            }
        }
    }

    private List<Activity> addPhoneCheckActivity(Activity initialActivity, int nextPhoneCheck, int phoneCheckDuration) {
        List<Activity> activities = new ArrayList<>();
        activities.add(initialActivity);

        DateTime phoneCheckOffset = initialActivity.getStart().plusMinutes(nextPhoneCheck);
        DateTime phoneCheckEnd = phoneCheckOffset.plusMinutes(phoneCheckDuration);
        DateTime activityEnd = initialActivity.getEndTime();

        //System.out.println("pce: " + phoneCheckEnd + "  ae: " + activityEnd);

        if (phoneCheckEnd.isBefore(activityEnd)) {
            // second part of the main activity
            Activity secondPart = initialActivity.copy();
            secondPart.setStart(phoneCheckEnd);
            secondPart.setDuration(initialActivity.getDuration() - nextPhoneCheck - phoneCheckDuration);

            // first part of the main activity
            initialActivity.setDuration(nextPhoneCheck);

            // phone check
            Activity phoneCheckActivity = new Activity("Phone check", phoneCheckOffset, phoneCheckDuration, initialActivity.getContext());
            phoneCheckActivity.getContext().setPhoneCheckSuitability(true);
            activities.add(phoneCheckActivity);
            activities.add(secondPart);

            // End of the phone check is outside the boundaries of the main activity. So, cut the phone check
            // activity such that it would exceed the end of the main activity.
        } else {
            phoneCheckDuration = initialActivity.getDuration() - nextPhoneCheck;
            Activity phoneCheckActivity = new Activity("Phone check", phoneCheckOffset, phoneCheckDuration, initialActivity.getContext());
            activities.add(phoneCheckActivity);

            initialActivity.setDuration(nextPhoneCheck);
        }

//        System.out.println("line index: " + lineIndex);
//        for (int i = 0; i < activities.size(); i++) {
//            System.out.println("splitted activities: " + activities.get(i).getName() + ": " + activities.get(i).getStart() + " duration: " + activities.get(i).getDuration());
//        }
        return activities;
    }

    private int getPhoneCheckDuration(Activity activity) {
        int phoneCheckDuration = 0;
        Random rand = new Random();
        int probability = rand.nextInt(100);
        if (activity.getStart().isBefore(middayTime)) {
            if (probability < 80) {
                phoneCheckDuration = 1 + rand.nextInt(2);
            } else if (probability >= 80 && probability < 95) {
                phoneCheckDuration = 4 + rand.nextInt(5) - 2;
            } else {
                phoneCheckDuration = 7 + rand.nextInt(6) - 3;
            }
        } else if (activity.getStart().isBefore(eveningTime)) {
            if (probability < 60) {
                phoneCheckDuration = 1 + rand.nextInt(2);
            } else if (probability >= 60 && probability < 95) {
                phoneCheckDuration = 4 + rand.nextInt(5) - 2;
            } else {
                phoneCheckDuration = 7 + rand.nextInt(6) - 3;
            }
        } else {
            if (probability < 50) {
                phoneCheckDuration = 1 + rand.nextInt(2);
            } else if (probability >= 60 && probability < 75) {
                phoneCheckDuration = 4 + rand.nextInt(5) - 2;
            } else {
                phoneCheckDuration = 10 + rand.nextInt(7) - 3;
            }
        }
        return phoneCheckDuration;
    }

    private void parseBehaviorSuitability() {
        List<Activity> parsedActivities = parsedActivityPool.getParsedActivities();
        List<String> activityLines = parsedActivityPool.getLinesForActivities();

        for(int i=0; i<activityLines.size(); i++) {
            String[] words = activityLines.get(i).split(",");
            if(Integer.parseInt(words[OFFSET_BEHAVIOR_SUITABILITY]) == 0) {
                parsedActivities.get(i).setSuitableForBehavior(false);
            } else {
                parsedActivities.get(i).setSuitableForBehavior(true);
            }
        }
    }

    private boolean isPhoneCheckHasSpecificContext(List<String> words) {
        if (!words.get(OFFSET_PHONE_CHECK).trim().equals("0") && !words.get(OFFSET_PHONE_CHECK).trim().equals("1")) {
            return true;
        }
        return false;
    }

    private boolean checkCondition(List<String> words, List<String> conditionTypes) {
        String condStr = words.get(OFFSET_CONDITION + 1);
        if (condStr.contains("<")) {
            String[] condParams = words.get(1).split("<");
            if (condParams[0].trim().equals(CONDITION_CURRENT_TIME) && conditionTypes.contains(CONDITION_CURRENT_TIME)) {
                LocalTime condTime = LocalTime.parse(condParams[1]);
                DateTime now = DateTime.now();
                DateTime condDate = now.withHourOfDay(condTime.getHourOfDay()).withMinuteOfHour(condTime.getMinuteOfHour()).withSecondOfMinute(0).withMillisOfSecond(0);
                List<Activity> parsedActivities = parsedActivityPool.getParsedActivities();
                if (parsedActivities.get(parsedActivities.size() - 2).getEndTime().isBefore(condDate)) {
                    return true;
                }
            } else if (condParams[0].trim().equals(CONDITION_RANDOM) && conditionTypes.contains(CONDITION_RANDOM)) {
                Random r = new Random();
                if (r.nextDouble() < Double.valueOf(condParams[1])) {
                    return true;
                }
            }
        } else if (condStr.contains("=")) {
            if (condStr.contains(CONDITION_MAX_TIME)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the values of alternative parameters.
     * Example alternative configuration: <code>:alternative,group=1,order=1,probability=60</code>
     *
     * @param words
     */
    private void parseAlternativeParameters(List<String> words) {
        parsedAlternative = new Alternative();
        for (int i = 1; i < words.size(); i++) {
            String[] attrDetails = words.get(i).split("=");
            String attrType = attrDetails[0].trim();
            if (attrType.equals(ATTRIBUTE_GROUP)) {
                parsedAlternative.setGroupId(Integer.valueOf(attrDetails[1]));
            } else if (attrType.equals(ATTRIBUTE_ORDER)) {
                parsedAlternative.addOrder(Integer.valueOf(attrDetails[1]));
            } else if (attrType.equals(ATTRIBUTE_PROBABILITY)) {
                parsedAlternative.setProbability(Integer.valueOf(attrDetails[1]));
            } else if (attrType.equals(ATTRIBUTE_LAST)) {
                parsedAlternative.setLast(true);
            } else if (attrType.equals(ATTRIBUTE_CONTINUATION)) {
                parsedAlternative.setContinuation(true);
            }
        }
    }

    private class ParsedGroupMetadata {
        private int firstAlternativeLineNumber;
        private Alternative activatedAlternative;

        public int getFirstAlternativeLineNumber() {
            return firstAlternativeLineNumber;
        }

        public Alternative getActivatedAlternative() {
            return activatedAlternative;
        }

        public void setFirstAlternativeLineNumber(int firstAlternativeLineNumber) {
            this.firstAlternativeLineNumber = firstAlternativeLineNumber;
        }

        public void setActivatedAlternative(Alternative activatedAlternative) {
            this.activatedAlternative = activatedAlternative;
        }
    }

    private class ParsedActivityPool {
        private List<Activity> parsedActivities = new ArrayList<>();

        private List<String> linesForActivities = new ArrayList<>();

        public List<Activity> getParsedActivities() {
            return parsedActivities;
        }

        public List<String> getLinesForActivities() {
            return linesForActivities;
        }
    }

    private void printGeneratedTimePlan(TimePlan timePlan) {
        for(Activity activity : timePlan.getActivities()) {
            //System.out.println(activity.toString());
        }
    }
}
