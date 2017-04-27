package tez.experiment;

import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import tez.algorithm.DayTerminalFunction;
import tez.algorithm.ReactionRewardFunction;
import tez.algorithm.SelfManagementDomainGenerator;
import tez.algorithm.SelfManagementQLearning;
import tez.experiment.performance.SelfManagementPerformanceMetric;
import tez.simulator.RealWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/22/2016.
 */
public class Experiment {

    private Environment environment;

    public static void main(String[] args) {
        Experiment exp = new Experiment();
        exp.runExperiment();
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    private void runExperiment() {

        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new ReactionRewardFunction();
        SelfManagementDomainGenerator domGen = new SelfManagementDomainGenerator();
        Domain domain = domGen.generateDomain();
        environment = new RealWorld(domain, rf, tf, "D:\\personalCodes\\tez\\RLTrials\\src\\main\\resources\\persona\\officejob", 60);
        domGen.setEnvironment(environment);

        LearningAgentFactory[] learningCases = getLearningAlternatives(domain);
        SelfManagementExperimenter exp = new SelfManagementExperimenter(environment,
                1, 1000, learningCases);

        exp.setUpPlottingConfiguration(750, 500, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
                //PerformanceMetric.CUMULATIVESTEPSPEREPISODE,
                //PerformanceMetric.CUMULATIVEREWARDPERSTEP,
                //PerformanceMetric.CUMULTAIVEREWARDPEREPISODE,
                //SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD,
                SelfManagementPerformanceMetric.STEPSPEREPISODE,
                //PerformanceMetric.MEDIANEPISODEREWARD,
                SelfManagementPerformanceMetric.REWARD_PER_EPISODE,
                SelfManagementPerformanceMetric.USER_REACTION_PER_EPISODE
        );


        //start experiment
        exp.startExperiment();
    }

    private LearningAgentFactory[] getLearningAlternatives(final Domain domain) {
        List<LearningAgentFactory> learningAlternatives = new ArrayList<>();
        final SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
            public String getAgentName() {
                return "Q-learning";
            }

            public LearningAgent generateAgent() {
                return new SelfManagementQLearning(domain, 0.9, hashingFactory, 0.3, 0.1);
            }
        };

        learningAlternatives.add(qLearningFactory);
        return learningAlternatives.toArray(new LearningAgentFactory[0]);
    }
}
