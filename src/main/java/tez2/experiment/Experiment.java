package tez2.experiment;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import tez2.algorithm.*;
import tez2.domain.DayTerminalFunction;
import tez2.domain.JitaiSelectionDomainGenerator;
import tez2.domain.OpportuneMomentDomainGenerator;
import tez2.domain.SelfManagementRewardFunction;
import tez2.environment.simulator.SimulatedWorld;
import tez2.environment.simulator.JitaiSelectionEnvironment;

import java.util.ArrayList;
import java.util.List;

import static tez2.experiment.performance.SelfManagementPerformanceMetric.*;

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
        String personaFolder = "D:\\mine\\odtu\\6\\tez\\codes\\RLTrials\\src\\main\\resources\\persona\\officejob";

        // jitai selection related objects
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new SelfManagementRewardFunction();
        JitaiSelectionDomainGenerator domGen = new JitaiSelectionDomainGenerator(null);
        Domain domain = domGen.generateDomain();
        JitaiSelectionEnvironment jitaiSelectionEnvironment = new JitaiSelectionEnvironment(domain, rf, tf, 60, personaFolder + "/config");

        LearningAgentFactory[] omiLearningCases = getOpportuneMomentIdentificationLearningAlternatives(domain);

        // opportune moment identification related objects
        OpportuneMomentDomainGenerator omiDomGen = new OpportuneMomentDomainGenerator();
        omiDomGen.generateDomain();
        environment = new SimulatedWorld(domain, rf, tf, 60, personaFolder);
        //environment = new SimulatedWorld(domain, rf, tf, 60,"D:\\personalCodes\\tez\\RLTrials\\src\\main\\resources\\persona\\officejob");
        omiDomGen.setEnvironment(environment);


        StaticSelfManagementExperimenter exp = new StaticSelfManagementExperimenter(environment,
                1, 100, omiLearningCases);

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

    private LearningAgentFactory[] getJitaiSelectionLearningAlternatives() {
        // TODO
        return null;
    }

    private LearningAgentFactory[] getOpportuneMomentIdentificationLearningAlternatives(final Domain domain) {
        List<LearningAgentFactory> learningAlternatives = new ArrayList<>();
        final SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Lam  Lambda_0.8 Gamma_0.1 LR_0.1";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementQLearning(domain, 0.1, hashingFactory, 0, 0.1, new SelfManagementGreedyQPolicy(), Integer.MAX_VALUE);
            }
        };
        learningAlternatives.add(qLearningFactory);

        qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Lam  Lambda_0.8 Gamma_0.1 LR_0.1";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementSarsaLam(domain, 0.1, hashingFactory, 0, 0.1, new SelfManagementGreedyQPolicy(), Integer.MAX_VALUE, 0.8);
            }
        };
        //learningAlternatives.add(qLearningFactory);

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
        //learningAlternatives.add(qLearningFactory);

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
        //learningAlternatives.add(qLearningFactory);

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
