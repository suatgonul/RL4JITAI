package power2dm;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;

import java.util.List;

/**
 * Created by suat on 08-Apr-16.
 */
public class P2DMRecommender {

    public static final String ACTION_DELIVER = "deliver";

    private P2DMDomain domain;
    private RewardFunction rf;
    private TerminalFunction tf;
    private StateConditionTest goalCondition;
    private State initialState;
    private HashableStateFactory hashingFactory;
    private Environment env;

    public static void main(String[] args) {
        P2DMRecommender example = new P2DMRecommender();
        String outputPath = "output/"; //directory to record results

        //run example
        example.QLearningExample(outputPath);
    }

    public P2DMRecommender() {
        domain = new P2DMDomain();
        rf = new DailyRewardFunction();
        tf = new DailyTerminalFunction();
        goalCondition = new TFGoalCondition(tf);

        initialState = domain.getInitialState();
        hashingFactory = new SimpleHashableStateFactory();

        env = new SimulatedEnvironment(domain, rf, tf, initialState);
    }

    public void QLearningExample(String outputPath) {

        LearningAgent agent = new QLearning(domain, 0.99, hashingFactory, 0., 1.);

        //run learning for 50 episodes
        for (int i = 0; i < 50; i++) {
            EpisodeAnalysis ea = agent.runLearningEpisode(env, 100);

            ea.writeToFile(outputPath + "ql_" + i);
            System.out.println("episode : " + i);
            for (int a = 0; a < ea.actionSequence.size(); a++) {
                GroundedAction ga = ea.actionSequence.get(a);
//                if (a >= 17 && a <= 21) {
                if (ga.actionName().equals("intervention_delivery")) {
//                    System.out.print(" Hour " + a + ": " + ga.actionName());
                    System.out.print("Hour " + a + ", ");
                    State state = ea.stateSequence.get(a);
                    List<QValue> qValues = ((QLearning) agent).getQs(state);
                    System.out.print("State " + state.getFirstObjectOfClass(P2DMDomain.CLASS_STATE).getIntValForAttribute(P2DMDomain.ATT_REACTED_INT) + ", ");
                    System.out.print("Rew " + ea.rewardSequence.get(a) + ", ");
                    System.out.print("Q-Val " + qValues.get(0).a.actionName() + ": " + qValues.get(0).q + ", ");
                    System.out.print(qValues.get(1).a.actionName() + ": " + qValues.get(1).q);
                    System.out.println();
                }
            }
            System.out.println();

            //reset environment for next learning episode
            env.resetEnvironment();
        }
    }
}
