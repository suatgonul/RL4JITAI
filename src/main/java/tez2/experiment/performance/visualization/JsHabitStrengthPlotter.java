package tez2.experiment.performance.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.YIntervalSeriesCollection;
import tez2.experiment.performance.StaticSelfManagementRewardPlotter;

import javax.swing.*;
import java.util.List;

/**
 * Created by suat on 23-Feb-18.
 */
public class JsHabitStrengthPlotter extends JFrame {


    public JFreeChart getChart(List<StaticSelfManagementRewardPlotter> dataset) {

        YIntervalSeriesCollection chartDataSet = new YIntervalSeriesCollection();
        chartDataSet.addSeries(dataset.get(0).allAgents_avgHabitStrengthPerEpisode.getSeries(0));
        chartDataSet.addSeries(dataset.get(1).allAgents_avgHabitStrengthPerEpisode.getSeries(0));

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Avg habit strength",
                "X-Axis",
                "Y-Axis",
                chartDataSet,
                PlotOrientation.VERTICAL,
                true, true, false);

        ((XYPlot) chart.getPlot()).setRenderer(0, new DeviationRenderer());

        return chart;
    }
}