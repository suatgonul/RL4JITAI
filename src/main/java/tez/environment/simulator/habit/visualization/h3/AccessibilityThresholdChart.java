package tez.environment.simulator.habit.visualization.h3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.List;

public class AccessibilityThresholdChart extends JFrame {
    private static final long serialVersionUID = 6294689542092367723L;

    public AccessibilityThresholdChart(String title) {
        super(title);
    }

    public XYSeriesCollection createDataset(List<Double> accessibilities, List<Double> thresholds) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries accessibilitySeries = new XYSeries("Accessibility");
        XYSeries thresholdSeries = new XYSeries("Threshold");

        for(int i=0; i<accessibilities.size(); i++) {
            accessibilitySeries.add(i+1, accessibilities.get(i));
            thresholdSeries.add(i+1 , thresholds.get(i));
        }

        //Add series to dataset
        dataset.addSeries(accessibilitySeries);
        dataset.addSeries(thresholdSeries);

        return dataset;
    }

    public void showChart(List<Double> accessibilities, List<Double> thresholds) {
        XYSeriesCollection dataset = createDataset(accessibilities, thresholds);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "XY Line Chart Example",
                "X-Axis",
                "Y-Axis",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }
}