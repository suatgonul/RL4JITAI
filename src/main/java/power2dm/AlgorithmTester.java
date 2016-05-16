package power2dm;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import power2dm.algorithm.*;
import power2dm.model.burden.BurdenDailyRewardFunction;
import power2dm.model.burden.reporting.BurdenEpisodeAnalyser;
import power2dm.model.burden.BurdenP2DMDomain;
import power2dm.model.burden.BurdenP2DMEnvironmentSimulator;
import power2dm.model.habit.HabitDailyRewardFunction;
import power2dm.model.habit.HabitP2DMDomain;
import power2dm.model.habit.NewHabitP2DMEnvironmentSimulator;
import power2dm.model.habit.reporting.HabitEpisodeAnalyser;
import power2dm.reporting.EpisodeAnalyser;
import power2dm.reporting.P2DMEpisodeAnalysis;
import power2dm.reporting.RunAnalyser;

/**
 * Created by suat on 08-Apr-16.
 */
public class AlgorithmTester {

    private P2DMDomain domain;
    private Environment env;
    private HashableStateFactory hashingFactory;

    private double epsilon = 0.001;
//    private Policy policy = new EpsilonGreedy(epsilon);
    private Policy policy = new GreedyQPolicy();
    private int episodeNum = 1000;


    public static void main(String[] args) {
        AlgorithmTester tester = new AlgorithmTester();
//        tester.testReactNonReactModel();
//        tester.testBurdenModel();
        tester.testHabitModel();
    }

    private void testReactNonReactModel() {
        String outputPath = "output/react_non_react"; //directory to record results
        // example.testQLearning(new GreedyQPolicy(), outputPath);
//        testQLearning(new EpsilonGreedy(0.01), outputPath);
        // example.testSarsaLambda(outputPath);
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
        String outputPath = "output/burden"; //directory to record results
        // example.testQLearning(new GreedyQPolicy(), outputPath);
        testQLearning(new BurdenEpisodeAnalyser(), outputPath);
        testSarsaLambda(new BurdenEpisodeAnalyser(), outputPath);
    }

    private void testHabitModel() {

        // initialize the environment
        domain = new HabitP2DMDomain();
        RewardFunction rf = new HabitDailyRewardFunction();
        TerminalFunction tf = new DailyTerminalFunction();
        hashingFactory = new SimpleHashableStateFactory();

        State initialState = domain.getInitialState();
//        env = new SimulatedEnvironment(domain, rf, tf, randomStateGenerator);
        env = new NewHabitP2DMEnvironmentSimulator(domain, rf, tf);
        ((HabitP2DMDomain) domain).setEnvironmentSimulator((NewHabitP2DMEnvironmentSimulator) env);

        StateGenerator randomStateGenerator = new P2DMRandomStateGenerator(domain, initialState, tf);
        ((NewHabitP2DMEnvironmentSimulator) env).setStateGenerator(randomStateGenerator);
        ((NewHabitP2DMEnvironmentSimulator) env).setCurStateTo(randomStateGenerator.generateState());
        ((NewHabitP2DMEnvironmentSimulator) env).initialize();

        ((HabitDailyRewardFunction) rf).setEnvironmentSimulator((NewHabitP2DMEnvironmentSimulator) env);

        // run the algorithms
        String outputPath = "output/habit"; //directory to record results
        // example.testQLearning(new GreedyQPolicy(), outputPath);
        testQLearning(new HabitEpisodeAnalyser((NewHabitP2DMEnvironmentSimulator) env), outputPath);
//        testSarsaLambda(new BurdenEpisodeAnalyser(), outputPath);
    }

    public void testQLearning(EpisodeAnalyser episodeAnalyser, String outputPath) {
        P2DMQLearning agent = new P2DMQLearning(domain, 0.5, hashingFactory, 0., 0.1, policy, 100, episodeAnalyser);

        RunAnalyser runAnalyser = new RunAnalyser();

        for (int i = 0; i < episodeNum; i++) {
            P2DMEpisodeAnalysis ea = agent.runLearningEpisode(env, 100, i);
            runAnalyser.recordEpisodeReward(policy, ea);
//            ea.writeToFile(outputPath + "ql_" + i);
//            if (isInterventionDeliveredInPrefferedRange(i, ea, agent)) {
//                totalDaysOfinterventionDeliveredInPreferredRange++;
//            }


            //reset environment for next learning episode
            env.resetEnvironment();
        }
        runAnalyser.drawRewardCharts("QLearning", policy);
    }

    private void testSarsaLambda(EpisodeAnalyser episodeAnalyser, String outputPath) {
        P2DMSarsaLambda agent = new P2DMSarsaLambda(domain, 0.99, hashingFactory, 0., 0.5, policy, 100, 0.01, episodeAnalyser);

        RunAnalyser runAnalyser = new RunAnalyser();

        for (int i = 0; i < episodeNum; i++) {
            P2DMEpisodeAnalysis ea = agent.runLearningEpisode(env, 100, i);
            runAnalyser.recordEpisodeReward(policy, ea);
            env.resetEnvironment();
        }
        runAnalyser.drawRewardCharts("Sarsa Lambda", policy);
    }

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
