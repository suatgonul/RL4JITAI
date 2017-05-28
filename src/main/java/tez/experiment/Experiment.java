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
import tez.algorithm.SelfManagementEligibilitySarsaLam;
import tez.algorithm.SelfManagementGreedyQPolicy;
import tez.algorithm.SelfManagementSarsa;
import tez.algorithm.SelfManagementSarsaLam;
import tez.algorithm.collaborative_learning.SparkStateClassifier;
import tez.domain.DayTerminalFunction;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.SelfManagementRewardFunction;
import tez.environment.real.RealWorld;
import tez.environment.simulator.SimulatedWorld;

import java.util.ArrayList;
import java.util.List;

import static tez.experiment.performance.SelfManagementPerformanceMetric.*;

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
        RewardFunction rf = new SelfManagementRewardFunction();
        SelfManagementDomainGenerator domGen = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = domGen.generateDomain();
        //environment = new SimulatedWorld(domain, rf, tf, "D:\\personalCodes\\tez\\RLTrials\\src\\main\\resources\\persona\\officejob", 60);
        environment = new SimulatedWorld(domain, rf, tf, 60,"D:\\mine\\odtu\\6\\tez\\codes\\RLTrials\\src\\main\\resources\\persona\\officejob");
        //environment = new RealWorld(domain, rf, tf, 1, 100);
        domGen.setEnvironment(environment);
        //H2OStateClassifier h2OStateClassifier = H2OStateClassifier.getInstance();
        //h2OStateClassifier.setDomain(domain);
        SparkStateClassifier sparkClassifier = SparkStateClassifier.getInstance();
        sparkClassifier.setDomain(domain);

        LearningAgentFactory[] learningCases = getLearningAlternatives(domain);
        /*SelfManagementExperimenter exp = new SelfManagementExperimenter(environment,
                2, 10000, learningCases);

        exp.setUpPlottingConfiguration(750, 500, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
                //SelfManagementPerformanceMetric.CUMULATIVESTEPSPEREPISODE,
                //SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP,
                SelfManagementPerformanceMetric.CUMULATIVE_REWARD_PER_EPISODE,
                //SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD,
                //SelfManagementPerformanceMetric.STEPSPEREPISODE,
                //PerformanceMetric.MEDIANEPISODEREWARD,
                SelfManagementPerformanceMetric.CUMULATIVE_REACTION,
                SelfManagementPerformanceMetric.REWARD_PER_EPISODE,

                SelfManagementPerformanceMetric.USER_REACTION_PER_EPISODE
        );


        //start experiment
        exp.startExperiment();*/
        StaticSelfManagementExperimenter exp = new StaticSelfManagementExperimenter(environment,
                10, 10000, learningCases);

        exp.setUpPlottingConfiguration(750, 500, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
                CUMULATIVE_REWARD_PER_EPISODE,
                CUMULATIVE_REACTION,
                AVERAGEEPISODEREWARD,
                REWARD_PER_EPISODE,
                USER_REACTION_PER_EPISODE
        );


        //start experiment
        exp.startExperiment();
    }

    private LearningAgentFactory[] getLearningAlternatives(final Domain domain) {
        List<LearningAgentFactory> learningAlternatives = new ArrayList<>();
        final SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Lam  Lambda_0.8 Gamma_0.1 LR_0.1";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementSarsaLam(domain, 0.1, hashingFactory, 0, 0.1, new SelfManagementGreedyQPolicy(), Integer.MAX_VALUE, 0.8);
            }
        };
        learningAlternatives.add(qLearningFactory);

        qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Elig-Lam  Lambda_0.8 Gamma_0.1 LR_0.1";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementEligibilitySarsaLam(domain, 0.1, hashingFactory, 0, 0.1, new SelfManagementGreedyQPolicy(), Integer.MAX_VALUE, 0.8, false);
            }
        };
        learningAlternatives.add(qLearningFactory);

        qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Elig-Lam  Lambda_0.8 Gamma_0.1 LR_0.1 collaborative";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementEligibilitySarsaLam(domain, 0.1, hashingFactory, 0, 0.1, new SelfManagementGreedyQPolicy(), Integer.MAX_VALUE, 0.8, true);
            }
        };
        learningAlternatives.add(qLearningFactory);

        qLearningFactory = new LearningAgentFactory() {
            public String getAgentName() {
                return "Sarsa  Gamma_0.1 LR_0.1";
            }

            public LearningAgent generateAgent() {
                return new SelfManagementSarsa(domain, 0.1, hashingFactory, 0, 0.1, new GreedyQPolicy(), Integer.MAX_VALUE, 0.8);
            }
        };
        //learningAlternatives.add(qLearningFactory);

        return learningAlternatives.toArray(new LearningAgentFactory[0]);
    }
}
