package power2dm.reporting;

import burlap.behavior.policy.Policy;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.List;
import java.util.Map;

/**
 * Created by suat on 16-May-16.
 */
public abstract class Visualizer extends ApplicationFrame {
    public Visualizer(String title) {
        super(title);
    }

    public void createRewardGraph(String chartTitle, String xLabel, String yLabel, XYDataset dataset) {
        JFreeChart xylineChart = ChartFactory.createXYLineChart(chartTitle, xLabel, yLabel, dataset, PlotOrientation.VERTICAL, false, true, false);
        ChartPanel chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(750, 375));
        setContentPane(chartPanel);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    public abstract void drawGraph(String chartTitle, String xLabel, String yLabel, XYDataset dataset);

    public abstract XYDataset createDatasets(Map<Policy, List<P2DMEpisodeAnalysis>> episodeAnalysisList);
}
