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
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.SelfManagementRewardFunction;
import tez.algorithm.SelfManagementEligibilitySarsaLam;
import tez.algorithm.SelfManagementSarsa;
import tez.simulator.RealWorld;

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
        environment = new RealWorld(domain, rf, tf, "D:\\personalCodes\\tez\\RLTrials\\src\\main\\resources\\persona\\officejob", 60);
        domGen.setEnvironment(environment);

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
                2, 10000, learningCases);

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
                return "Sarsa-Elig-Lam  Lambda_0.8 Gamma_0.1 LR_ 0.1";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementEligibilitySarsaLam(domain, 0.1, hashingFactory, 0, 0.1, new GreedyQPolicy(), Integer.MAX_VALUE, 0.8);
            }
        };
        learningAlternatives.add(qLearningFactory);

        qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Elig-Lam  Lambda_0.8 Gamma_0.1 LR_1.0";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementEligibilitySarsaLam(domain, 0.1, hashingFactory, 0, 1.0, new GreedyQPolicy(), Integer.MAX_VALUE, 0.8);
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
        learningAlternatives.add(qLearningFactory);

        return learningAlternatives.toArray(new LearningAgentFactory[0]);
    }
}
