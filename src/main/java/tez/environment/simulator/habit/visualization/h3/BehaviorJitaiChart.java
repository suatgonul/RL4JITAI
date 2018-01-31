package tez.environment.simulator.habit.visualization.h3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.List;

public class BehaviorJitaiChart extends JFrame {
    private static final long serialVersionUID = 6294689542092367723L;

    public BehaviorJitaiChart(String title) {
        super(title);
    }

    public XYSeriesCollection createDataset(List<Integer> behaviours, List<Integer> remembers) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries behaviourSeries = new XYSeries("Behaviours");
        XYSeries rememberSeries = new XYSeries("Remembers");
        for(int i=0; i<behaviours.size(); i++) {
            behaviourSeries.add(i+1, behaviours.get(i));
            rememberSeries.add(i+1, remembers.get(i));
        }

        //Add series to dataset
        dataset.addSeries(behaviourSeries);
        dataset.addSeries(rememberSeries);
        return dataset;
    }

    public void showChart(List<Integer> behaviours, List<Integer> remembers) {
        XYSeriesCollection dataset = createDataset(behaviours, remembers);

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