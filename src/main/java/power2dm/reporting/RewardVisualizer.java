package power2dm.reporting;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
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
    public RewardVisualizer(String applicationTitle, Policy policy, Map<Policy, List<P2DMEpisodeAnalysis>> rewards) {
        super(applicationTitle);
        JFreeChart xylineChart = ChartFactory.createXYLineChart(getChartTitle(policy), "Episode", "Reward", createDataset(rewards), PlotOrientation.VERTICAL, false, true, false);

        ChartPanel chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(750, 375));
        setContentPane(chartPanel);
    }

    private XYDataset createDataset(Map<Policy, List<P2DMEpisodeAnalysis>> episodeAnalysisList) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (Map.Entry<Policy, List<P2DMEpisodeAnalysis>> e : episodeAnalysisList.entrySet()) {
            XYSeries rewardSeries = new XYSeries(e.getKey().toString());
            for (int i = 0; i < e.getValue().size(); i++) {
                rewardSeries.add(i, e.getValue().get(i).getTotalReward());
            }
            dataset.addSeries(rewardSeries);
        }
        return dataset;
    }

    public void createRewardGraph() {
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    private static String getChartTitle(Policy policy) {
        if (policy instanceof EpsilonGreedy) {
            double epsilon = ((EpsilonGreedy) policy).getEpsilon();
            return "Epsilon Greedy (" + epsilon + ")";
        }
        return "";
    }

    public static void main(final String[] args) {
        RewardVisualizer chart = new RewardVisualizer("Browser Usage Statistics", null, null);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}
