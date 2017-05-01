package tez.experiment;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import tez.domain.DayTerminalFunction;
import tez.domain.ReactionRewardFunction;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.algorithm.SelfManagementSarsa;
import tez.domain.algorithm.SelfManagementSarsaLam;
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
        SelfManagementDomainGenerator domGen = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = domGen.generateDomain();
        environment = new RealWorld(domain, rf, tf, "D:\\personalCodes\\tez\\RLTrials\\src\\main\\resources\\persona\\officejob", 60);
        domGen.setEnvironment(environment);

        LearningAgentFactory[] learningCases = getLearningAlternatives(domain);
        SelfManagementExperimenter exp = new SelfManagementExperimenter(environment,
                3, 5000, learningCases);

        exp.setUpPlottingConfiguration(750, 500, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
                //SelfManagementPerformanceMetric.CUMULATIVESTEPSPEREPISODE,
                //SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP,
                SelfManagementPerformanceMetric.CUMULTAIVEREWARDPEREPISODE,
                SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD,
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

        LearningAgentFactory         qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementSarsa(domain, 0.9, hashingFactory, 0, 0.1, new GreedyQPolicy(), Integer.MAX_VALUE, 0.8);
            }
        };
        learningAlternatives.add(qLearningFactory);

        /*qLearningFactory = new LearningAgentFactory() {
            public String getAgentName() {
                return "Q-learning";
            }

            public LearningAgent generateAgent() {
                return new SelfManagementQLearning(domain, 0.9, hashingFactory, 0, 0.1, new GreedyQPolicy(), Integer.MAX_VALUE);
            }
        };
        learningAlternatives.add(qLearningFactory);*/

        qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Lam";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementSarsaLam(domain, 0.9, hashingFactory, 0, 0.1, new GreedyQPolicy(), Integer.MAX_VALUE, 0);
            }
        };
        learningAlternatives.add(qLearningFactory);

        return learningAlternatives.toArray(new LearningAgentFactory[0]);
    }
}
