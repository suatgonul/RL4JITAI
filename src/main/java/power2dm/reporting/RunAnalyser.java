package power2dm.reporting;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 28-Apr-16.
 */
public class RunAnalyser {
    private Map<Policy, List<Double>> totalRewardsPerPolicy = new HashMap<Policy, List<Double>>();

    public void recordEpisodeReward(Policy policy, EpisodeAnalysis ea) {
        List<Double> totalRewardsPerEpisode = totalRewardsPerPolicy.get(policy);
        if(totalRewardsPerEpisode == null) {
            totalRewardsPerEpisode = new ArrayList<Double>();
            totalRewardsPerPolicy.put(policy, totalRewardsPerEpisode);
        }
        totalRewardsPerEpisode.add(calculateTotalReward(ea));
    }

    private double calculateTotalReward(EpisodeAnalysis ea) {
        double totalReward = 0;
        for (double reward : ea.rewardSequence) {
            totalReward += reward;
        }
        return totalReward;
    }

    public void drawRewardChards() {
        RewardVisualizer.createRewardGraph("Total rewards per episode", "Greedy rewards", totalRewardsPerPolicy);
    }
}
