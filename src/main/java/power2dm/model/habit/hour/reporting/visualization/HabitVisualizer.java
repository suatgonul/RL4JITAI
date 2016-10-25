package power2dm.model.habit.hour.reporting.visualization;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import power2dm.model.habit.hour.reporting.HabitEpisodeAnalysis;
import power2dm.reporting.P2DMEpisodeAnalysis;
import power2dm.reporting.visualization.VisualizationData;
import power2dm.reporting.visualization.Visualizer;

import java.util.List;
import java.util.Map;

/**
 * Created by suat on 16-May-16.
 */
public class HabitVisualizer extends Visualizer {

    private static final String HABIT = "Habit";
    private static final String INTERVENTION = "Intervention";
    private static final String CALORIE_INTAKE = "Calorie Intake";
    private static final String ENTRY_PROBABILITY = "Entry Probability";
    private static final String GAIN_OFFSET = "Gain Offset";

    private static final String WINDOW_TITLE = "Habit / Interventiom / Calorie Intake / Entry " +
            "Probability / Gain Offset change during the learning process";
    private static final String CHART_TITLE = "";
    private static final String X_LABEL = "Episode";
    private static final String Y_LABEL = "";

    public HabitVisualizer(Map<String, Object> visualizationMetadata) {
        super(visualizationMetadata);
        visualizationMetadata.put(Visualizer.METADATA_WINDOW_TITLE, WINDOW_TITLE);
        visualizationMetadata.put(Visualizer.METADATA_X_LABEL, X_LABEL);
        visualizationMetadata.put(Visualizer.METADATA_Y_LABEL, Y_LABEL);
        this.setTitle((String) visualizationMetadata.get(METADATA_WINDOW_TITLE));
    }

    @Override
    public VisualizationData createDataset(List<P2DMEpisodeAnalysis> episodeAnalysisList) {
        XYSeriesCollection dataSet = new XYSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        VisualizationData visualizationData = new VisualizationData(dataSet, renderer);

        XYSeries habitSeries = new XYSeries(HABIT);
        XYSeries interventionSeries = new XYSeries(INTERVENTION);
        XYSeries calorieIntakeEntrySeries = new XYSeries(CALORIE_INTAKE);
        XYSeries entryProbabilitySeries = new XYSeries(ENTRY_PROBABILITY);
        XYSeries gainOffsetSeries = new XYSeries(GAIN_OFFSET);

        for (int i = 0; i < episodeAnalysisList.size(); i++) {
            habitSeries.add(i, ((HabitEpisodeAnalysis) episodeAnalysisList.get(i)).isHabitActive() == true ? 20 : 0);
            interventionSeries.add(i, ((HabitEpisodeAnalysis) episodeAnalysisList.get(i)).isInterventionDelivered() == true ? 20 : 0);
            calorieIntakeEntrySeries.add(i, ((HabitEpisodeAnalysis) episodeAnalysisList.get(i)).isCalorieIntakeEntered() == true ? 20 : 0);
            entryProbabilitySeries.add(i, ((HabitEpisodeAnalysis) episodeAnalysisList.get(i)).getEntryProbabilityWhenInterventionDelivery()*20);
            gainOffsetSeries.add(i, ((HabitEpisodeAnalysis) episodeAnalysisList.get(i)).getHabitGainOffset());
        }

        dataSet.addSeries(habitSeries);
        dataSet.addSeries(interventionSeries);
        dataSet.addSeries(calorieIntakeEntrySeries);
        dataSet.addSeries(entryProbabilitySeries);
        dataSet.addSeries(gainOffsetSeries);

        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(2, true);
        renderer.setSeriesLinesVisible(2, false);
        renderer.setSeriesShapesVisible(3, true);
        renderer.setSeriesLinesVisible(3, false);
        renderer.setSeriesShapesVisible(4, true);
        renderer.setSeriesLinesVisible(4, false);

        return visualizationData;
    }

    @Override
    protected String getChartTitle() {
        return CHART_TITLE;
    }
}
