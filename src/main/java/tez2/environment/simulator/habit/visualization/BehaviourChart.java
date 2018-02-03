package tez2.environment.simulator.habit.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.List;

public class BehaviourChart extends JFrame {
    private static final long serialVersionUID = 6294689542092367723L;

    public BehaviourChart(String title) {
        super(title);
    }

    public XYSeriesCollection createDataset(List<Boolean> behaviours, List<Boolean> reminders) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries behaviourSeries = new XYSeries("Behaviours");
        XYSeries reminderSeries = new XYSeries("Reminders");
        for(int i=0; i<behaviours.size(); i++) {
            behaviourSeries.add(i+1, behaviours.get(i) ? 1 : 0);
            reminderSeries.add(i+1, reminders.get(i) ? 1 : 0);
        }

        //Add series to dataset
        dataset.addSeries(behaviourSeries);
        dataset.addSeries(reminderSeries);
        return dataset;
    }

    public void showChart(List<Boolean> behaviours, List<Boolean> reminders) {
        XYSeriesCollection dataset = createDataset(behaviours, reminders);

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