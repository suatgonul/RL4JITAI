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
public class ReactionHitRatioVisualizer extends Visualizer {

    private static final String HIT_REACTION_RATIO = "Hit Reaction";

    private static final String CHART_TITLE = "";
    private static final String X_LABEL = "Episode";
    private static final String Y_LABEL = "";

    public ReactionHitRatioVisualizer(Map<String, Object> visualizationMetadata) {
        super(visualizationMetadata);
        visualizationMetadata.put(Visualizer.METADATA_WINDOW_TITLE, "Hit Ratio" + Visualizer.METADATA_LEARNING_ALGORITHM);
        visualizationMetadata.put(Visualizer.METADATA_X_LABEL, X_LABEL);
        visualizationMetadata.put(Visualizer.METADATA_Y_LABEL, Y_LABEL);
        this.setTitle((String) visualizationMetadata.get(Visualizer.METADATA_LEARNING_ALGORITHM));
    }

    @Override
    public VisualizationData createDataset(List<SelfManagementEpisodeAnalysis> episodeAnalysisList) {
        XYSeriesCollection dataSet = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        VisualizationData visualizationData = new VisualizationData(dataSet, renderer);

        XYSeries reactionHitRatioSeries = new XYSeries(HIT_REACTION_RATIO);


        for (int i = 0; i < episodeAnalysisList.size(); i++) {
            reactionHitRatioSeries.add(i, 100 * (double) episodeAnalysisList.get(i).actionDeliveredDuringPhoneCheck / (double) episodeAnalysisList.get(i).phoneCheckNumber);
            //cumulativeReactionNumberSeries.add(i, episodeAnalysisList.get(i).actionDeliveredDuringPhoneCheck);
        }

        dataSet.addSeries(reactionHitRatioSeries);
        //dataSet.addSeries(cumulativeReactionNumberSeries);

        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(0, true);
        /*renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesLinesVisible(1, true);*/

        return visualizationData;
    }

    @Override
    protected String getChartTitle() {
        return CHART_TITLE;
    }
}
