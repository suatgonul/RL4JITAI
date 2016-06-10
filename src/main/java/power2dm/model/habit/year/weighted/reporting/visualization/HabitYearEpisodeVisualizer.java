package power2dm.model.habit.year.weighted.reporting.visualization;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import power2dm.model.habit.year.weighted.HabitYearP2DMDomain;
import power2dm.model.habit.year.weighted.reporting.HabitYearEpisodeAnalysis;
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
public class HabitYearEpisodeVisualizer extends Visualizer {
    private static final String HABIT_GAIN = "Habit Gain";
    private static final String INTERVENTION = "Intervention";

    public HabitYearEpisodeVisualizer(Map<String, Object> visualizerMetadata) {
        super(visualizerMetadata);
        visualizerMetadata.put(METADATA_X_LABEL, "Days");
        visualizerMetadata.put(METADATA_Y_LABEL, "");
    }

    @Override
    public VisualizationData createDataset(List<P2DMEpisodeAnalysis> episodeAnalysisList) {
        XYSeriesCollection dataSet = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        VisualizationData visualizationData = new VisualizationData(dataSet, renderer);

        List<XYSeries> generatedData = new ArrayList<XYSeries>();
        for (int i = 0; i < episodeAnalysisList.size(); i++) {
            if (i % 250 == 0) {
                HabitYearEpisodeAnalysis hyea = (HabitYearEpisodeAnalysis) episodeAnalysisList.get(i);

                XYSeries habitGainSeries = new XYSeries(HABIT_GAIN + i);
                XYSeries interventionSeries = new XYSeries(INTERVENTION + i);
                for (int j = 0; j < hyea.numTimeSteps(); j++) {
                    habitGainSeries.add(j, hyea.getHabitGainList().get(j));
                    if (hyea.actionSequence.get(j).actionName().equals(HabitYearP2DMDomain.ACTION_INT_DELIVERY)) {
                        interventionSeries.add(j, hyea.getHabitGainList().get(j));
                    }
                }

                generatedData.add(habitGainSeries);
                generatedData.add(interventionSeries);
            }
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
        return "Habit / Intervention";
    }
}
