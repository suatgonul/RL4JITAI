package tez.experiment.real;

import breeze.optimize.linear.LinearProgram;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import tez.algorithm.SelfManagementEligibilitySarsaLam;
import tez.algorithm.SelfManagementGreedyQPolicy;
import tez.algorithm.collaborative_learning.SparkStateClassifier;
import tez.domain.DayTerminalFunction;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.SelfManagementRewardFunction;
import tez.environment.real.RealWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 19-Jun-17.
 */
public class RealExperimentManager {
    private static RealExperimentManager instance;
    private Map<String, Environment> environments;

    private RealExperimentManager() {

    }

    public static RealExperimentManager getInstance() {
        if (instance == null) {
            instance = new RealExperimentManager();
        }
        return instance;
    }

    public boolean experiementStarted(String deviceIdentifier) {
        return environments.get(deviceIdentifier) != null;
    }

    public void runExperiment(String deviceIdentifier) {

        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new SelfManagementRewardFunction();

        RealWorld environment = new RealWorld(domain, rf, tf, 1, 1000, deviceIdentifier);
        smdg.setEnvironment(environment);
        environments.put(deviceIdentifier, environment);

        SparkStateClassifier sparkClassifier = SparkStateClassifier.getInstance();
        sparkClassifier.setDomain(domain);

        LearningAgentFactory[] learningCases = getLearningAlternatives(domain);

        RealExperimenter exp = new RealExperimenter(environment,
                10, 10000, deviceIdentifier, learningCases);

        //start experiment
        exp.startExperiment();
    }

    private LearningAgentFactory[] getLearningAlternatives(final Domain domain) {
        List<LearningAgentFactory> learningAlternatives = new ArrayList<>();
        final SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
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
        return learningAlternatives.toArray(new LearningAgentFactory[0]);
    }
}
