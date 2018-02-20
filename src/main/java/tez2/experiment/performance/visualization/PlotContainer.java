package tez2.experiment.performance.visualization;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

/**
 * Created by suat on 17-Feb-18.
 */
public class PlotContainer extends JFrame {

    private int maxHeight = 1000;
    private int columns = 2;
    private int chartWidth = 750;
    private int chartHeight = 500;

    private Container plotContainer;
    private GridBagConstraints c;

    public PlotContainer() {
        plotContainer = new Container();
        plotContainer.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 10);

        int totalChartHeight = ((8 / columns) + 1) * (chartHeight + 10);
        if (totalChartHeight > maxHeight) {
            JScrollPane scrollPane = new JScrollPane(plotContainer);
            scrollPane.setPreferredSize(new Dimension(chartWidth * columns + 50, maxHeight));
            this.add(scrollPane);
        } else {
            this.add(plotContainer);
        }

        this.pack();
        this.setVisible(true);

    }

    public void insertChart(JFreeChart chart) {
        ChartPanel chartPanelCSRAvg = new ChartPanel(chart);
        chartPanelCSRAvg.setPreferredSize(new Dimension(chartWidth, chartHeight));
        plotContainer.add(chartPanelCSRAvg, c);
        this.updateGBConstraint(c, columns);
        chartPanelCSRAvg.setVisible(true);
    }

    private void updateGBConstraint(GridBagConstraints c, int maxCol) {
        c.gridx++;
        if (c.gridx >= maxCol) {
            c.gridx = 0;
            c.gridy++;
        }
    }
}
