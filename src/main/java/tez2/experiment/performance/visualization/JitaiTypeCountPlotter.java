package tez2.experiment.performance.visualization;

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
import org.jfree.data.xy.YIntervalSeriesCollection;
import tez2.environment.simulator.habit.HabitGainRatio;
import tez2.experiment.performance.StaticSelfManagementRewardPlotter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by suat on 15-Feb-18.
 */
public class JitaiTypeCountPlotter extends JFrame {

    public void drawJitaiTypeRatios(List<StaticSelfManagementRewardPlotter> datasets) {
        final DefaultCategoryDataset chartDataset = new DefaultCategoryDataset();
        for(int p=0; p<datasets.size(); p++) {
            StaticSelfManagementRewardPlotter dataset = datasets.get(p);
            int totalJitaiCount = getTotalJitaiCount(dataset);
            for(int i=0; i<3; i++) {
                double ratio = dataset.allAgents_totalNumberOfJitaisTypes.getY(0, i).doubleValue() / (double) totalJitaiCount;
                chartDataset.addValue(ratio, "JITAI-" + i, "Person" + (p+1));
            }
        }

        final JFreeChart chart = ChartFactory.createBarChart("Bar Chart Demo", // chart
                // title
                "Category", // domain axis label
                "Value", // range axis label
                chartDataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
        );

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(chartPanel);

        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private int getTotalJitaiCount(StaticSelfManagementRewardPlotter dataset) {
        int totalCount = 0;
        for(int i=0; i<3; i++) {
            totalCount += dataset.allAgents_totalNumberOfJitaisTypes.getY(0, i).intValue();
        }
        return totalCount;
    }
}
