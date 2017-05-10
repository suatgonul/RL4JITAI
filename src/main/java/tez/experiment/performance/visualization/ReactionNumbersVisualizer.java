package tez.experiment.performance.visualization;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import power2dm.reporting.visualization.VisualizationData;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.List;
import java.util.Map;

/**
 * Created by suat on 06-Jun-16.
 */
public class ReactionNumbersVisualizer extends Visualizer {

    private static final String TOTAL_REACTION_POTENTIAL = "Total Reaction Potential";
    private static final String TOTAL_REACTION = "Total Reaction";

    private static final String CHART_TITLE = "";
    private static final String X_LABEL = "Episode";
    private static final String Y_LABEL = "";

    public ReactionNumbersVisualizer(Map<String, Object> visualizationMetadata) {
        super(visualizationMetadata);
        visualizationMetadata.put(Visualizer.METADATA_WINDOW_TITLE, "Reaction Numbers" + Visualizer.METADATA_LEARNING_ALGORITHM);
        visualizationMetadata.put(Visualizer.METADATA_X_LABEL, X_LABEL);
        visualizationMetadata.put(Visualizer.METADATA_Y_LABEL, Y_LABEL);
        this.setTitle((String) visualizationMetadata.get(METADATA_WINDOW_TITLE));
    }

    @Override
    public VisualizationData createDataset(List<SelfManagementEpisodeAnalysis> episodeAnalysisList) {
        XYSeriesCollection dataSet = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        VisualizationData visualizationData = new VisualizationData(dataSet, renderer);

        XYSeries cumulativeReactionNumberSeries = new XYSeries(TOTAL_REACTION_POTENTIAL);
        XYSeries cumulativeReactionHitSeries = new XYSeries(TOTAL_REACTION);

        int cumulativeReactionPotential = 0;
        int cumulativeReactionHit = 0;

        for (int i = 0; i < episodeAnalysisList.size(); i++) {
            cumulativeReactionPotential += episodeAnalysisList.get(i).phoneCheckNumber;
            cumulativeReactionHit += episodeAnalysisList.get(i).actionDeliveredDuringPhoneCheck;
            cumulativeReactionHitSeries.add(i, cumulativeReactionHit);
            cumulativeReactionNumberSeries.add(i, cumulativeReactionPotential);
        }

        dataSet.addSeries(cumulativeReactionNumberSeries);
        dataSet.addSeries(cumulativeReactionHitSeries);

        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesLinesVisible(1, true);

        return visualizationData;
    }

    @Override
    protected String getChartTitle() {
        return CHART_TITLE;
    }
}
