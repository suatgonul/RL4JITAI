package tez.experiment;

import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import tez.algorithm.DayTerminalFunction;
import tez.algorithm.ReactionRewardFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/22/2016.
 */
public class Experiment {

    private void runExperiment() {
        SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new ReactionRewardFunction();
        Domain domain = new SADomain();
        State initialState = domain.getInitialState();
        Environment env = new SimulatedEnvironment(domain, rf, tf);

        LearningAgentFactory[] learningCases = getLearningAlternatives();
        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env,
                10, 100, learningCases);

        exp.setUpPlottingConfiguration(500, 250, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
                PerformanceMetric.CUMULATIVESTEPSPEREPISODE,
                PerformanceMetric.AVERAGEEPISODEREWARD);


        //start experiment
        exp.startExperiment();
    }

    private LearningAgentFactory[] getLearningAlternatives() {
        List<LearningAgentFactory> learningAlternatives= new ArrayList<>();
        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
            public String getAgentName() {
                return "Q-learning";
            }
            public LearningAgent generateAgent() {
                return new QLearning(domain, 0.99, hashingFactory, 0.3, 0.1);
            }
        };

        learningAlternatives.add(qLearningFactory);
        return (LearningAgentFactory[]) learningAlternatives.toArray();
    }
}
