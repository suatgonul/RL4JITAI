package tez2.environment.simulator;

import burlap.behavior.singleagent.learning.LearningAgentFactory;
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
import burlap.oomdp.statehashing.HashableState;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import tez2.algorithm.collaborative_learning.StateClassifier;
import tez2.algorithm.collaborative_learning.js.SparkJsStateClassifier;
import tez2.algorithm.jitai_selection.JsQLearning;
import tez2.domain.DomainConfig;
import tez2.domain.omi.OmiEnvironmentOutcome;
import tez2.domain.TerminalState;
import tez2.environment.SelfManagementEnvironment;
import tez2.environment.context.*;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;
import tez2.experiment.performance.js.JsEpisodeAnalysis;
import tez2.persona.ActionPlan;
import tez2.persona.Activity;
import tez2.persona.PersonaConfig;
import tez2.persona.TimePlan;
import tez2.persona.parser.PersonaParser;
import tez2.persona.parser.PersonaParserException;

import java.util.*;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suatgonul on 12/2/2016.
 */
public class SimulatedWorld extends SelfManagementEnvironment {

    /*
     * dynamically updated values throughout the simulation of activities
     */
    private int currentDay;
    private int dayOfMonth;
    private TimePlan currentTimePlan;
    private Activity currentActivity;
    private boolean lastActivity;
    private GroundedAction lastAction;
    private boolean reactedToJitai;
    private int suitableActivityCount;
    private int processedActivityCountForBehaviorPerformance;
    private int processedActivityCountForJitaiReaction;
    private int totalNumberOfSentJitais;
    private int numberOfSentJitaisForAction;
    private int numberOfReactedJitais;
    private boolean reactedToJitaiForAction;
    private Map<Integer, List<Integer>> meanOfBehaviorPerformanceTime;

    // variables keeping the values in the previous step of the execution to populate the environment outcome
    private Activity previousActivity;
    private LocalTime previousStateTime;
    private LocalTime previousJitaiDeliveryTime;
    private String personaFolder;
    private PersonaConfig config;

    // jitai selection related objects
    private boolean willRemember;
    private boolean behaviorPerformed;
    private JsEnvironment jitaiSelectionEnvironment;
    private LearningAgentFactory[] jsLearningAlternatives;
    private JsQLearning jitaiSelectionLearning;
    private JsEpisodeAnalysis jitaiSelectionEpisode;
    private List<SelfManagementEpisodeAnalysis> jsEpisodes = new ArrayList<>();
    private int checkedActionPlanIndex;
    private ActionPlan actionPlan;
    private List<Integer> actionPlanRanges;
    private GroundedAction lastSelectedJitai;
    private Map<String, Double> jitaiPreferences;


    public SimulatedWorld(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, String personaFolder, JsEnvironment jitaiSelectionEnvironment, LearningAgentFactory[] jsLearningAlternatives) {
        super(domain, rf, tf, stateChangeFrequency);
        this.personaFolder = personaFolder;
        this.currentDay = 1;
        this.jsLearningAlternatives = jsLearningAlternatives;
        this.jitaiSelectionLearning = (JsQLearning) jsLearningAlternatives[0].generateAgent();
        this.jitaiSelectionEnvironment = jitaiSelectionEnvironment;
    }

    public void setConfig(PersonaConfig config) {
        this.config = config;
        initEpisode();
        this.curState = stateGenerator.generateState();
        this.jitaiPreferences = config.getJitaiPreferences();
        this.jitaiSelectionEnvironment.setConfig(config);
        initActionPlan();
        meanOfBehaviorPerformanceTime = new HashMap<>();
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
        lastAction = ga;

        GroundedAction simGA = (GroundedAction) ga.copy();
        simGA.action = this.domain.getAction(ga.actionName());
        if (simGA.action == null) {
            throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
        }

        if (simGA.actionName().equals(ACTION_SEND_JITAI)) {
            previousJitaiDeliveryTime = currentTime.toLocalTime();
            totalNumberOfSentJitais++;
            numberOfSentJitaisForAction++;
        }

        for (EnvironmentObserver observer : this.observers) {
            observer.observeEnvironmentActionInitiation(this.getCurrentObservation(), ga);
        }

        State nextState;
        if (this.allowActionFromTerminalStates || !this.isInTerminalState()) {
            // advances the time plan
            nextState = simGA.executeIn(this.curState);

            // generates the reward based on the reaction of the user
            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
        } else {
            nextState = this.curState;
            this.lastReward = 0.;
        }

        EnvironmentOutcome eo = new OmiEnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState), previousActivity.getContext(), behaviorPerformed, previousStateTime);

        this.curState = nextState;

        for (EnvironmentObserver observer : this.observers) {
            observer.observeEnvironmentInteraction(eo);
        }

        return eo;
    }


    /**
     * Increase the time in real world by considering the start of the next activity and state time period
     */
    @Override
    public State getNextState(GroundedAction action) {
        advanceTimePlan();
        State state = getStateFromCurrentContext();
        return state;
    }

    private void advanceTimePlan() {
        // execute the jitai selection step
        List<Object> currentRange = getTimeRange();

//        System.out.println("Currenty activty start: " + currentActivity.getStart());
//        if(currentRange != null) {
//            System.out.println(currentRange.get(0));
//        } else {
//            System.out.println("current range null");
//        }

        if (currentRange != null) {
            // initialize the jitai selection environment state
            if (checkedActionPlanIndex == -1) {
                jitaiSelectionEnvironment.initEpisode();
                jitaiSelectionEpisode = new JsEpisodeAnalysis(jitaiSelectionEnvironment.getCurrentObservation());
                jsEpisodes.add(jitaiSelectionEpisode);
                jitaiSelectionEpisode.episodeNo = currentDay;
            }

            // processing the first activity overlapping with this time range
            int rangeIndex = (Integer) currentRange.get(0);
            if (rangeIndex > checkedActionPlanIndex) {
                // The if block below is executed when all the related activities are passed.
                // Because we want to check whether the user reacted to jitai or not to be able to
                // learn according to the preferences of the user on jitais
                HashableState curJitaiSelectionState = jitaiSelectionLearning.stateHash(jitaiSelectionEnvironment.getCurrentObservation());
                if (checkedActionPlanIndex > -1) {
                    jitaiSelectionLearning.executeLearningStep(jitaiSelectionEnvironment, curJitaiSelectionState, jitaiSelectionEpisode);
                }

                reactedToJitai = false;
                behaviorPerformed = false;
                reactedToJitaiForAction = false;
                suitableActivityCount = 0;
                processedActivityCountForBehaviorPerformance = 0;
                processedActivityCountForJitaiReaction = 0;
                willRemember = jitaiSelectionEnvironment.willRemember();

                ActionPlan.JitaiTimeRange timeRange = (ActionPlan.JitaiTimeRange) currentRange.get(1);
                curJitaiSelectionState = jitaiSelectionLearning.stateHash(jitaiSelectionEnvironment.getCurrentObservation());
                lastSelectedJitai = jitaiSelectionLearning.selectAction(curJitaiSelectionState);
                //lastSelectedJitai = jitaiSelectionEnvironment.getLastAction();
                checkedActionPlanIndex++;

                if (lastSelectedJitai.actionName().contentEquals(ACTION_NO_ACTION)) {
                    numberOfSentJitaisForAction = 0;
                } else {
                    numberOfSentJitaisForAction = 1;
                }

                // check the last time range
                if (checkedActionPlanIndex + 1 == actionPlan.getJitaiTimeRanges().size()) {
                    jitaiSelectionEnvironment.resetEnvironment();

                } else {
                    if (checkedActionPlanIndex % 2 == 0) {
                        // find the number of activities in which the behavior could be performed
                        LocalTime time = currentTimePlan.getStart().toLocalTime();
                        LocalTime timeRangeStart = actionPlan.getJitaiTimeRanges().get(checkedActionPlanIndex).getStartTime();
                        LocalTime timeRangeEnd = actionPlan.getJitaiTimeRanges().get(checkedActionPlanIndex).getEndTime();


                        for (int i = 0; i < currentTimePlan.getActivities().size(); i++) {
                            time = time.plusMinutes(currentTimePlan.getActivities().get(i).getDuration());
                            if (time.equals(timeRangeStart) || time.equals(timeRangeEnd) || (time.isAfter(timeRangeStart) && time.isBefore(timeRangeEnd))) {
                                if (currentTimePlan.getActivities().get(i).isSuitableForBehavior()) {
                                    suitableActivityCount++;
                                }
                            }
                        }
                    }
                }
            }
        }

        // simulate reaction to jitai and performance of behavior
        if (!lastSelectedJitai.actionName().contentEquals(ACTION_NO_ACTION) && lastAction.actionName().contentEquals(ACTION_SEND_JITAI)) {
            simulateUserReactionToJitai();
        }

        // simulate reaction to jitai and performance of behavior
        if (checkedActionPlanIndex % 2 == 0 && currentActivity.isSuitableForBehavior() && willRemember && !behaviorPerformed) {
            // perform reaction to the JITAI
            simulateBehaviorPerformance();
        }

        // advance the time plan
        previousActivity = currentActivity;
        previousStateTime = currentTime.toLocalTime();

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

            } else {
                // execute the last step of js environment
                HashableState curJitaiSelectionState = jitaiSelectionLearning.stateHash(jitaiSelectionEnvironment.getCurrentObservation());
                lastSelectedJitai = jitaiSelectionLearning.selectAction(curJitaiSelectionState);
                jitaiSelectionLearning.executeLearningStep(jitaiSelectionEnvironment, curJitaiSelectionState, jitaiSelectionEpisode);
            }
        }
    }

    void simulateBehaviorPerformance() {
        // apply gaussian distribution to select
        NormalDistribution nd = new NormalDistribution();
        double sample = nd.sample();
        double cumulativeProbability = nd.cumulativeProbability(sample);
        processedActivityCountForBehaviorPerformance++;
        double probabilityOfPerformanceInCurrentStep = (double) processedActivityCountForBehaviorPerformance / (double) suitableActivityCount;

        if (cumulativeProbability < (probabilityOfPerformanceInCurrentStep)) {
            updateBehaviorPerformanceTimeMean();
            if(actionPlanRanges.get(0) == 2 && !lastSelectedJitai.actionName().equals(DomainConfig.ACTION_NO_ACTION)) {
                //System.out.println("processed activity count: " + processedActivityCountForBehaviorPerformance + ", last jitai: " + lastSelectedJitai.actionName());
            }
            behaviorPerformed = true;
        }
    }

    private boolean simulateUserReactionToJitai() {
        processedActivityCountForJitaiReaction++;
        if(reactedToJitaiForAction) {
            return false;
        }

        int dayType = getDayType(currentDay);
        Location location = currentActivity.getContext().getLocation();
        PhysicalActivity physicalActivity = currentActivity.getContext().getPhysicalActivity();
        StateOfMind stateOfMind = currentActivity.getContext().getStateOfMind();
        EmotionalStatus emotionalStatus = currentActivity.getContext().getEmotionalStatus();
        PhoneUsage phoneUsage = currentActivity.getContext().getPhoneUsage();

        // check performance of the behavior for the reminder type jitais
        if (checkedActionPlanIndex % 2 == 0) {
            if (behaviorPerformed == true) {
                return false;
            }
        }

        // reduce the reaction ratio based on the total number of reacted interventions
        if (numberOfReactedJitais > 0 && new Random().nextDouble() > 1. / numberOfReactedJitais) {
            return false;
        }

        // check suitability of the context
        if (currentActivity.getContext().getPhoneCheckSuitability() == true) {
            boolean contextSuitable = false;

            // before going to work and at the beginning of the working day
            if ((stateOfMind == StateOfMind.CALM || stateOfMind == StateOfMind.FOCUS) &&
                    (emotionalStatus == EmotionalStatus.NEUTRAL || emotionalStatus == EmotionalStatus.RELAXED || emotionalStatus == EmotionalStatus.HAPPY) &&
                    physicalActivity == PhysicalActivity.SITTING_IN_CAR || physicalActivity == PhysicalActivity.SEDENTARY) {
                contextSuitable = true;
            }

            // check other heuristics related to reaction to a delivered intervention
            if (contextSuitable) {
                // check the time between two reactions is less than 3 hours
//                if (lastInterventionCheckTime != null && time.getMillis() - lastInterventionCheckTime.getMillis() < 3 * 60 * 60 * 1000) {
//                    return false;
//                } else {
//                    lastInterventionCheckTime = time;
//                    return true;
//                }

                // check preferences on jitais
                double rand = new Random().nextDouble();
                if (rand > jitaiPreferences.get(lastSelectedJitai.actionName()) / (double) processedActivityCountForJitaiReaction) {
                    return false;
                }

                numberOfReactedJitais++;
                reactedToJitaiForAction = true;
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    @Override
    public State getStateFromCurrentContext() {
        State s;
        if (!lastActivity) {
            s = new MutableState();
            s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

            ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_LOCATION, currentActivity.getContext().getLocation().ordinal());
            o.setValue(ATT_QUARTER_HOUR_OF_DAY, getQuarterStateRepresentation());
            o.setValue(ATT_ACTIVITY, currentActivity.getContext().getPhysicalActivity().ordinal());
            o.setValue(ATT_PHONE_USAGE, currentActivity.getContext().getPhoneUsage().ordinal());
            o.setValue(ATT_EMOTIONAL_STATUS, currentActivity.getContext().getEmotionalStatus().ordinal());
            o.setValue(ATT_NUMBER_OF_JITAIS_SENT, numberOfSentJitaisForAction);

        } else {
            s = new TerminalState();
        }

        return s;
    }

    /**
     * Initializes the variables that are reset for each episode. Variables that preserve the value across episodes
     * are not changed in this method such as habit strength.
     *
     * @throws WorldSimulationException
     */
    protected void initEpisode() throws WorldSimulationException {
        // initialize time plan
        PersonaParser personaParser = new PersonaParser();
        String personaPath = personaFolder + "/" + config.getPersonaFile();

        try {
            currentTimePlan = personaParser.getTimePlanForPersona(personaPath);
        } catch (PersonaParserException e) {
            System.out.println("Could get time plan for day of week: " + currentDay);
            throw new WorldSimulationException("Could get time plan for day of week: " + currentDay, e);
        }

        // initialize time
        currentTime = currentTimePlan.getStart();
        // initialize activity
        currentActivity = currentTimePlan.getActivities().get(0);
        dayOfMonth = currentTime.getDayOfMonth();

        // All activities are processed. Set lastActivity to false to set the initial state of the next episode properly
        lastActivity = false;

        numberOfReactedJitais = 0;
        totalNumberOfSentJitais = 0;
        previousStateTime = currentTime.toLocalTime();
        checkedActionPlanIndex = -1;
        behaviorPerformed = false;
    }

    @Override
    public void resetEnvironment() {
        if (StateClassifier.classifierModeIncludes("generate") || StateClassifier.classifierModeIncludes("generate-js")) {
            try {
                SparkJsStateClassifier.getInstance().updateLearningModel(jsEpisodes);
                jsEpisodes.clear();
            } catch (Exception e) {
                System.out.println("Failed to update learning model. Map size: " + SparkJsStateClassifier.getInstance().stateActionCounts.keySet().size());
            }
         }
        currentDay++;
        initEpisode();
        super.resetEnvironment();
    }

    public void endTrial() {
        this.currentDay = 1;
        jitaiSelectionEnvironment.endTrial();
        this.jitaiSelectionLearning = (JsQLearning) jsLearningAlternatives[0].generateAgent();

        this.jsEpisodes = new ArrayList<>();
    }

    private void initActionPlan() {

        actionPlan = new ActionPlan();
        actionPlanRanges = new ArrayList<>();
        LocalTime time = new LocalTime().withMillisOfDay(21600000); // 6 o'clock
        List<Integer> ranges = config.getActionPlanRanges();

        for (int i = 0; i < ranges.size(); i++) {
            actionPlanRanges.add(ranges.get(i));
            ActionPlan.JitaiTimeRange tr = new ActionPlan.JitaiTimeRange(time, time.plusHours(actionPlanRanges.get(i)), i % 2 == 0 ? ActionPlan.JitaiNature.REMINDER : ActionPlan.JitaiNature.MOTIVATION);
            actionPlan.addJITAITimeRange(tr);
            time = time.plusHours(ranges.get(i));
        }
    }

    private List<Object> getTimeRange() {
        for (int i = 0; i < actionPlan.getJitaiTimeRanges().size(); i++) {
            ActionPlan.JitaiTimeRange tr = actionPlan.getJitaiTimeRanges().get(i);
            DateTime controlStart = tr.getStartTime().toDateTimeToday();
            DateTime controlEnd = controlStart.plusHours(actionPlanRanges.get(i));

            if (currentTime.equals(controlStart) || (currentTime.isAfter(controlStart) && currentTime.isBefore(controlEnd))) {
                List<Object> result = new ArrayList<>();
                result.add(i);
                result.add(tr);
                return result;
            }
        }
        return null;
    }

    public DynamicSimulatedWorldContext getContext() {
        DynamicSimulatedWorldContext context = new DynamicSimulatedWorldContext();
        context.setActivity(currentActivity);
        context.setCurrentDayType(getDayType(currentDay));
        context.setCurrentDayPart(getDayPart());
        context.setExpectedJitaiNature(((ActionPlan.JitaiTimeRange) getTimeRange().get(1)).getJitaiNature());
        context.setCurrentHourOfDay(currentTime.getHourOfDay());
        return context;
    }

    public DynamicSimulatedWorldContext getLastContextForJitai(int jitaiOffset) {
        DynamicSimulatedWorldContext context = new DynamicSimulatedWorldContext();
        context.setCurrentDayType(getDayType(currentDay));
        context.setExpectedJitaiNature((actionPlan.getJitaiTimeRanges().get(jitaiOffset)).getJitaiNature());

        if (jitaiOffset == 5) {
            context.setCurrentDayPart(DayPart.NIGHT.ordinal());
            context.setActivity(currentTimePlan.getActivities().get(currentTimePlan.getActivities().size() - 1));
            return context;
        }

        DateTime time = currentTimePlan.getStart();
        DateTime timeRangeEnd;
        if (jitaiOffset < 5) {
            timeRangeEnd = new DateTime().withTime(actionPlan.getJitaiTimeRanges().get(jitaiOffset).getEndTime()).withDayOfMonth(dayOfMonth);
        } else {
            timeRangeEnd = new DateTime().withTime(actionPlan.getJitaiTimeRanges().get(jitaiOffset).getEndTime()).withDayOfMonth(dayOfMonth + 1);
        }

        for (int i = 0; i < currentTimePlan.getActivities().size(); i++) {
            time = time.plusMinutes(currentTimePlan.getActivities().get(i).getDuration());
            if (time.isEqual(timeRangeEnd) || time.isAfter(timeRangeEnd)) {
                context.setActivity(currentTimePlan.getActivities().get(i));
                context.setCurrentDayPart(getDayPart(time));
                context.setCurrentHourOfDay(time.getHourOfDay());
                break;
            }
        }
        return context;
    }

    private void updateBehaviorPerformanceTimeMean() {
        int index = checkedActionPlanIndex / 2;
        List<Integer> currentMeanInfo = meanOfBehaviorPerformanceTime.get(index);

        if (currentMeanInfo == null) {
            currentMeanInfo = new ArrayList<>();
            meanOfBehaviorPerformanceTime.put(index, currentMeanInfo);
        }

        LocalTime currentLocalTime = currentTime.toLocalTime();
        if (currentMeanInfo.size() == 0) {
            currentMeanInfo.add(1);
            currentMeanInfo.add(currentLocalTime.getHourOfDay() * 60 + currentLocalTime.getMinuteOfHour());

        } else {
            int behaviorCount = currentMeanInfo.get(0);
            int currentMean = currentMeanInfo.get(1);

            currentMean = (currentMean * behaviorCount + (currentLocalTime.getHourOfDay() * 60 + currentLocalTime.getMinuteOfHour())) / (behaviorCount + 1);
            currentMeanInfo.set(0, behaviorCount + 1);
            currentMeanInfo.set(1, currentMean);
        }
    }

    public boolean isBehaviorPerformed() {
        return behaviorPerformed;
    }

    public boolean isReactedToJitai() {
        return reactedToJitai;
    }

    public LocalTime getPreviousJitaiDeliveryTime() {
        return previousJitaiDeliveryTime;
    }

    public int getNumberOfSentJitaisForAction() {
        return numberOfSentJitaisForAction;
    }

    public boolean isReactedToJitaiForAction() {
        return reactedToJitaiForAction;
    }

    public JsEpisodeAnalysis getJsEpisodeAnalysis() {
        return jitaiSelectionEpisode;
    }

    public List<Integer> getBehaviourPerformanceTimeMean() {
        return meanOfBehaviorPerformanceTime.get(checkedActionPlanIndex / 2);
    }

    public int getCheckedActionPlanIndex() {
        return checkedActionPlanIndex;
    }

    public static class DynamicSimulatedWorldContext {
        private Activity activity;
        private int currentDayPart;
        private int currentDayType;
        private ActionPlan.JitaiNature expectedJitaiNature;
        private int currentHourOfDay;

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public int getCurrentDayPart() {
            return currentDayPart;
        }

        public void setCurrentDayPart(int currentDayPart) {
            this.currentDayPart = currentDayPart;
        }

        public int getCurrentDayType() {
            return currentDayType;
        }

        public void setCurrentDayType(int currentDayType) {
            this.currentDayType = currentDayType;
        }

        public ActionPlan.JitaiNature getExpectedJitaiNature() {
            return expectedJitaiNature;
        }

        public void setExpectedJitaiNature(ActionPlan.JitaiNature expectedJitaiNature) {
            this.expectedJitaiNature = expectedJitaiNature;
        }

        public int getCurrentHourOfDay() {
            return currentHourOfDay;
        }

        public void setCurrentHourOfDay(int currentHourOfDay) {
            this.currentHourOfDay = currentHourOfDay;
        }
    }
}
