package power2dm;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import power2dm.model.DailyTerminalFunction;
import power2dm.model.P2DMDomain;
import power2dm.model.P2DMQLearning;
import power2dm.model.burden.BurdenDailyRewardFunction;
import power2dm.model.burden.BurdenP2DMDomain;
import power2dm.reporting.RewardVisualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 08-Apr-16.
 */
public class AlgorithmComparator {

    private P2DMDomain domain;
    private HashableStateFactory hashingFactory;
    private Environment env;

    private Map<String, List<Double>> totalRewardsPerPolicy;

    public static void main(String[] args) {
        AlgorithmComparator example = new AlgorithmComparator();
        String outputPath = "output/"; //directory to record results

        //run example
//        example.QLearningExample(new GreedyQPolicy(), outputPath);
        example.QLearningExample(new EpsilonGreedy(0.01), outputPath);
//        example.sarsaExample(outputPath);
        //visualize total rewards
        example.drawRewardChards();
    }

    public AlgorithmComparator() {
        domain = new BurdenP2DMDomain();
        RewardFunction rf = new BurdenDailyRewardFunction();
        TerminalFunction tf = new DailyTerminalFunction();

        State initialState = domain.getInitialState();
        HashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        env = new SimulatedEnvironment(domain, rf, tf, initialState);

        totalRewardsPerPolicy = new HashMap<String, List<Double>>();

    }

    public void QLearningExample(Policy policy, String outputPath) {
        int totalInterventionDelieveyInPreferredRange = 0;

        List<Double> totalRewards = new ArrayList<Double>();
        int totalDaysOfinterventionDeliveredInPreferredRange = 0;
        //run learning for 50 episodes
        P2DMQLearning agent = new P2DMQLearning(domain, 0.5, hashingFactory, 0., 0.1);
        agent.setLearningPolicy(policy);
        ((SolverDerivedPolicy) policy).setSolver(agent);

        for (int i = 0; i < 50000; i++) {
            EpisodeAnalysis ea = agent.runLearningEpisode(env, 100, i);

//            ea.writeToFile(outputPath + "ql_" + i);
            if (isInterventionDeliveredInPrefferedRange(i, ea, agent)) {
                totalDaysOfinterventionDeliveredInPreferredRange++;
            }

            // record total reward
            totalRewards.add(calculateTotalReward(ea));

            //reset environment for next learning episode
            env.resetEnvironment();
        }
        totalRewardsPerPolicy.put(policy.getClass().getName(), totalRewards);
    }

    private void sarsaExample(String outputPath) {
        LearningAgent agent = new SarsaLam(domain, 0.99, hashingFactory, 0., 0.5, 0.3);

        // TODO ==> total reward'lar episode analyser veya daha high-level bi şeyin altına taşınacak
        List<Double> totalRewards = new ArrayList<Double>();

        for (int i = 0; i < 100000; i++) {
            EpisodeAnalysis ea = agent.runLearningEpisode(env);

            // record total reward
            totalRewards.add(calculateTotalReward(ea));

            //reset environment for next learning episode
            env.resetEnvironment();
        }

        totalRewardsPerPolicy.put("Epsilon Greedy", totalRewards);
    }

    private void drawRewardChards() {
        RewardVisualizer.createRewardGraph("Total rewards per episode", "Greedy rewards", totalRewardsPerPolicy);
    }

    private boolean isInterventionDeliveredInPrefferedRange(int episode, EpisodeAnalysis ea, LearningAgent agent) {
        boolean interventionDeliveredInPreferredRange = false;

        for (int a = 0; a < ea.actionSequence.size(); a++) {
            if (a >= 19 && a <= 21) {
                if (ea.getAction(a).actionName().equals(BurdenP2DMDomain.ACTION_INT_DELIVERY)) {
                    interventionDeliveredInPreferredRange = true;
                }
            }
        }
        return interventionDeliveredInPreferredRange;
    }

    private double calculateTotalReward(EpisodeAnalysis ea) {
        double totalReward = 0;
        for (double reward : ea.rewardSequence) {
            totalReward += reward;
        }
        return totalReward;
    }
}
