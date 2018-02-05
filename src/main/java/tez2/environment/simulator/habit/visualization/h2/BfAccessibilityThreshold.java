package tez2.environment.simulator.habit.visualization.h2;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import tez2.environment.simulator.habit.HabitSimulator2;

import javax.swing.*;
import java.util.List;

public class BfAccessibilityThreshold extends JFrame {
    private static final long serialVersionUID = 6294689542092367723L;

    public BfAccessibilityThreshold(String title) {
        super(title);
    }

    public XYSeriesCollection createDataset(List<HabitSimulator2.BehaviorFrequency> behaviorFrequencies) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries behaviourFrequencySeries = new XYSeries("Behavior frequency");
        XYSeries thresholdSeries= new XYSeries("Threshold");
        XYSeries accessibilitySeries = new XYSeries("Accessibility");
        XYSeries habitStrengths = new XYSeries("Habit Strength");
        for(int i=0; i<behaviorFrequencies.size(); i++) {
            behaviourFrequencySeries.add(i+1, behaviorFrequencies.get(i).getBehaviourFrequency());
            thresholdSeries.add(i+1, behaviorFrequencies.get(i).getThreshold());
            accessibilitySeries.add(i+1, behaviorFrequencies.get(i).getAccessibility());
            habitStrengths.add(i+1, behaviorFrequencies.get(i).getHabitStrength());
        }

        //Add series to dataset
        dataset.addSeries(behaviourFrequencySeries);
        dataset.addSeries(thresholdSeries);
        dataset.addSeries(accessibilitySeries);
        dataset.addSeries(habitStrengths);
        return dataset;
    }

    public void showChart(List<HabitSimulator2.BehaviorFrequency> behaviorFrequencies) {
        XYSeriesCollection dataset = createDataset(behaviorFrequencies);

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