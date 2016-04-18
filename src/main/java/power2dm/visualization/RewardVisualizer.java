package power2dm.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.List;
import java.util.Map;

/**
 * Created by suatgonul on 4/15/2016.
 */
public class RewardVisualizer extends ApplicationFrame {
    public RewardVisualizer(String applicationTitle, String chartTitle, Map<String, List<Double>> rewards) {
        super(applicationTitle);
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                chartTitle,
                "Episode",
                "Reward",
                createDataset(rewards),
                PlotOrientation.VERTICAL,
                false, true, false);

        ChartPanel chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(750, 375));
//        final XYPlot plot = xylineChart.getXYPlot();
//        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//        renderer.setSeriesPaint(0, Color.RED);
//        renderer.setSeriesStroke(0, new BasicStroke(4.0f));
//        plot.setRenderer(renderer);
        setContentPane(chartPanel);
    }

    private XYDataset createDataset(Map<String, List<Double>> rewards) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for(Map.Entry<String, List<Double>> e : rewards.entrySet()) {
            XYSeries rewardSeries = new XYSeries(e.getKey());
            for (int i = 0; i < e.getValue().size(); i++) {
                rewardSeries.add(i, e.getValue().get(i));
            }
            dataset.addSeries(rewardSeries);
        }
        return dataset;
    }

    public static void createRewardGraph(String applicationTitle, String chartTitle, Map<String, List<Double>> totalRewards) {
        RewardVisualizer chart = new RewardVisualizer(applicationTitle, chartTitle, totalRewards);
        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }

    public static void main(final String[] args) {
        RewardVisualizer chart = new RewardVisualizer("Browser Usage Statistics", "Which Browser are you using?", null);
        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }
}
