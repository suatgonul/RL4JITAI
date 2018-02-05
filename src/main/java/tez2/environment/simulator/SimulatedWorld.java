package tez2.environment.simulator;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.EnvironmentObserver;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import org.joda.time.DateTime;
import tez2.environment.SelfManagementEnvironment;
import tez2.environment.context.DayType;
import tez2.environment.context.PartOfDay;
import tez2.persona.Activity;
import tez2.persona.TimePlan;
import tez2.persona.parser.PersonaParser;
import tez2.persona.parser.PersonaParserException;

/**
 * Created by suatgonul on 12/2/2016.
 */
public class SimulatedWorld extends SelfManagementEnvironment {
    /*
     * dynamically updated values throughout the simulation of activities
     */
    private double habitStrength;
    private double behaviorFrequency;

    private int currentDay;
    private TimePlan currentTimePlan;
    private Activity currentActivity;
    private boolean lastActivity;

    private String personaFolder;
    private String personId;

    public SimulatedWorld(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, String personaFolder, String personId) {
        super(domain, rf, tf, stateChangeFrequency);
        this.personaFolder = personaFolder;
        this.personId = personId;
        this.currentDay = 1;
        episodeInit();
        this.curState = stateGenerator.generateState();
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

        EnvironmentOutcome eo = new EnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState));

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
    protected void episodeInit() throws WorldSimulationException {
        // initialize time plan
        PersonaParser personaParser = new PersonaParser();
        String personaPath = personaFolder + "/" + personId + "/weekday.csv";

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
    }
}
