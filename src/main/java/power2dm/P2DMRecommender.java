package power2dm;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;

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

    public void QLearningExample(String outputPath){

        LearningAgent agent = new QLearning(domain, 0.99, hashingFactory, 0., 1.);

        //run learning for 50 episodes
        for(int i = 0; i < 50; i++){
            EpisodeAnalysis ea = agent.runLearningEpisode(env);

            ea.writeToFile(outputPath + "ql_" + i);
            System.out.println(i + ": " + ea.maxTimeStep());

            //reset environment for next learning episode
            env.resetEnvironment();
        }
    }
}
