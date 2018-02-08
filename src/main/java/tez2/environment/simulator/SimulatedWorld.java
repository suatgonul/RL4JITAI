package tez2.environment.simulator;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.EnvironmentObserver;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.statehashing.HashableState;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import tez.environment.context.DayType;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;
import tez2.algorithm.jitai_selection.JitaiSelectionQLearning;
import tez2.domain.ExtendedEnvironmentOutcome;
import tez2.environment.SelfManagementEnvironment;
import tez2.persona.ActionPlan;
import tez2.persona.Activity;
import tez2.persona.TimePlan;
import tez2.persona.parser.PersonaParser;
import tez2.persona.parser.PersonaParserException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/2/2016.
 */
public class SimulatedWorld extends SelfManagementEnvironment {
    /*
     * dynamically updated values throughout the simulation of activities
     */
    private int currentDay;
    private TimePlan currentTimePlan;
    private Activity currentActivity;
    private boolean lastActivity;


    private String personaFolder;

    // jitai selection related objects
    private JitaiSelectionEnvironment jitaiSelectionEnvironment;
    private JitaiSelectionQLearning jitaiSelectionLearning;
    private List<SelfManagementEpisodeAnalysis> jitaiSelectionResults = new ArrayList<>();
    private SelfManagementEpisodeAnalysis jitaiSelectionEpisode;
    private int checkedActionPlanIndex;
    private ActionPlan actionPlan;


    public SimulatedWorld(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, String personaFolder, JitaiSelectionEnvironment jitaiSelectionEnvironment, JitaiSelectionQLearning jitaiSelectionLearning) {
        super(domain, rf, tf, stateChangeFrequency);
        this.personaFolder = personaFolder;
        this.currentDay = 1;
        this.jitaiSelectionLearning = jitaiSelectionLearning;
        this.jitaiSelectionEnvironment = jitaiSelectionEnvironment;
        initEpisode();
        this.curState = stateGenerator.generateState();
        initActionPlan();
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

            // generates the reward based on the reaction of the user
            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
        } else {
            nextState = this.curState;
            this.lastReward = 0.;
        }

        EnvironmentOutcome eo = new ExtendedEnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState), null, false);

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
    public State getNextState() {
        advanceTimePlan();
        State state = getStateFromCurrentContext();
        return state;
    }

    private void advanceTimePlan() {
        // execute the jitai selection step
        List<Object> currentRange = getTimeRange();
        if(currentRange != null) {
            // initialize the jitai selection environment state
            if(checkedActionPlanIndex == -1) {
                jitaiSelectionEnvironment.initEpisode();
            }

            int rangeIndex = (Integer) currentRange.get(0);
            if(rangeIndex > checkedActionPlanIndex) {
                ActionPlan.JitaiTimeRange timeRange = (ActionPlan.JitaiTimeRange) currentRange.get(1);
                HashableState curJitaiSelectionState = jitaiSelectionLearning.stateHash(jitaiSelectionEnvironment.getCurrentObservation());
                jitaiSelectionLearning.executeLearningStep(jitaiSelectionEnvironment, curJitaiSelectionState, jitaiSelectionEpisode);
                checkedActionPlanIndex++;

                // check the last time range
                if(checkedActionPlanIndex+1 == actionPlan.getJitaiTimeRanges().size()) {

                }
            }
        }

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

    @Override
    public State getStateFromCurrentContext() {

        //TODO
        return null;
    }

    @Override
    public boolean simulateBehavior() {
        return false;
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
        String personaPath = personaFolder + "/weekdayv2.csv";

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

        // All activities are processed. Set lastActivity to false to set the initial state of the next episode properly
        lastActivity = false;

        // jitai-selection related initialization
        jitaiSelectionResults.add(jitaiSelectionEpisode);
        checkedActionPlanIndex = -1;
        jitaiSelectionEpisode = new SelfManagementEpisodeAnalysis(jitaiSelectionEnvironment.getCurrentObservation());
    }

    @Override
    public void resetEnvironment() {
        currentDay++;
        initEpisode();
        super.resetEnvironment();
    }

    private void initActionPlan() {
        ActionPlan ap = new ActionPlan();
        // morning time range
        // TODO
    }

    private List<Object> getTimeRange() {
        for(int i=0; i<actionPlan.getJitaiTimeRanges().size(); i++) {
            ActionPlan.JitaiTimeRange tr = actionPlan.getJitaiTimeRanges().get(i);
            if(currentTime.toLocalTime().isAfter(tr.getStartTime()) && currentTime.toLocalTime().isBefore(tr.getEndTime())) {
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
        context.setCurrentTime(currentTime);
        context.setExpectedJitaiNature(((ActionPlan.JitaiTimeRange) getTimeRange().get(1)).getJitaiNature());
        return context;
    }

    public DynamicSimulatedWorldContext getLastContextForJitai() {
        DynamicSimulatedWorldContext context = new DynamicSimulatedWorldContext();
        context.setCurrentDayType(getDayType(currentDay));
        context.setExpectedJitaiNature(((ActionPlan.JitaiTimeRange) getTimeRange().get(1)).getJitaiNature());

        LocalTime time = currentActivity.getStart().toLocalTime();
        Activity activity = currentActivity;

        for(int i=0; i<actionPlan.getJitaiTimeRanges().size(); i++) {
            ActionPlan.JitaiTimeRange tr = actionPlan.getJitaiTimeRanges().get(i);
            if(currentTime.toLocalTime().isAfter(tr.getStartTime()) && currentTime.toLocalTime().isBefore(tr.getEndTime())) {
                List<Object> result = new ArrayList<>();
                result.add(i);
                result.add(tr);
                return result;
            }
        }


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

    private void executeBehaviorInRealLife() {
        if(checkedActionPlanIndex % 2 == 0) {

        }
    }

    public static class DynamicSimulatedWorldContext {
        private Activity activity;
        private DateTime currentTime;
        private int currentDayType;
        private ActionPlan.JitaiNature expectedJitaiNature;

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public DateTime getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(DateTime currentTime) {
            this.currentTime = currentTime;
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
    }
}
