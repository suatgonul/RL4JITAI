package tez2.experiment.performance.visualization;

import javafx.scene.chart.Chart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import tez2.environment.simulator.habit.HabitGainRatio;
import tez2.experiment.performance.StaticSelfManagementRewardPlotter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by suat on 15-Feb-18.
 */
public class EpisodeJitaiCountHabitStrengthPlotter extends JFrame {


    public JFreeChart getChart(List<StaticSelfManagementRewardPlotter> datasets) {
        YIntervalSeriesCollection jitaiCountsForPersonas = datasets.get(0).allAgents_totalJitaisPerEpisode;
        jitaiCountsForPersonas.addSeries(datasets.get(1).allAgents_totalJitaisPerEpisode.getSeries(0));

        XYSeries series2 = new XYSeries("series2");
        series2.add(10, 2);
        series2.add(30, 4);
        series2.add(60, 5);

        //construct the plot
        XYPlot plot = new XYPlot();

        plot.setDataset(0, jitaiCountsForPersonas);
        plot.setDataset(1, getHabitSeries());
//
        //customize the plot with renderers and axis
        plot.setRenderer(0, new XYLineAndShapeRenderer());//use default fill paint for first series
        plot.setRenderer(1, new XYLineAndShapeRenderer());
        plot.setRangeAxis(0, new NumberAxis("Series 1"));
        plot.setRangeAxis(1, new NumberAxis("Series 2"));
        plot.setDomainAxis(new NumberAxis("X Axis"));

        //Map the data to the appropriate axis
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);

        //generate the chart
        JFreeChart chart = new JFreeChart("MyPlot", Font.getFont(Font.MONOSPACED), plot, true);

        return chart;
    }

    public void drawEpisodeCountHabitStrengthChart(List<StaticSelfManagementRewardPlotter> datasets) {
        YIntervalSeriesCollection jitaiCountsForPersonas = datasets.get(0).allAgents_totalJitaisPerEpisode;
        jitaiCountsForPersonas.addSeries(datasets.get(1).allAgents_totalJitaisPerEpisode.getSeries(0));

        XYSeries series2 = new XYSeries("series2");
        series2.add(10, 2);
        series2.add(30, 4);
        series2.add(60, 5);

        //construct the plot
        XYPlot plot = new XYPlot();

        plot.setDataset(0, jitaiCountsForPersonas);
        plot.setDataset(1, getHabitSeries());
//
        //customize the plot with renderers and axis
        plot.setRenderer(0, new XYLineAndShapeRenderer());//use default fill paint for first series
        plot.setRenderer(1, new XYLineAndShapeRenderer());
        plot.setRangeAxis(0, new NumberAxis("Series 1"));
        plot.setRangeAxis(1, new NumberAxis("Series 2"));
        plot.setDomainAxis(new NumberAxis("X Axis"));

        //Map the data to the appropriate axis
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);

        //generate the chart
        JFreeChart chart = new JFreeChart("MyPlot", Font.getFont(Font.MONOSPACED), plot, true);
        //chart.setBackgroundPaint(Color.WHITE);
        JPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);

        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private XYSeriesCollection getHabitSeries() {
        XYSeries easyHabitSeries = new XYSeries("Easy Habit Series");
        int i = 0;
        for (; i < HabitGainRatio.easy.size(); i++) {
            easyHabitSeries.add(i, HabitGainRatio.easy.get(i));
        }
        for (; i < 100; i++) {
            easyHabitSeries.add(i, 100);
        }

        XYSeries mediumHabitSeries = new XYSeries("Medium Habit Series");
        for (i = 0; i < HabitGainRatio.medium.size(); i++) {
            mediumHabitSeries.add(i, HabitGainRatio.medium.get(i) * 100. / 35.);
        }
        for (; i < 100; i++) {
            mediumHabitSeries.add(i, 100);
        }

        XYSeriesCollection habitSeriesCollection = new XYSeriesCollection();
        habitSeriesCollection.addSeries(easyHabitSeries);
        habitSeriesCollection.addSeries(mediumHabitSeries);
        return habitSeriesCollection;
    }
}
