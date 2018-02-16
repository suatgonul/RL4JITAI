package tez2.experiment.performance.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * Created by suat on 15-Feb-18.
 */
public class Temp extends JFrame {
    public static void main(String[] args) {
        Temp temp = new Temp();

//        example.setSize(800, 400);
//        example.setLocationRelativeTo(null);
//        example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        temp.pack();
        RefineryUtilities.centerFrameOnScreen(temp);
        temp.setVisible(true);

        //create the series - add some dummy data
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(0.2, "Jitai 1", "Person 1");
        dataset.addValue(0.4, "Jitai 2", "Person 1");
        dataset.addValue(0.4, "Jitai 3", "Person 1");

        dataset.addValue(0.8, "Jitai 1", "Person 2");
        dataset.addValue(0.1, "Jitai 2", "Person 2");
        dataset.addValue(0.1, "Jitai 3", "Person 2");

        final JFreeChart chart = ChartFactory.createStackedBarChart(
                "Stacked Bar Chart Demo 2",
                "Person",                  // domain axis label
                "Jitais",                     // range axis label
                dataset,                     // data
                PlotOrientation.VERTICAL,  // the plot orientation
                true,                        // include legend
                true,                        // tooltips
                false                        // urls
        );

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        temp.setContentPane(chartPanel);
    }
}
