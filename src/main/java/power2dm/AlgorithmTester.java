package power2dm;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import power2dm.algorithm.*;
import power2dm.model.burden.BurdenDailyRewardFunction;
import power2dm.model.burden.BurdenP2DMDomain;
import power2dm.model.burden.BurdenP2DMEnvironmentSimulator;
import power2dm.model.burden.reporting.BurdenEpisodeAnalyser;
import power2dm.model.habit.hour.HabitDailyRewardFunction;
import power2dm.model.habit.hour.HabitP2DMDomain;
import power2dm.model.habit.hour.HabitP2DMEnvironmentSimulator;
import power2dm.model.habit.hour.reporting.HabitEpisodeAnalyser;
import power2dm.model.habit.hour.reporting.visualization.HabitVisualizer;
import power2dm.model.habit.year.periodic.HabitYearPeriodicDailyRewardFunction;
import power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain;
import power2dm.model.habit.year.periodic.environment.HabitYearPeriodicP2DMEnvironmentSimulator;
import power2dm.model.habit.year.periodic.HabitYearPeriodicTerminalFunction;
import power2dm.model.habit.year.periodic.reporting.HabitYearPeriodicEpisodeAnalyser;
import power2dm.model.habit.year.periodic.reporting.visualization.HabitYearPeriodicEpisodeVisualizer;
import power2dm.model.habit.year.weighted.HabitYearDailyRewardFunction;
import power2dm.model.habit.year.weighted.HabitYearP2DMDomain;
import power2dm.model.habit.year.weighted.HabitYearP2DMEnvironmentSimulator;
import power2dm.model.habit.year.weighted.HabitYearTerminalFunction;
import power2dm.model.habit.year.weighted.reporting.HabitYearEpisodeAnalyser;
import power2dm.model.habit.year.weighted.reporting.visualization.HabitYearEpisodeVisualizer;
import power2dm.reporting.EpisodeAnalyser;
import power2dm.reporting.P2DMEpisodeAnalysis;
import power2dm.reporting.RunAnalyser;
import power2dm.reporting.visualization.RewardVisualizer;
import power2dm.reporting.visualization.Visualizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 08-Apr-16.
 */
public class AlgorithmTester {

    private P2DMDomain domain;
    private Environment env;
    private HashableStateFactory hashingFactory;

    private double epsilon = 0.001;
//        private Policy policy = new EpsilonGreedy(epsilon);
    private Policy policy = new GreedyQPolicy();
    private int episodeNum = 20006;


    public static void main(String[] args) {
        HabitGainRatio.initializeRatios();
        AlgorithmTester tester = new AlgorithmTester();
//        tester.testReactNonReactModel();
//        tester.testBurdenModel();
//        tester.testHabitModel();
//        tester.testHabitYearModel();
        tester.testHabitYearPeriodicModel();
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
        testQLearning(new BurdenEpisodeAnalyser(), Arrays.asList(new Class[] {RewardVisualizer.class}), outputPath);
        testSarsaLambda(new BurdenEpisodeAnalyser(), Arrays.asList(new Class[] {RewardVisualizer.class}), outputPath);
    }

    private void testHabitModel() {

        // initialize the environment
        domain = new HabitP2DMDomain();
        RewardFunction rf = new HabitDailyRewardFunction();
        TerminalFunction tf = new DailyTerminalFunction();
        hashingFactory = new SimpleHashableStateFactory();

        // start to episode always from the initial state
        State initialState = domain.getInitialState();
        env = new HabitP2DMEnvironmentSimulator(domain, rf, tf, initialState);
        ((HabitP2DMDomain) domain).setEnvironmentSimulator((HabitP2DMEnvironmentSimulator) env);
        ((HabitP2DMEnvironmentSimulator) env).initialize();
        ((HabitDailyRewardFunction) rf).setEnvironmentSimulator((HabitP2DMEnvironmentSimulator) env);

        // start to episode from a random state
        /*State initialState = domain.getInitialState();
        env = new HabitYearP2DMEnvironmentSimulator(domain, rf, tf);
        ((HabitYearPeriodicP2DMDomain) domain).setEnvironmentSimulator((HabitYearP2DMEnvironmentSimulator) env);

        StateGenerator randomStateGenerator = new P2DMRandomStateGenerator(domain, initialState, tf);
        ((HabitYearP2DMEnvironmentSimulator) env).setStateGenerator(randomStateGenerator);
        ((HabitYearP2DMEnvironmentSimulator) env).setCurStateTo(randomStateGenerator.generateState());
        ((HabitYearP2DMEnvironmentSimulator) env).initialize();

        ((HabitYearPeriodicDailyRewardFunction) rf).setEnvironmentSimulator((HabitYearP2DMEnvironmentSimulator) env);*/



        // run the algorithms
        String outputPath = "output/habit"; //directory to record results
        // example.testQLearning(new GreedyQPolicy(), outputPath);
        testQLearning(new HabitEpisodeAnalyser((HabitP2DMEnvironmentSimulator) env), Arrays.asList(new Class[] {RewardVisualizer.class, HabitVisualizer.class}), outputPath);
//        testSarsaLambda(new HabitEpisodeAnalyser((HabitP2DMEnvironmentSimulator) env), Arrays.asList(new Class[] {RewardVisualizer.class, HabitVisualizer.class}), outputPath);
    }

    private void testHabitYearModel() {

        // initialize the environment
        domain = new HabitYearP2DMDomain();
        RewardFunction rf = new HabitYearDailyRewardFunction();
        TerminalFunction tf = new HabitYearTerminalFunction();
        hashingFactory = new SimpleHashableStateFactory();

        // start to episode always from the initial state
        State initialState = domain.getInitialState();
        env = new HabitYearP2DMEnvironmentSimulator(domain, rf, tf, initialState);
        ((HabitYearP2DMDomain) domain).setEnvironmentSimulator((HabitYearP2DMEnvironmentSimulator) env);

        ((HabitYearDailyRewardFunction) rf).setEnvironmentSimulator((HabitYearP2DMEnvironmentSimulator) env);
        ((HabitYearTerminalFunction) tf).setEnvironmentSimulator((HabitYearP2DMEnvironmentSimulator) env);

        // run the algorithms
        String outputPath = "output/habit"; //directory to record results
        testQLearning(new HabitYearEpisodeAnalyser((HabitYearP2DMEnvironmentSimulator) env), Arrays.asList(new Class[] {RewardVisualizer.class, HabitYearEpisodeVisualizer.class}), outputPath);
//        testSarsaLambda(new HabitYearEpisodeAnalyser((HabitYearP2DMEnvironmentSimulator) env), Arrays.asList(new Class[] {RewardVisualizer.class, HabitYearEpisodeVisualizer.class}), outputPath);
    }

    private void testHabitYearPeriodicModel() {
        // initialize the environment
        domain = new HabitYearPeriodicP2DMDomain();
        RewardFunction rf = new HabitYearPeriodicDailyRewardFunction();
        TerminalFunction tf = new HabitYearPeriodicTerminalFunction();
        hashingFactory = new SimpleHashableStateFactory();

        // start to episode always from the initial state
        State initialState = domain.getInitialState();
        env = new HabitYearPeriodicP2DMEnvironmentSimulator(domain, rf, tf, initialState);
        ((HabitYearPeriodicP2DMDomain) domain).setEnvironmentSimulator((HabitYearPeriodicP2DMEnvironmentSimulator) env);

        ((HabitYearPeriodicDailyRewardFunction) rf).setEnvironmentSimulator((HabitYearPeriodicP2DMEnvironmentSimulator) env);
        ((HabitYearPeriodicTerminalFunction) tf).setEnvironmentSimulator((HabitYearPeriodicP2DMEnvironmentSimulator) env);

        // run the algorithms
        String outputPath = "output/habit"; //directory to record results
        testQLearning(new HabitYearPeriodicEpisodeAnalyser((HabitYearPeriodicP2DMEnvironmentSimulator) env), Arrays.asList(new Class[] {RewardVisualizer.class, HabitYearPeriodicEpisodeVisualizer.class}), outputPath);
//        testSarsaLambda(new HabitYearEpisodeAnalyser((HabitYearP2DMEnvironmentSimulator) env), Arrays.asList(new Class[] {RewardVisualizer.class, HabitYearEpisodeVisualizer.class}), outputPath);
    }

    public void testQLearning(EpisodeAnalyser episodeAnalyser, List<Class> visualizerClasses, String outputPath) {
        P2DMQLearning agent = new P2DMQLearning(domain, 0.9, hashingFactory, 0., 0.1, policy, episodeAnalyser);

        RunAnalyser runAnalyser = new RunAnalyser();

        for (int i = 0; i < episodeNum; i++) {
            P2DMEpisodeAnalysis ea = agent.runLearningEpisode(env, 365, i);
            runAnalyser.recordEpisodeReward(ea);
//            ea.writeToFile(outputPath + "ql_" + i);
//            if (isInterventionDeliveredInPrefferedRange(i, ea, agent)) {
//                totalDaysOfinterventionDeliveredInPreferredRange++;
//            }

            if(i %1000 == 0) System.out.println("E:" + i);
            //reset environment for next learning episode
            env.resetEnvironment();
        }

        Map<String, Object> visualizationMetadata = prepareVisualizationMetadata("Q-Learning", policy, "Total Rewards", "Reward", "Episode");
        runAnalyser.drawRewardCharts(visualizerClasses, visualizationMetadata);
    }

    private void testSarsaLambda(EpisodeAnalyser episodeAnalyser, List<Class> visualizerClasses, String outputPath) {
        P2DMSarsaLambda agent = new P2DMSarsaLambda(domain, 0.9, hashingFactory, 0., 0.1, policy, 0.8, episodeAnalyser);

        RunAnalyser runAnalyser = new RunAnalyser();

        for (int i = 0; i < episodeNum; i++) {
            P2DMEpisodeAnalysis ea = agent.runLearningEpisode(env, 365, i);
            runAnalyser.recordEpisodeReward(ea);
            env.resetEnvironment();
        }

        Map<String, Object> visualizationMetadata = prepareVisualizationMetadata("Sarsa-Lambda", policy, "Total Rewards", "Reward", "Episode");
        runAnalyser.drawRewardCharts(visualizerClasses, visualizationMetadata);
    }

    private Map<String, Object> prepareVisualizationMetadata(String learningAlgorithm, Policy policy, String windowTitle, String xLabel, String yLabel) {
        Map<String, Object> visualizationMetadata = new HashMap<String, Object>();
        visualizationMetadata.put(Visualizer.METADATA_LEARNING_ALGORITHM, learningAlgorithm);
        visualizationMetadata.put(Visualizer.METADATA_POLICY, policy);
        visualizationMetadata.put(Visualizer.METADATA_WINDOW_TITLE, windowTitle);
        visualizationMetadata.put(Visualizer.METADATA_X_LABEL, xLabel);
        visualizationMetadata.put(Visualizer.METADATA_Y_LABEL, yLabel);
        return visualizationMetadata;
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
