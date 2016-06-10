package power2dm.reporting.visualization;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import power2dm.reporting.P2DMEpisodeAnalysis;

import java.util.List;
import java.util.Map;

/**
 * Created by suatgonul on 4/15/2016.
 */
public class RewardVisualizer extends Visualizer {
    private static final String TOTAL_REWARD = "Total Reward";

    public RewardVisualizer(Map<String, Object> visualizerMetadata) {
        super(visualizerMetadata);
        visualizerMetadata.put(METADATA_X_LABEL, "Episode");
        visualizerMetadata.put(METADATA_Y_LABEL, "Reward");
    }

    @Override
    public VisualizationData createDataset(List<P2DMEpisodeAnalysis> episodeAnalysisList) {
        XYSeriesCollection dataSet = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        VisualizationData visualizationData = new VisualizationData(dataSet, renderer);

        XYSeries rewardSeries = new XYSeries(TOTAL_REWARD);
        for (int i = 0; i < episodeAnalysisList.size(); i++) {
            rewardSeries.add(i, episodeAnalysisList.get(i).getTotalReward());
        }

        dataSet.addSeries(rewardSeries);

        renderer.setSeriesShapesVisible(0, false);
        return visualizationData;
    }

    protected String getChartTitle() {
        Policy policy = (Policy) visualizerMetadata.get(METADATA_POLICY);
        if (policy instanceof EpsilonGreedy) {
            double epsilon = ((EpsilonGreedy) policy).getEpsilon();
            return "Epsilon Greedy (" + epsilon + ")";
        }
        return "";
    }
}
