package power2dm.reporting;

import burlap.behavior.policy.Policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 28-Apr-16.
 */
public class RunAnalyser {
    private Map<Policy, List<P2DMEpisodeAnalysis>> episodeAnalysisPerPolicy = new HashMap<Policy, List<P2DMEpisodeAnalysis>>();

    public void recordEpisodeReward(Policy policy, P2DMEpisodeAnalysis ea) {
        List<P2DMEpisodeAnalysis> totalRewardsPerEpisode = episodeAnalysisPerPolicy.get(policy);
        if(totalRewardsPerEpisode == null) {
            totalRewardsPerEpisode = new ArrayList<P2DMEpisodeAnalysis>();
            episodeAnalysisPerPolicy.put(policy, totalRewardsPerEpisode);
        }
        totalRewardsPerEpisode.add(ea);
    }

    public void drawRewardCharts(List<Class> visualizationTypes, String applicationTitle, Policy policy) {
        RewardVisualizer rewardVisualizer = new RewardVisualizer(applicationTitle, policy, episodeAnalysisPerPolicy);
        rewardVisualizer.createRewardGraph();
    }
}
