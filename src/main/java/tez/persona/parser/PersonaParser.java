package tez.persona.parser;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import tez.persona.Activity;
import tez.persona.TimePlan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by suatgonul on 12/10/2016.
 * Implements the parsing logic used to create the persona time plans
 */
public class PersonaParser {
    private static String KEYWORD_ALTERNATIVE = ":alternative";
    private static String KEYWORD_END_ALTERNATIVE = ":end_alternative";
    private static String KEYWORD_POSSIBILITY = ":possibility";
    private static String KEYWORD_CONDITION = ":cond";
    private static String KEYWORD_RELATIVE = "rel";
    private static String ATTRIBUTE_GROUP = "group";
    private static String ATTRIBUTE_ORDER = "order";
    private static String ATTRIBUTE_PROBABILITY = "probability";
    private static String ATTRIBUTE_LAST = "last";
    private static String ATTRIBUTE_CONTINUATION = "continuation";
    private static String CONDITION_CURRENT_TIME = "current_time";
    private static String CONDITION_RANDOM = "rand";

    private TimePlan timePlan = new TimePlan();
    private Alternative parsedAlternative;
    private AlternativeStatus alternativeStatus = AlternativeStatus.NONE;
    private int remainingProbability = 100;
    private List<String> chosenActivities;
    private List<Activity> parsedActivities;

    private enum AlternativeStatus {
        NONE, IDENTIFIED, ACTIVE, REJECTED, COMPLETED;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++)
            new PersonaParser().createTimePlanForPersona("src/main/resources/persona/officejob/weekday.csv");
    }

    private void createTimePlanForPersona(String filePath) throws IOException {
        Path p = Paths.get(filePath);
        List<String> lines = Files.readAllLines(p);
        selectActivities(lines);

        instantiateActivities();

        //TODO delete the for below
        for (int i = 0; i < parsedActivities.size(); i++) {
            System.out.println(parsedActivities.get(i).getName() + "\t" + parsedActivities.get(i).getStart() + "\t" + parsedActivities.get(i).getDuration());
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
                if (!line.trim().equals("")) {
                    chosenActivities.add(lines.get(i));
                }
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
        if (line.startsWith(KEYWORD_ALTERNATIVE)) {
            parseAlternativeParameters(Arrays.asList(line.split(",")));
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

    private void instantiateActivities() {
        parsedActivities = new ArrayList<Activity>();
        for (int i = 0; i < chosenActivities.size(); i++) {
            int currentSize = parsedActivities.size();
            parseActivity(chosenActivities.get(i));
            i = i + parsedActivities.size() - currentSize - 1;
        }
    }

    private void parseActivity(String activityLine) {
        int activityParameterOffset = 0;
        List<String> words = Arrays.asList(activityLine.split(","));
        Activity firstActivity = new Activity();
        firstActivity.setName(words.get(activityParameterOffset));

        int currentActivityIndex = parsedActivities.size();
        parsedActivities.add(firstActivity);
        System.out.println("current act index: " + currentActivityIndex + " name: " + words.get(0));

        // First check whether there is a prerequisite that must be satisfied for selection of this activity
        if (words.get(0).equals(KEYWORD_CONDITION)) {
            boolean conditionSatisfied = checkCondition(words);
            if (!conditionSatisfied) {
                System.out.println("Condition not satisfied");
                parsedActivities.remove(firstActivity);
                return;
            } else {
                activityParameterOffset = activityParameterOffset + 2;
            }
        }

        // If the activity does not have a static start time, we consider the end time of the last activity.
        // Having relative start time implies no deviation in the start time. In other words, start time has some
        // deviation if and only if it is fixed at a certain time.
        DateTime startTime;
        if (words.get(activityParameterOffset + 1).equals(KEYWORD_RELATIVE)) {
            startTime = parsedActivities.get(currentActivityIndex - 1).getEndTime();

        } else {
            LocalTime parsedTime = LocalTime.parse(words.get(activityParameterOffset + 1));
            DateTime now = DateTime.now();
            startTime = now.withHourOfDay(parsedTime.getHourOfDay()).withMinuteOfHour(parsedTime.getMinuteOfHour()).withSecondOfMinute(0).withMillisOfSecond(0);
            if (parsedTime.getHourOfDay() == 0) {
                startTime = startTime.plusDays(1);
            }

            Random r = new Random();
            int timingDeviation = Integer.valueOf(words.get(activityParameterOffset + 2));
            int temp = timingDeviation;
            timingDeviation = (int) (r.nextGaussian() * timingDeviation);
            timingDeviation = timingDeviation > 0 ? Math.min(timingDeviation, temp) : Math.max(-1 * temp, timingDeviation);
            startTime = startTime.plusMinutes(timingDeviation);
        }
        firstActivity.setStart(startTime);

        // If the duration has a relative value, the next activity should be starting at a particular time. In this
        // case we should get the starting time of the next activity.
        int duration = 0;
        if (words.get(activityParameterOffset + 3).equals(KEYWORD_RELATIVE)) {
            // get start time of the next activity
            int lineIndex = chosenActivities.indexOf(activityLine);
            parseActivity(chosenActivities.get(lineIndex + 1));
            System.out.println("called rec parse activity");
            Activity nextActivity = parsedActivities.get(currentActivityIndex + 1);

            //
            DateTime nextStart = nextActivity.getStart();
            if (startTime.isAfter(nextStart)) {
                parsedActivities.remove(firstActivity);
                System.out.println("start time shifted");
                return;
            } else {
                DateTime durationPeriod = nextStart.minusMinutes(startTime.getHourOfDay() * 60 + startTime.getMinuteOfHour());
                duration = durationPeriod.getHourOfDay() * 60 + durationPeriod.getMinuteOfHour();
            }

        } else {
            duration = Integer.valueOf(words.get(activityParameterOffset + 3));
            Random r = new Random();
            int durationDeviation = Integer.valueOf(words.get(activityParameterOffset + 4));
            int temp = durationDeviation;
            durationDeviation = (int) r.nextGaussian() * durationDeviation;
            durationDeviation = durationDeviation > 0 ? Math.min(durationDeviation, temp) : Math.max(-1 * temp, durationDeviation);
            duration = duration + durationDeviation;
        }

        // check is there a maximum end time specified. If so, adjust the duration accordingly
        if (words.size() == (activityParameterOffset + 7)) {
            LocalTime parsedTime = LocalTime.parse(words.get(activityParameterOffset + 6));
            DateTime now = DateTime.now();
            DateTime maxEnd = now.withHourOfDay(parsedTime.getHourOfDay()).withMinuteOfHour(parsedTime.getMinuteOfHour()).withSecondOfMinute(0).withMillisOfSecond(0);
            if(firstActivity.getEndTime().isAfter(maxEnd)) {

            }

        }
        firstActivity.setDuration(duration);

        // Check whether we need to shift times because of inconsistencies between start and end time
        for (int i = currentActivityIndex; (i + 1) < parsedActivities.size(); i++) {
            Activity currentActivity = parsedActivities.get(i);
            Activity nextActivity = parsedActivities.get(i + 1);
            if (currentActivity.getStart().isAfter(nextActivity.getStart())) {
                nextActivity.setStart(currentActivity.getEndTime());
                System.out.println("current start: " + currentActivity.getStart() + ", next start: " + nextActivity.getStart() + ", new next start: " + currentActivity.getEndTime());
            }
        }

        System.out.println("at the end for index: " + currentActivityIndex);
    }

    private boolean checkCondition(List<String> words) {
        String condStr = words.get(1);
        if (condStr.contains("<")) {
            String[] condParams = words.get(1).split("<");
            if (condParams[0].equals(CONDITION_CURRENT_TIME)) {
                DateTime condTime = DateTime.parse(condParams[1]);
                if (timePlan.getEndTime().isBefore(condTime)) {
                    return true;
                }
            } else if (condParams[0].equals(CONDITION_RANDOM)) {
                Random r = new Random();
                if (r.nextDouble() < Double.valueOf(condParams[1])) {
                    return true;
                }
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
            if (attrDetails[0].equals(ATTRIBUTE_GROUP)) {
                parsedAlternative.setGroupId(Integer.valueOf(attrDetails[1]));
            } else if (attrDetails[0].equals(ATTRIBUTE_ORDER)) {
                parsedAlternative.addOrder(Integer.valueOf(attrDetails[1]));
            } else if (attrDetails[0].equals(ATTRIBUTE_PROBABILITY)) {
                parsedAlternative.setProbability(Integer.valueOf(attrDetails[1]));
            } else if (attrDetails[0].equals(ATTRIBUTE_LAST)) {
                parsedAlternative.setLast(true);
            } else if (attrDetails[0].equals(ATTRIBUTE_CONTINUATION)) {
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
}
