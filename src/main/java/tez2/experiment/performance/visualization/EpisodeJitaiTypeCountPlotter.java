package tez2.experiment.performance.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeriesCollection;
import tez2.environment.simulator.habit.HabitGainRatio;
import tez2.experiment.performance.StaticSelfManagementRewardPlotter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by suat on 15-Feb-18.
 */
public class EpisodeJitaiTypeCountPlotter extends JFrame {


    public JFreeChart getChart(StaticSelfManagementRewardPlotter dataset) {

        XYSeriesCollection chartDataSet = new XYSeriesCollection();
        for(int a=0; a<3; a++) {
            //Add series to chartDataset
            chartDataSet.addSeries(dataset.allAgents_totalNumberOfJitaiTypesPerEpisode.get(a).getSeries(0));
        }

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "XY Line Chart Example",
                "X-Axis",
                "Y-Axis",
                chartDataSet,
                PlotOrientation.VERTICAL,
                true, true, false);

        return chart;
    }
}
