package tez2.experiment.performance.visualization;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import tez2.experiment.performance.StaticSelfManagementRewardPlotter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by suat on 15-Feb-18.
 */
public class EpisodeJitaiCountHabitStrengthPlotter extends JFrame {

    public void drawCharts(List<StaticSelfManagementRewardPlotter> datasets) {
        // first merge the plotter data
        YIntervalSeriesCollection firstSeries = datasets.get(0).allAgents_totalJitaisPerEpisode;
        firstSeries.addSeries(datasets.get(1).allAgents_totalJitaisPerEpisode.getSeries(0));
        firstSeries.setNotify(true);
        drawChart(firstSeries);
        for(int i=0; i<firstSeries.getSeriesCount(); i++) {
            YIntervalSeries series = firstSeries.getSeries(i);
            System.out.println("Series " + i + " count: " + series.getItemCount());
            for(int j=0; j<series.getItemCount(); j++) {
                System.out.println("x: " + series.getX(j) + ", y: " + series.getYValue(j));
            }
        }
    }

    public void drawChart(YIntervalSeriesCollection jitaiCountsForPersonas) {
        XYSeries series2 = new XYSeries("series2");
        series2.add(10, 2);
        series2.add(30, 4);
        series2.add(60, 5);

        XYSeriesCollection dataset2 = new XYSeriesCollection();
        dataset2.addSeries(series2);

        //construct the plot
        XYPlot plot = new XYPlot();

        plot.setDataset(0, jitaiCountsForPersonas);
        plot.setDataset(1, dataset2);
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
        chart.setBackgroundPaint(Color.WHITE);
        JPanel chartPanel = new ChartPanel(chart);
        chartPanel.setVisible(true);
        setContentPane(chartPanel);
        setVisible(true);
    }
}
