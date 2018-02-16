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
import tez2.algorithm.collaborative_learning.SparkStateClassifier;
import tez2.algorithm.jitai_selection.JsQLearning;
import tez2.domain.DayTerminalFunction;
import tez2.domain.js.JsDomainGenerator;
import tez2.domain.js.JsRewardFunction;
import tez2.domain.omi.OmiDomainGenerator;
import tez2.domain.omi.OmiRewardFunction;
import tez2.environment.simulator.JsEnvironment;
import tez2.environment.simulator.SimulatedWorld;
import tez2.experiment.performance.StaticSelfManagementRewardPlotter;
import tez2.experiment.performance.visualization.EpisodeJitaiCountHabitStrengthPlotter;
import tez2.experiment.performance.visualization.JitaiTypeCountPlotter;
import tez2.persona.PersonaConfig;

import java.util.ArrayList;
import java.util.List;

import static tez2.experiment.performance.SelfManagementPerformanceMetric.AVG_TOTAL_JITAIS_PER_EPISODE;
import static tez2.experiment.performance.SelfManagementPerformanceMetric.TOTAL_NUMBER_OF_JITAI_TYPES;

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
        //String personaFolder = "D:\\personalCodes\\tez\\RLTrials\\src\\main\\resources\\persona\\officejob";

        List<PersonaConfig> configs = PersonaConfig.getConfigs(personaFolder);

        // jitai selection related objects
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new JsRewardFunction();
        JsDomainGenerator domGen = new JsDomainGenerator(null);
        Domain jsDomain = domGen.generateDomain();
        JsEnvironment jitaiSelectionEnvironment = new JsEnvironment(jsDomain, rf, tf, 60);
        domGen.setEnvironment(jitaiSelectionEnvironment);
        final SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        // opportune moment identification related objects
        OmiDomainGenerator omiDomGen = new OmiDomainGenerator();
        Domain domain = omiDomGen.generateDomain();

        rf = new OmiRewardFunction();
        environment = new SimulatedWorld(domain, rf, tf, 60, personaFolder, jitaiSelectionEnvironment, getJsLearningAlternatives(jsDomain));
        //environment = new SimulatedWorld(domain, rf, tf, 60,"D:\\personalCodes\\tez\\RLTrials\\src\\main\\resources\\persona\\officejob");
        omiDomGen.setEnvironment(environment);

        jitaiSelectionEnvironment.setSimulatedWorld((SimulatedWorld) environment);

        LearningAgentFactory[] omiLearningCases = getOpportuneMomentIdentificationLearningAlternatives(domain);

        SparkStateClassifier sparkClassifier = SparkStateClassifier.getInstance();
        sparkClassifier.setDomain(domain);


//        exp.setUpPlottingConfiguration(750, 500, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
//                CUMULATIVE_REWARD_PER_EPISODE,
//                CUMULATIVE_REACTION,
//                AVERAGEEPISODEREWARD,
//                REWARD_PER_EPISODE,
//                USER_REACTION_PER_EPISODE
//        );

        //start experiment
        List<StaticSelfManagementRewardPlotter> experimentResultsForPersonas = new ArrayList<>();
        for(PersonaConfig config : configs) {
            StaticSelfManagementExperimenter exp = new StaticSelfManagementExperimenter(environment,
                    10, 10, omiLearningCases);
            ((SimulatedWorld) environment).setConfig(config);
            exp.setUpPlottingConfiguration(750, 500, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
                    //RATIO_JITAIS_PER_TIME_OF_DAY,
                    TOTAL_NUMBER_OF_JITAI_TYPES,
                    AVG_TOTAL_JITAIS_PER_EPISODE
            );
            exp.startExperiment();
            System.out.println(exp.plotter.allAgents_totalJitaisPerEpisode.getItemCount(0));
            experimentResultsForPersonas.add(exp.plotter);
        }

        EpisodeJitaiCountHabitStrengthPlotter plotter = new EpisodeJitaiCountHabitStrengthPlotter();
        plotter.drawEpisodeCountHabitStrengthChart(experimentResultsForPersonas);
        JitaiTypeCountPlotter jitaiTypeCountPlotter = new JitaiTypeCountPlotter();
        jitaiTypeCountPlotter.drawJitaiTypeRatios(experimentResultsForPersonas);
    }

    private LearningAgentFactory[] getJsLearningAlternatives(final Domain domain) {

        List<LearningAgentFactory> learningAlternatives = new ArrayList<>();
        final SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Lam  Lambda_0.8 Gamma_0.1 LR_0.1";
            }

            @Override
            public LearningAgent generateAgent() {
                return new JsQLearning(domain, 0.1, hashingFactory, 0, 0.1, new SelfManagementGreedyQPolicy(), Integer.MAX_VALUE);
            }
        };
        learningAlternatives.add(qLearningFactory);
        return learningAlternatives.toArray(new LearningAgentFactory[0]);
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
        //learningAlternatives.add(qLearningFactory);

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
