package power2dm;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import power2dm.model.DailyTerminalFunction;
import power2dm.model.P2DMDomain;
import power2dm.model.P2DMQLearning;
import power2dm.model.burden.BurdenDailyRewardFunction;
import power2dm.model.burden.BurdenEpisodeAnalyser;
import power2dm.model.burden.BurdenP2DMDomain;
import power2dm.model.burden.BurdenP2DMEnvironmentSimulator;
import power2dm.reporting.EpisodeAnalyser;
import power2dm.reporting.RunAnalyser;

/**
 * Created by suat on 08-Apr-16.
 */
public class AlgorithmTester {

    private P2DMDomain domain;
    private Environment env;
    private HashableStateFactory hashingFactory;


    public static void main(String[] args) {
        AlgorithmTester tester = new AlgorithmTester();
        tester.testReactNonReactModel();
        tester.testBurdenModel();
    }

    private void testReactNonReactModel() {
        String outputPath = "output/react_non_react"; //directory to record results
        // example.testQLearning(new GreedyQPolicy(), outputPath);
//        testQLearning(new EpsilonGreedy(0.01), outputPath);
        // example.sarsaExample(outputPath);
    }

    private void testBurdenModel() {
        // initialize the environment
        domain = new BurdenP2DMDomain(new BurdenP2DMEnvironmentSimulator());
        RewardFunction rf = new BurdenDailyRewardFunction();
        TerminalFunction tf = new DailyTerminalFunction();
        hashingFactory = new SimpleHashableStateFactory();

        State initialState = domain.getInitialState();
        env = new SimulatedEnvironment(domain, rf, tf, initialState);

        // run the algorithms
        String outputPath = "output/react_non_react"; //directory to record results
        // example.testQLearning(new GreedyQPolicy(), outputPath);
        testQLearning(new EpsilonGreedy(0.01), new BurdenEpisodeAnalyser(), outputPath);
        // example.sarsaExample(outputPath);
    }

    public void testQLearning(Policy policy, EpisodeAnalyser episodeAnalyser, String outputPath) {
        P2DMQLearning agent = new P2DMQLearning(domain, 0.5, hashingFactory, 0., 0.1, episodeAnalyser);
        agent.setLearningPolicy(policy);
        ((SolverDerivedPolicy) policy).setSolver(agent);

        RunAnalyser runAnalyser = new RunAnalyser();

        for (int i = 0; i < 3000; i++) {
            EpisodeAnalysis ea = agent.runLearningEpisode(env, 100, i);
            runAnalyser.recordEpisodeReward(policy, ea);
//            ea.writeToFile(outputPath + "ql_" + i);
//            if (isInterventionDeliveredInPrefferedRange(i, ea, agent)) {
//                totalDaysOfinterventionDeliveredInPreferredRange++;
//            }


            //reset environment for next learning episode
            env.resetEnvironment();
        }
        runAnalyser.drawRewardChards();
    }

//    private void sarsaExample(String outputPath) {
//        RunAnalyser runAnalyser = new RunAnalyser();
//        LearningAgent agent = new SarsaLam(domain, 0.99, hashingFactory, 0., 0.5, 0.3);
//
//        // TODO ==> total reward'lar episode analyser veya daha high-level bi şeyin altına taşınacak
//        List<Double> totalRewards = new ArrayList<Double>();
//
//        for (int i = 0; i < 100000; i++) {
//            EpisodeAnalysis ea = agent.runLearningEpisode(env);
//            runAnalyser.recordEpisodeReward(ea);
//            env.resetEnvironment();
//        }
//    }

//    private boolean isInterventionDeliveredInPrefferedRange(int episode, EpisodeAnalysis ea, LearningAgent agent) {
//        boolean interventionDeliveredInPreferredRange = false;
//
//        for (int a = 0; a < ea.actionSequence.size(); a++) {
//            if (a >= 19 && a <= 21) {
//                if (ea.getAction(a).actionName().equals(BurdenP2DMDomain.ACTION_INT_DELIVERY)) {
//                    interventionDeliveredInPreferredRange = true;
//                }
//            }
//        }
//        return interventionDeliveredInPreferredRange;
//    }


}
