package tez.experiment.real;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import org.apache.log4j.Logger;
import tez.algorithm.SelfManagementEligibilitySarsaLam;
import tez.algorithm.SelfManagementGreedyQPolicy;
import tez.algorithm.SelfManagementNoActionFavoringQPolicy;
import tez.algorithm.SelfManagementSarsa;
import tez.algorithm.collaborative_learning.SparkStateClassifier;
import tez.domain.DayTerminalFunction;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.SelfManagementRewardFunction;
import tez.environment.real.RealWorld;
import tez.environment.real.UserRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tez.util.LogUtil.log_info;

/**
 * Created by suat on 19-Jun-17.
 */
public class RealExperimentManager {
    private static RealExperimentManager instance;
    private static final Logger log = Logger.getLogger(RealExperimentManager.class);

    private Map<String, Environment> environments = new HashMap<>();
    private Map<String, Boolean> experiments = new HashMap<>();

    private RealExperimentManager() {

    }

    public static RealExperimentManager getInstance() {
        if (instance == null) {
            instance = new RealExperimentManager();
        }
        return instance;
    }

    private boolean experiementStarted(String deviceIdentifier) {
        Boolean started = experiments.get(deviceIdentifier);
        if(started == null) {
            return false;
        }
        return started;
    }

    public void runExperiment(String deviceIdentifier) {
        boolean settingsConfigured = UserRegistry.getInstance().isSettingsConfigured(deviceIdentifier);
        if(settingsConfigured) {
            boolean isControl = UserRegistry.getInstance().getUser(deviceIdentifier).isControl();
            if(!isControl && !experiementStarted(deviceIdentifier)) {
                log_info(log, deviceIdentifier, "RL experiment will start");
                runRLExperiment(deviceIdentifier);
            } else {
                log_info(log, deviceIdentifier,"Control experiment will start");
            }
            experiments.put(deviceIdentifier, true);
        } else {
            log_info(log, deviceIdentifier,"Settings are not configured" );
        }
    }

    private void runRLExperiment(String deviceIdentifier) {
        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new SelfManagementRewardFunction();

        RealWorld environment = new RealWorld(domain, rf, tf, 1, deviceIdentifier);
        smdg.setEnvironment(environment);
        environments.put(deviceIdentifier, environment);

        SparkStateClassifier sparkClassifier = SparkStateClassifier.getInstance();
        sparkClassifier.setDomain(domain);

        LearningAgentFactory[] learningCases = getLearningAlternatives(domain, deviceIdentifier);

        RealExperimenter exp = new RealExperimenter(environment,
                10, 10000, deviceIdentifier, learningCases);

        //start experiment
        exp.startExperiment();
    }

    private LearningAgentFactory[] getLearningAlternatives(final Domain domain, String deviceIdentifier) {
        List<LearningAgentFactory> learningAlternatives = new ArrayList<>();
        final SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "Sarsa-Elig-Lam  Lambda_0.8 Gamma_0.1 LR_0.1 collaborative";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SelfManagementSarsa(domain, 0.1, hashingFactory, 0, 0.1, new SelfManagementNoActionFavoringQPolicy(deviceIdentifier), Integer.MAX_VALUE, 0.8);
            }
        };
        learningAlternatives.add(qLearningFactory);
        return learningAlternatives.toArray(new LearningAgentFactory[0]);
    }
}
