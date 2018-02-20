package tez2.experiment.performance.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlock;
import org.jfree.ui.RectangleEdge;
import tez2.experiment.performance.StaticSelfManagementRewardPlotter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 15-Feb-18.
 */
public class TimeOfDayJitaiCountBehaviorPerformancePlotter extends JFrame {

    public JFreeChart getChart(StaticSelfManagementRewardPlotter dataset) {
        final DefaultCategoryDataset chartDataset = new DefaultCategoryDataset();
        int totalCount = getTotalJitaiCount(dataset);
        int totalBehaviorCount = getTotalBehaviorPerformanceCount(dataset);
        for (int i = 0; i < 96; i++) {
            int count = dataset.allAgents_totalNumberOfJitaisPerTimeOfDay.getY(0, i).intValue();
            chartDataset.addValue((double) count / (double) totalCount, "Jitai Ratio", getQuerterHourFromIndex(i));
            count = dataset.allAgents_totalNumberOfBehaviorPerformancePerTimeOfDay.getY(0, i).intValue();
            chartDataset.addValue((double) count / (double) totalBehaviorCount, "Performance Ratio", getQuerterHourFromIndex(i));
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

        final SparselyLabeledCategoryAxis domainAxis = new SparselyLabeledCategoryAxis("Person");
        //final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        plot.setDomainAxis(domainAxis);

        return chart;
    }

    private String getQuerterHourFromIndex(int index) {
        int reminder = index % 4;
        int hour = index / 4;
        return hour + ":" + (reminder * 15);
    }

    private String getHourFromIndex(int index) {
        return (index / 4) + "";
    }

    private int getTotalJitaiCount(StaticSelfManagementRewardPlotter dataset) {
        int totalCount = 0;
        for (int i = 0; i < 96; i++) {
            totalCount += dataset.allAgents_totalNumberOfJitaisPerTimeOfDay.getY(0, i).intValue();
        }
        return totalCount;
    }

    private int getTotalBehaviorPerformanceCount(StaticSelfManagementRewardPlotter dataset) {
        int totalCount = 0;
        for (int i = 0; i < 96; i++) {
            totalCount += dataset.allAgents_totalNumberOfBehaviorPerformancePerTimeOfDay.getY(0, i).intValue();
        }
        return totalCount;
    }

    private class SparselyLabeledCategoryAxis extends CategoryAxis {
        private static final long serialVersionUID = 478725789943763302L;

        /**
         * Construct and axis with a label.
         *
         * @param label the axis label
         */
        public SparselyLabeledCategoryAxis(String label) {
            super(label);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea,
                                 RectangleEdge edge) {
            List<CategoryTick> standardTicks = super.refreshTicks(g2, state, dataArea, edge);
            if (standardTicks.isEmpty()) {
                return standardTicks;
            }
            int tickEvery = 4;

            //Replace a few labels with blank ones
            List<CategoryTick> fixedTicks = new ArrayList<CategoryTick>(standardTicks.size());
            //Skip the first tick so your 45degree labels don't fall of the edge
            for (int i = 0; i < standardTicks.size(); i++) {
                CategoryTick tick = standardTicks.get(i);
                if (i % tickEvery == 0) {
                    fixedTicks.add(tick);
                } else {
                    fixedTicks.add(new CategoryTick(tick.getCategory(), new TextBlock(), tick
                            .getLabelAnchor(), tick.getRotationAnchor(), tick.getAngle()));
                }
            }
            return fixedTicks;
        }
    }
}
