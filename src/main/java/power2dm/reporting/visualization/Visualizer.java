package power2dm.reporting.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import power2dm.reporting.P2DMEpisodeAnalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 16-May-16.
 */
public abstract class Visualizer extends ApplicationFrame {
    public static final String METADATA_WINDOW_TITLE = "metadata_window_title";
    public static final String METADATA_X_LABEL = "xLabel";
    public static final String METADATA_Y_LABEL = "yLabel";

    public static final String METADATA_POLICY = "metadata_policy";
    public static final String METADATA_LEARNING_ALGORITHM = "metadata_learning_algorithm";

    protected Map<String, Object> visualizationMetadata = new HashMap<String, Object>();

    public Visualizer(Map<String, Object> visualizationMetadata) {
        super((String) visualizationMetadata.get(METADATA_WINDOW_TITLE));
        this.visualizationMetadata = visualizationMetadata;
    }

    public void createRewardGraph(List<P2DMEpisodeAnalysis> episodeAnalysisList) {
        VisualizationData visualizationData = createDataset(episodeAnalysisList);

        String xLabel = (String) visualizationMetadata.get(METADATA_X_LABEL);
        String yLabel = (String) visualizationMetadata.get(METADATA_Y_LABEL);

        JFreeChart xylineChart = ChartFactory.createXYLineChart(getChartTitle(), xLabel, yLabel, visualizationData.getDataset(), PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(750, 375));

        XYPlot plot = xylineChart.getXYPlot();
        plot.setRenderer(visualizationData.getRenderer());

        setContentPane(chartPanel);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    public abstract VisualizationData createDataset(List<P2DMEpisodeAnalysis> episodeAnalysisList);

    protected abstract String getChartTitle();
}
