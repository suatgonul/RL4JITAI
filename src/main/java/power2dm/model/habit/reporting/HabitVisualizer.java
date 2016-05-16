package power2dm.model.habit.reporting;

import burlap.behavior.policy.Policy;
import power2dm.reporting.P2DMEpisodeAnalysis;
import power2dm.reporting.RewardVisualizer;

import java.util.List;
import java.util.Map;

/**
 * Created by suat on 16-May-16.
 */
public class HabitVisualizer extends RewardVisualizer {

    public HabitVisualizer(String applicationTitle, Policy policy, Map<Policy, List<P2DMEpisodeAnalysis>> rewards) {
        super(applicationTitle, policy, rewards);
    }


}
