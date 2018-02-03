package tez2.environment.simulator.habit.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.List;

public class ThresholdChart extends JFrame {
    private static final long serialVersionUID = 6294689542092367723L;

    public ThresholdChart(String title) {
        super(title);
    }

    public XYSeriesCollection createDataset(List<Double> thresholds, List<Double> positives, List<Double> negatives) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries thresholdSeries = new XYSeries("Threshold");
        XYSeries positivesSeries = new XYSeries("Positives");
        XYSeries negativeSeries = new XYSeries("Negatives");

        for(int i=0; i<thresholds.size(); i++) {
            thresholdSeries.add(i+1, thresholds.get(i));
            positivesSeries.add(i+1 , positives.get(i));
            negativeSeries.add(i+1, negatives.get(i));
        }

        //Add series to dataset
        dataset.addSeries(thresholdSeries);
        dataset.addSeries(positivesSeries);
        dataset.addSeries(negativeSeries);

        return dataset;
    }

    public void showChart(List<Double> thresholds, List<Double> positives, List<Double> negatives) {
        XYSeriesCollection dataset = createDataset( thresholds, positives, negatives);

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