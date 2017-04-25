package tez.experiment;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import tez.algorithm.DayTerminalFunction;
import tez.algorithm.ReactionRewardFunction;
import tez.algorithm.SelfManagementDomainGenerator;
import tez.simulator.RealWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/22/2016.
 */
public class Experiment {

    private Experiment experiment;
    private Environment environment;

    public Experiment getInstance() {
        if (experiment == null) {
            experiment = new Experiment();
        }
        return this;
    }

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
        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(environment,
                10, 100, learningCases);

        exp.setUpPlottingConfiguration(750, 500, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE,
                PerformanceMetric.CUMULATIVESTEPSPEREPISODE,
                PerformanceMetric.CUMULATIVEREWARDPERSTEP,
                PerformanceMetric.CUMULTAIVEREWARDPEREPISODE,
                PerformanceMetric.AVERAGEEPISODEREWARD,
                PerformanceMetric.STEPSPEREPISODE,
                PerformanceMetric.MEDIANEPISODEREWARD);


        //start experiment
        exp.startExperiment();
    }

    private LearningAgentFactory[] getLearningAlternatives(Domain domain) {
        List<LearningAgentFactory> learningAlternatives = new ArrayList<>();
        SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
            public String getAgentName() {
                return "Q-learning";
            }

            public LearningAgent generateAgent() {
                return new QLearning(domain, 0.99, hashingFactory, 0.3, 0.1);
            }
        };

        learningAlternatives.add(qLearningFactory);
        return learningAlternatives.toArray(new LearningAgentFactory[0]);
    }
}
