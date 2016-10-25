package power2dm.model.habit.year.periodic.reporting.visualization;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain;
import power2dm.model.habit.year.periodic.reporting.HabitYearPeriodicEpisodeAnalysis;
import power2dm.reporting.P2DMEpisodeAnalysis;
import power2dm.reporting.visualization.VisualizationData;
import power2dm.reporting.visualization.Visualizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by suat on 06-Jun-16.
 */
public class HabitYearPeriodicEpisodeVisualizer extends Visualizer {
    private static final String LEGEND_INTERVENTION = "Intervention";
    private static final String WINDOW_TITLE = "Habit formation trend vs intervention delivery per episode";
    private static final String CHART_TITLE = "";
    private static final String X_LABEL = "Days";
    private static final String Y_LABEL = "Habit and intervention";

    public HabitYearPeriodicEpisodeVisualizer(Map<String, Object> visualizationMetadata) {
        super(visualizationMetadata);
        visualizationMetadata.put(METADATA_WINDOW_TITLE, WINDOW_TITLE);
        visualizationMetadata.put(METADATA_X_LABEL, X_LABEL);
        visualizationMetadata.put(METADATA_Y_LABEL, Y_LABEL);
        this.setTitle((String) visualizationMetadata.get(METADATA_WINDOW_TITLE));
    }

    @Override
    public VisualizationData createDataset(List<P2DMEpisodeAnalysis> episodeAnalysisList) {
        XYSeriesCollection dataSet = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        VisualizationData visualizationData = new VisualizationData(dataSet, renderer);

        List<XYSeries> generatedData = new ArrayList<XYSeries>();
        for (int i = 0; i < episodeAnalysisList.size(); i++) {
            HabitYearPeriodicEpisodeAnalysis hyea = (HabitYearPeriodicEpisodeAnalysis) episodeAnalysisList.get(i);

            XYSeries habitGainSeries = new XYSeries(" Task " + i + ": " + hyea.getTaskDifficulty().toString());
            XYSeries interventionSeries = new XYSeries(LEGEND_INTERVENTION + " " + i);

            for (int j = 0; j < hyea.numTimeSteps(); j++) {
                habitGainSeries.add(j, hyea.getHabitGainList().get(j));
                if (hyea.actionSequence.get(j).actionName().equals(HabitYearPeriodicP2DMDomain.ACTION_INT_DELIVERY)) {
                    interventionSeries.add(j, hyea.getHabitGainList().get(j));
                }
            }

            generatedData.add(habitGainSeries);
            generatedData.add(interventionSeries);
        }

        for (int i = generatedData.size() - 2; i >= 0; i -= 2) {
            dataSet.addSeries(generatedData.get(i));
            dataSet.addSeries(generatedData.get(i + 1));

            Random rnd = new Random();
            int r = rnd.nextInt((255 - 0) + 1) + 0;
            int g = rnd.nextInt((255 - 0) + 1) + 0;
            int b = rnd.nextInt((255 - 0) + 1) + 0;

            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesPaint(i, new Color(r, g, b));
            renderer.setSeriesShapesVisible(i + 1, true);
            renderer.setSeriesLinesVisible(i + 1, false);
            renderer.setSeriesPaint(i + 1, new Color(r, g, b));
        }

        return visualizationData;
    }

    @Override
    protected String getChartTitle() {
        return CHART_TITLE;
    }
}
