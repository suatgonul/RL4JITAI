package tez.experiment.performance;

import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentObserver;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by suatgonul on 4/27/2017.
 */
public class SelfManagementRewardPlotter extends JFrame implements EnvironmentObserver {

    private static final long serialVersionUID = 1L;


    private static final Map<Integer, Double> cachedCriticalValues = new HashMap<Integer, Double>();


    /**
     * Contains all the current trial performance data
     */
    protected Trial curTrial;

    /**
     * contains the plot series data that will be displayed for the current agent
     */
    protected AgentDatasets curAgentDatasets;


    /**
     * contains all trial data for each agent
     */
    protected Map<String, List<Trial>> agentTrials;


    /**
     * The name of the current agent being tested
     */
    protected String curAgentName;


    /**
     * All agent plot series for the the most recetent trial's cumulative reward per step
     */
    protected XYSeriesCollection colCSR;

    /**
     * All agent plot series for the the most recetent trial's cumulative reward per episode
     */
    protected XYSeriesCollection colCER;

    /**
     * All agent plot series for the the most recetent trial's average reward per episode
     */
    protected XYSeriesCollection colAER;

    /**
     * All agent plot series for the the most recetent trial's median reward per episode
     */
    protected XYSeriesCollection colMER;

    /**
     * All agent plot series for the the most recetent trial's cumulative step per episode
     */
    protected XYSeriesCollection colCSE;

    /**
     * All agent plot series for the most recetent trial's steps per episode
     */
    protected XYSeriesCollection colSE;
    protected XYSeriesCollection colRPE;
    protected XYSeriesCollection colURE;


    /**
     * All agent plot series for the average of all trial's cumulative reward per step
     */
    protected YIntervalSeriesCollection colCSRAvg;

    /**
     * All agent plot series for the average of all trial's cumulative reward per episode
     */
    protected YIntervalSeriesCollection colCERAvg;

    /**
     * All agent plot series for the average of all trial's average reward per episode
     */
    protected YIntervalSeriesCollection colAERAvg;

    /**
     * All agent plot series for the average of all trial's median reward per episode
     */
    protected YIntervalSeriesCollection colMERAvg;

    /**
     * All agent plot series for the average of all trial's cumulative steps per episode
     */
    protected YIntervalSeriesCollection colCSEAvg;

    /**
     * All agent plot series for the average of all trial's steps per episode
     */
    protected YIntervalSeriesCollection colSEAvg;
    protected YIntervalSeriesCollection colRPEAvg;
    protected YIntervalSeriesCollection colUREAvg;


    /**
     * A set specifying the performance metrics that will be plotted
     */
    protected Set<SelfManagementPerformanceMetric> metricsSet = new HashSet<SelfManagementPerformanceMetric>();

    /**
     * specifies whether the most recent trial, average of all trials, or both plots will be displayed
     */
    protected TrialMode trialMode;


    /**
     * Whether the data from action observations received should be recoreded or not.
     */
    protected boolean collectData = false;


    /**
     * The last time step at which the plots' series data was updated
     */
    protected int lastTimeStepUpdate = 0;

    /**
     * The last episode at which the plot's series data was updated
     */
    protected int lastEpisode = 0;


    /**
     * the current time step that was recorded
     */
    protected int curTimeStep = 0;

    /**
     * the current episode that was recorded
     */
    protected int curEpisode = 0;

    /**
     * the delay in milliseconds between which the charts are updated automatically
     */
    protected int delay = 1000;

    /**
     * the significance level used for confidence intervals. The default is 0.05 (corresponding to a 95% CI).
     */
    protected double significance = 0.05;


    /**
     * Whether the current plots need their series data cleared for a new trial
     */
    protected boolean needsClearing = false;

    /**
     * Synchronization object to ensure proper threaded plot updating
     */
    protected MutableBoolean trialUpdateComplete = new MutableBoolean(true);


    /**
     * Initializes a performance plotter.
     *
     * @param firstAgentName  the name of the first agent whose performance will be measured.
     * @param chartWidth      the width of each chart/plot
     * @param chartHeight     the height of each chart//plot
     * @param columns         the number of columns of the plots displayed. Plots are filled in columns first, then move down the next row.
     * @param maxWindowHeight the maximum window height allowed before a scroll view is used.
     * @param trialMode       which plots to use; most recent trial, average over all trials, or both. If both, the most recent plot will be inserted into the window first, then the average.
     * @param metrics         the metrics that should be plotted. The metrics will appear in the window in the order that they are specified (columns first)
     */
    public SelfManagementRewardPlotter(String firstAgentName, int chartWidth, int chartHeight, int columns, int maxWindowHeight,
                                       TrialMode trialMode, SelfManagementPerformanceMetric... metrics) {

        this.curAgentName = firstAgentName;

        this.agentTrials = new HashMap<String, List<Trial>>();
        this.agentTrials.put(this.curAgentName, new ArrayList<SelfManagementRewardPlotter.Trial>());

        colCSR = new XYSeriesCollection();
        colCER = new XYSeriesCollection();
        colAER = new XYSeriesCollection();
        colMER = new XYSeriesCollection();
        colCSE = new XYSeriesCollection();
        colSE = new XYSeriesCollection();
        colRPE = new XYSeriesCollection();
        colURE = new XYSeriesCollection();

        colCSRAvg = new YIntervalSeriesCollection();
        colCERAvg = new YIntervalSeriesCollection();
        colAERAvg = new YIntervalSeriesCollection();
        colMERAvg = new YIntervalSeriesCollection();
        colCSEAvg = new YIntervalSeriesCollection();
        colSEAvg = new YIntervalSeriesCollection();
        colRPEAvg = new YIntervalSeriesCollection();
        colUREAvg = new YIntervalSeriesCollection();

        this.curTrial = new Trial();
        this.curAgentDatasets = new AgentDatasets(curAgentName);

        if (metrics.length == 0) {
            metricsSet.add(SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP);

            metrics = new SelfManagementPerformanceMetric[]{SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP};
        }

        this.trialMode = trialMode;


        Container plotContainer = new Container();
        plotContainer.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 10);

        for (SelfManagementPerformanceMetric m : metrics) {

            this.metricsSet.add(m);

            if (m == SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Reward", "Time Step", "Cumulative Reward", colCSR, colCSRAvg);
            } else if (m == SelfManagementPerformanceMetric.CUMULTAIVEREWARDPEREPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Reward", "Episode", "Cumulative Reward", colCER, colCERAvg);
            } else if (m == SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Average Reward", "Episode", "Average Reward", colAER, colAERAvg);
            } else if (m == SelfManagementPerformanceMetric.MEDIANEPISODEREWARD) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Median Reward", "Episode", "Median Reward", colMER, colMERAvg);
            } else if (m == SelfManagementPerformanceMetric.CUMULATIVESTEPSPEREPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Steps", "Episode", "Cumulative Steps", colCSE, colCSEAvg);
            } else if (m == SelfManagementPerformanceMetric.STEPSPEREPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Number of Steps", "Episode", "Number of Steps", colSE, colSEAvg);
            } else if (m == SelfManagementPerformanceMetric.REWARD_PER_EPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Episode Reward", "Episode", "Episode Reward", colRPE, colRPEAvg);
            } else if (m == SelfManagementPerformanceMetric.USER_REACTION_PER_EPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Episode User Reaction", "Episode", "Episode User Reaction", colURE, colUREAvg);
            }


        }

        int totalChartHeight = ((metrics.length / columns) + 1) * (chartHeight + 10);
        if (totalChartHeight > maxWindowHeight) {
            JScrollPane scrollPane = new JScrollPane(plotContainer);
            scrollPane.setPreferredSize(new Dimension(chartWidth * columns + 50, maxWindowHeight));
            this.add(scrollPane);
        } else {
            this.add(plotContainer);
        }


    }


    /**
     * sets the delay in milliseconds between automatic refreshes of the plots
     *
     * @param delayInMS the refresh delay in milliseconds
     */
    public void setRefreshDelay(int delayInMS) {
        this.delay = delayInMS;
    }


    /**
     * Sets the significance used for confidence intervals.
     * The default is 0.05, which corresponds to a 95% confidence interval.
     *
     * @param signifcance the significance used for confidence intervals.
     */
    public void setSignificanceForCI(double signifcance) {
        this.significance = signifcance;
    }


    /**
     * Toggle whether performance data collected from the action observation is recorded or not
     *
     * @param collectData true if data collected should be plotted; false if not.
     */
    public void toggleDataCollection(boolean collectData) {
        this.collectData = collectData;
    }


    /**
     * Launches the GUI and automatic refresh thread.
     */
    public void startGUI() {
        this.pack();
        this.setVisible(true);
        this.launchThread();
    }

    public void observeEnvironmentActionInitiation(State o, GroundedAction action) {
        //do nothing
    }

    synchronized public void observeEnvironmentInteraction(EnvironmentOutcome eo) {
        if (!this.collectData) {
            return;
        }

        this.curTrial.stepIncrement(eo.r);
        this.curTimeStep++;

    }

    public void observeEnvironmentReset(Environment resetEnvironment) {
        //do nothing
    }

    /**
     * Informs the plotter that all data for the last episode has been collected.
     */
    synchronized public void endEpisode() {
        this.curTrial.setupForNewEpisode();
        this.curEpisode++;
    }


    /**
     * Informs the plotter that a new trial of the current agent is beginning.
     */
    synchronized public void startNewTrial() {
        if (this.curTimeStep > 0) {
            this.needsClearing = true;
        }

        this.curTrial = new Trial();
        this.lastTimeStepUpdate = 0;
        this.lastEpisode = 0;
        this.curTimeStep = 0;
        this.curEpisode = 0;
    }

    /**
     * Informs the plotter that all data for the current trial as been collected.
     */
    public void endTrial() {


        this.trialUpdateComplete.b = false;
        this.updateTimeSeries();
        this.agentTrials.get(this.curAgentName).add(curTrial);


        //wait until it's updated before allowing anything else to happen
        synchronized (this.trialUpdateComplete) {
            while (this.trialUpdateComplete.b == false) {
                try {
                    this.trialUpdateComplete.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.trialUpdateComplete.notifyAll();

        }

    }


    /**
     * Informs the plotter that data collecton for a new agent should begin.
     * If the current agent is already set to the agent name provided, then a warning message is printed and nothing changes.
     *
     * @param agentName the name of the agent
     */
    synchronized public void startNewAgent(final String agentName) {

        if (this.curAgentName.equals(agentName)) {
            System.out.println("Already recording data for: " + agentName + "; noting to change from startNewAgent method call.");
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (SelfManagementRewardPlotter.this) {
                    SelfManagementRewardPlotter.this.endTrialsForCurrentAgent();
                    SelfManagementRewardPlotter.this.curAgentName = agentName;
                    SelfManagementRewardPlotter.this.agentTrials.put(SelfManagementRewardPlotter.this.curAgentName, new ArrayList<SelfManagementRewardPlotter.Trial>());
                    SelfManagementRewardPlotter.this.curAgentDatasets = new AgentDatasets(curAgentName);
                }
            }
        });
    }


    /**
     * Informs the plotter that all data for all agents has been collected.
     * Will also cause the average plots for the last agent's data to be plotted.
     */
    synchronized public void endAllAgents() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (SelfManagementRewardPlotter.this) {
                    SelfManagementRewardPlotter.this.endTrialsForCurrentAgent();
                }
            }
        });
    }

    /**
     * Adds the most recent trial (if enabled) chart and trial average (if enabled) chart into the provided container.
     * The GridBagConstraints will aumatically be incremented to the next position after this method returns.
     *
     * @param plotContainer        the contain in which to insert the plot(s).
     * @param c                    the current grid bag contraint locaiton in which the plots should be inserted.
     * @param columns              the number of columns to fill in the plot container
     * @param chartWidth           the width of any single plot
     * @param chartHeight          the height of any single plot
     * @param title                the title to label thep plot; if average trial plots are enabled the word "Average" will be prepended to the title for the average plot.
     * @param xlab                 the xlab axis of the plot
     * @param ylab                 the y lab axis of the plot
     * @param mostRecentCollection the XYSeriesCollection dataset with which the most recent trial plot is associated
     * @param averageCollection    the YIntervalSeriesCollection dataset with which the trial average plot is associated
     */
    protected void insertChart(Container plotContainer, GridBagConstraints c, int columns, int chartWidth, int chartHeight,
                               String title, String xlab, String ylab, XYSeriesCollection mostRecentCollection, YIntervalSeriesCollection averageCollection) {

        if (this.trialMode.mostRecentTrialEnabled()) {
            final JFreeChart chartCSR = ChartFactory.createXYLineChart(title, xlab, ylab, mostRecentCollection);
            ChartPanel chartPanelCSR = new ChartPanel(chartCSR);
            chartPanelCSR.setPreferredSize(new java.awt.Dimension(chartWidth, chartHeight));
            plotContainer.add(chartPanelCSR, c);
            this.updateGBConstraint(c, columns);
        }

        if (this.trialMode.averagesEnabled()) {
            final JFreeChart chartCSRAvg = ChartFactory.createXYLineChart("Average " + title, xlab, ylab, averageCollection);
            ((XYPlot) chartCSRAvg.getPlot()).setRenderer(this.createDeviationRenderer());
            ChartPanel chartPanelCSRAvg = new ChartPanel(chartCSRAvg);
            chartPanelCSRAvg.setPreferredSize(new java.awt.Dimension(chartWidth, chartHeight));
            plotContainer.add(chartPanelCSRAvg, c);
            this.updateGBConstraint(c, columns);
        }
    }


    /**
     * Creates a DeviationRenderer to use for the trial average plots
     *
     * @return a DeviationRenderer
     */
    protected DeviationRenderer createDeviationRenderer() {
        DeviationRenderer renderer = new DeviationRenderer(true, false);

        for (int i = 0; i < DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length; i++) {
            Color c = (Color) DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i];
            Color nc = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
            renderer.setSeriesFillPaint(i, nc);
        }

        return renderer;
    }


    /**
     * Increments the x-y position of a constraint to the next position.
     * If there are still free columns in the current row, then the next position in the next column; otherwise a new row is started.
     *
     * @param c      the constraint to increment
     * @param maxCol the maximum columns allowable in a container
     */
    protected void updateGBConstraint(GridBagConstraints c, int maxCol) {
        c.gridx++;
        if (c.gridx >= maxCol) {
            c.gridx = 0;
            c.gridy++;
        }
    }


    /**
     * Launches the automatic plot refresh thread.
     */
    protected void launchThread() {
        Thread refreshThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    SelfManagementRewardPlotter.this.updateTimeSeries();
                    try {
                        Thread.sleep(SelfManagementRewardPlotter.this.delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        refreshThread.start();

    }


    /**
     * Updates all the most recent trial time series with the latest data
     */
    synchronized protected void updateTimeSeries() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (SelfManagementRewardPlotter.this.trialMode.mostRecentTrialEnabled()) {
                    synchronized (SelfManagementRewardPlotter.this) {

                        synchronized (SelfManagementRewardPlotter.this.trialUpdateComplete) {

                            if (SelfManagementRewardPlotter.this.needsClearing) {
                                SelfManagementRewardPlotter.this.curAgentDatasets.clearNonAverages();
                                SelfManagementRewardPlotter.this.needsClearing = false;
                            }

                            if (SelfManagementRewardPlotter.this.curTimeStep > SelfManagementRewardPlotter.this.lastTimeStepUpdate) {
                                SelfManagementRewardPlotter.this.updateCSRSeries();
                                SelfManagementRewardPlotter.this.lastTimeStepUpdate = curTimeStep;
                            }
                            if (SelfManagementRewardPlotter.this.curEpisode > SelfManagementRewardPlotter.this.lastEpisode) {
                                SelfManagementRewardPlotter.this.updateCERSeries();
                                SelfManagementRewardPlotter.this.updateAERSeris();
                                SelfManagementRewardPlotter.this.updateMERSeris();
                                SelfManagementRewardPlotter.this.updateCSESeries();
                                SelfManagementRewardPlotter.this.updateSESeries();
                                SelfManagementRewardPlotter.this.updateRPESeries();
                                SelfManagementRewardPlotter.this.updateURESeries();

                                SelfManagementRewardPlotter.this.lastEpisode = SelfManagementRewardPlotter.this.curEpisode;
                            }


                            SelfManagementRewardPlotter.this.trialUpdateComplete.b = true;
                            SelfManagementRewardPlotter.this.trialUpdateComplete.notifyAll();

                        }
                    }
                }
            }
        });
    }


    /**
     * Informs the plotter that all trials for the current agent have been collected and causes the average plots to be set and displayed.
     */
    protected void endTrialsForCurrentAgent() {

        final String aName = this.curAgentName;

        if (!this.trialMode.averagesEnabled()) {
            return;
        }


        List<Trial> trials = SelfManagementRewardPlotter.this.agentTrials.get(aName);
        int[] n = SelfManagementRewardPlotter.this.minStepAndEpisodes(trials);


        if (this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP)) {
            for (int i = 0; i < n[0]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.cumulativeStepReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.csrAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }


        if (this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULTAIVEREWARDPEREPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.cumulativeEpisodeReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.cerAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }


        if (this.metricsSet.contains(SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.averageEpisodeReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.aerAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.MEDIANEPISODEREWARD)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.medianEpisodeReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.merAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVESTEPSPEREPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.cumulativeStepEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.cseAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.STEPSPEREPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.stepEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.seAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.REWARD_PER_EPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.cumulativeRewardInEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.rpeAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.USER_REACTION_PER_EPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.cumulativeEpisodeUserReaction.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.ureAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }
        curAgentDatasets.fireAllAverages();
    }


    /**
     * Updates the cumulative reward by step series. Does nothing if that metric is not being plotted.
     */
    protected void updateCSRSeries() {

        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP)) {
            return;
        }

        int n = this.curTrial.cumulativeStepReward.size();
        for (int i = this.lastTimeStepUpdate; i < n; i++) {
            this.curAgentDatasets.cumulativeStepRewardSeries.add((double) i, this.curTrial.cumulativeStepReward.get(i), false);
        }
        if (n > this.lastTimeStepUpdate) {
            this.curAgentDatasets.cumulativeStepRewardSeries.fireSeriesChanged();
        }
    }


    /**
     * Updates the cumulative reward by episode series.  Does nothing if that metric is not being plotted.
     */
    protected void updateCERSeries() {

        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULTAIVEREWARDPEREPISODE)) {
            return;
        }

        int n = this.curTrial.cumulativeEpisodeReward.size();
        for (int i = this.lastEpisode; i < n; i++) {
            this.curAgentDatasets.cumulativeEpisodeRewardSeries.add((double) i, this.curTrial.cumulativeEpisodeReward.get(i), false);
        }
        if (n > this.lastEpisode) {
            this.curAgentDatasets.cumulativeEpisodeRewardSeries.fireSeriesChanged();
        }

    }


    /**
     * Updates the average reward by episode series.  Does nothing if that metric is not being plotted.
     */
    protected void updateAERSeris() {

        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD)) {
            return;
        }

        int n = this.curTrial.averageEpisodeReward.size();
        for (int i = this.lastEpisode; i < n; i++) {
            this.curAgentDatasets.averageEpisodeRewardSeries.add((double) i, this.curTrial.averageEpisodeReward.get(i), false);
        }
        if (n > this.lastEpisode) {
            this.curAgentDatasets.averageEpisodeRewardSeries.fireSeriesChanged();
        }
    }


    /**
     * Updates the median reward by episode series.  Does nothing if that metric is not being plotted.
     */
    protected void updateMERSeris() {

        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.MEDIANEPISODEREWARD)) {
            return;
        }

        int n = this.curTrial.medianEpisodeReward.size();
        for (int i = this.lastEpisode; i < n; i++) {
            this.curAgentDatasets.medianEpisodeRewardSeries.add((double) i, this.curTrial.medianEpisodeReward.get(i), false);
        }
        if (n > this.lastEpisode) {
            this.curAgentDatasets.medianEpisodeRewardSeries.fireSeriesChanged();
        }
    }


    /**
     * Updates the cumulative steps by episode series.  Does nothing if that metric is not being plotted.
     */
    protected void updateCSESeries() {

        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVESTEPSPEREPISODE)) {
            return;
        }

        int n = this.curTrial.cumulativeStepEpisode.size();
        for (int i = this.lastEpisode; i < n; i++) {
            this.curAgentDatasets.cumulativeStepEpisodeSeries.add((double) i, this.curTrial.cumulativeStepEpisode.get(i), false);
        }
        if (n > this.lastEpisode) {
            this.curAgentDatasets.cumulativeStepEpisodeSeries.fireSeriesChanged();
        }
    }


    /**
     * Updates the steps by episode series.  Does nothing if that metric is not being plotted.
     */
    protected void updateSESeries() {
        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.STEPSPEREPISODE)) {
            return;
        }

        int n = this.curTrial.stepEpisode.size();
        for (int i = this.lastEpisode; i < n; i++) {
            this.curAgentDatasets.stepEpisodeSeries.add((double) i, this.curTrial.stepEpisode.get(i), false);
        }
        if (n > this.lastEpisode) {
            this.curAgentDatasets.stepEpisodeSeries.fireSeriesChanged();
        }
    }

    protected void updateRPESeries() {
        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.REWARD_PER_EPISODE)) {
            return;
        }

        int n = this.curTrial.cumulativeRewardInEpisode.size();
        for (int i = this.lastEpisode; i < n; i++) {
            this.curAgentDatasets.cumulativeRewardInEpisodeSeries.add((double) i, this.curTrial.cumulativeRewardInEpisode.get(i), false);
        }
        if (n > this.lastEpisode) {
            this.curAgentDatasets.cumulativeRewardInEpisodeSeries.fireSeriesChanged();
        }
    }

    protected void updateURESeries() {
        if (!this.metricsSet.contains(SelfManagementPerformanceMetric.USER_REACTION_PER_EPISODE)) {
            return;
        }

        int n = this.curTrial.cumulativeEpisodeUserReaction.size();
        for (int i = this.lastEpisode; i < n; i++) {
            this.curAgentDatasets.cumulativeEpisodeUserReactionSeries.add((double) i, this.curTrial.cumulativeEpisodeUserReaction.get(i), false);
        }
        if (n > this.lastEpisode) {
            this.curAgentDatasets.cumulativeEpisodeUserReactionSeries.fireSeriesChanged();
        }
    }

    /**
     * Computes the sum of the last entry in list and the value v and adds it to the end of list. Use for maintainly cumulative data.
     *
     * @param list the list to add and append to.
     * @param v    the value to add to the last value of list and append
     */
    protected static void accumulate(List<Double> list, double v) {
        if (list.size() > 0) {
            v += list.get(list.size() - 1);
        }
        list.add(v);
    }


    /**
     * Returns the minimum steps and episodes across all trials
     *
     * @param trials the trials to perform the min over
     * @return a double array of length 2; the first entry is the minimum steps, the second entry tthe minimum episodes
     */
    protected int[] minStepAndEpisodes(List<Trial> trials) {
        int minStep = Integer.MAX_VALUE;
        int minEpisode = Integer.MAX_VALUE;

        for (Trial t : trials) {
            minStep = Math.min(minStep, t.totalSteps);
            minEpisode = Math.min(minEpisode, t.totalEpisodes);
        }

        return new int[]{minStep, minEpisode};
    }


    /**
     * Returns the confidence interval for the specified significance level
     *
     * @param stats             the summary including the array of data for which the confidence interval is to be returned
     * @param significanceLevel the significance level required
     * @return a double array of length three in the form: {mean, lowerBound, upperBound}
     */
    public static double[] getCI(DescriptiveStatistics stats, double significanceLevel) {

        int n = (int) stats.getN();
        Double critD = cachedCriticalValues.get(n - 1);
        if (critD == null) {
            TDistribution tdist = new TDistribution(stats.getN() - 1);
            double crit = tdist.inverseCumulativeProbability(1. - (significanceLevel / 2.));
            critD = crit;
            cachedCriticalValues.put(n - 1, critD);
        }
        double crit = critD;
        double width = crit * stats.getStandardDeviation() / Math.sqrt(stats.getN());
        double m = stats.getMean();
        return new double[]{m, m - width, m + width};
    }


    /**
     * A datastructure for maintaining all the metric stats for a single trial.
     *
     * @author James MacGlashan
     */
    protected class Trial {

        /**
         * Stores the cumulative reward by step
         */
        public List<Double> cumulativeStepReward = new ArrayList<Double>();

        /**
         * Stores the cumulative reward by episode
         */
        public List<Double> cumulativeEpisodeReward = new ArrayList<Double>();

        /**
         * Stores the average reward by episode
         */
        public List<Double> averageEpisodeReward = new ArrayList<Double>();

        /**
         * Stores the median reward by episode
         */
        public List<Double> medianEpisodeReward = new ArrayList<Double>();


        /**
         * Stores the cumulative steps by episode
         */
        public List<Double> cumulativeStepEpisode = new ArrayList<Double>();

        /**
         * Stores the steps by episode
         */
        public List<Double> stepEpisode = new ArrayList<Double>();

        public List<Integer> cumulativeEpisodeUserReaction = new ArrayList<>();
        public List<Double> cumulativeRewardInEpisode = new ArrayList<>();

        /**
         * The cumulative reward of the episode so far
         */
        public double curEpisodeReward = 0.;

        /**
         * The number of steps in the episode so far
         */
        public int curEpisodeSteps = 0;
        public int currentEpisodeUserReaction = 0;


        /**
         * the total number of steps in the trial
         */
        public int totalSteps = 0;

        /**
         * The total number of episodes in the trial
         */
        public int totalEpisodes = 0;


        /**
         * A list of the reward sequence in the current episode
         */
        protected List<Double> curEpisodeRewards = new ArrayList<Double>();


        /**
         * Updates all datastructures with the reward received from the last step
         *
         * @param r the last reward received
         */
        public void stepIncrement(double r) {
            accumulate(this.cumulativeStepReward, r);
            this.curEpisodeReward += r;
            this.curEpisodeSteps++;
            this.curEpisodeRewards.add(r);
            if(r > 0)
                this.currentEpisodeUserReaction++;
        }


        /**
         * Completes the last episode and sets up the datastructures for the next episode
         */
        public void setupForNewEpisode() {
            accumulate(this.cumulativeEpisodeReward, this.curEpisodeReward);
            accumulate(this.cumulativeStepEpisode, this.curEpisodeSteps);

            double avgER = this.curEpisodeReward / (double) this.curEpisodeSteps;
            this.averageEpisodeReward.add(avgER);
            this.stepEpisode.add((double) this.curEpisodeSteps);
            this.cumulativeEpisodeUserReaction.add(this.currentEpisodeUserReaction);
            this.cumulativeRewardInEpisode.add(this.curEpisodeReward);

            Collections.sort(this.curEpisodeRewards);
            double med = 0.;
            if (this.curEpisodeSteps > 0) {
                int n2 = this.curEpisodeSteps / 2;
                if (this.curEpisodeSteps % 2 == 0) {
                    double m = this.curEpisodeRewards.get(n2);
                    double m2 = this.curEpisodeRewards.get(n2 - 1);
                    med = (m + m2) / 2.;
                } else {
                    med = this.curEpisodeRewards.get(n2);
                }
            }

            this.medianEpisodeReward.add(med);


            this.totalSteps += this.curEpisodeSteps;
            this.totalEpisodes++;

            this.curEpisodeReward = 0.;
            this.curEpisodeSteps = 0;
            this.currentEpisodeUserReaction = 0;
            this.curEpisodeRewards.clear();

        }

    }


    /**
     * A datastructure for maintain the plot series data in the current agent
     *
     * @author James MacGlashan
     */
    protected class AgentDatasets {


        /**
         * Most recent trial's cumulative reward per step series data
         */
        public XYSeries cumulativeStepRewardSeries;

        /**
         * Most recent trial's cumulative reward per step episode data
         */
        public XYSeries cumulativeEpisodeRewardSeries;

        /**
         * Most recent trial's average reward per step episode data
         */
        public XYSeries averageEpisodeRewardSeries;

        /**
         * Most recent trial's median reward per step episode data
         */
        public XYSeries medianEpisodeRewardSeries;


        /**
         * Most recent trial's cumulative steps per step episode data
         */
        public XYSeries cumulativeStepEpisodeSeries;

        /**
         * Most recent trial's steps per step episode data
         */
        public XYSeries stepEpisodeSeries;
        public XYSeries cumulativeRewardInEpisodeSeries;
        public XYSeries cumulativeEpisodeUserReactionSeries;

        /**
         * All trial's average cumulative reward per step series data
         */
        public YIntervalSeries csrAvgSeries;

        /**
         * All trial's average cumulative reward per episode series data
         */
        public YIntervalSeries cerAvgSeries;

        /**
         * All trial's average average reward per episode series data
         */
        public YIntervalSeries aerAvgSeries;

        /**
         * All trial's average median reward per episode series data
         */
        public YIntervalSeries merAvgSeries;

        /**
         * All trial's average cumulative steps per episode series data
         */
        public YIntervalSeries cseAvgSeries;

        /**
         * All trial's average steps per episode series data
         */
        public YIntervalSeries seAvgSeries;
        public YIntervalSeries rpeAvgSeries;
        public YIntervalSeries ureAvgSeries;


        /**
         * Initializes the datastructures for an agent with the given name
         */
        public AgentDatasets(String agentName) {
            this.cumulativeStepRewardSeries = new XYSeries(agentName);
            colCSR.addSeries(this.cumulativeStepRewardSeries);

            this.cumulativeEpisodeRewardSeries = new XYSeries(agentName);
            colCER.addSeries(this.cumulativeEpisodeRewardSeries);

            this.averageEpisodeRewardSeries = new XYSeries(agentName);
            colAER.addSeries(this.averageEpisodeRewardSeries);

            this.cumulativeStepEpisodeSeries = new XYSeries(agentName);
            colCSE.addSeries(this.cumulativeStepEpisodeSeries);

            this.stepEpisodeSeries = new XYSeries(agentName);
            colSE.addSeries(this.stepEpisodeSeries);

            this.medianEpisodeRewardSeries = new XYSeries(agentName);
            colMER.addSeries(this.medianEpisodeRewardSeries);

            this.cumulativeRewardInEpisodeSeries = new XYSeries(agentName);
            colRPE.addSeries(this.cumulativeRewardInEpisodeSeries);

            this.cumulativeEpisodeUserReactionSeries = new XYSeries(agentName);
            colURE.addSeries(this.cumulativeEpisodeUserReactionSeries);


            this.csrAvgSeries = new YIntervalSeries(agentName);
            this.csrAvgSeries.setNotify(false);
            colCSRAvg.addSeries(this.csrAvgSeries);

            this.cerAvgSeries = new YIntervalSeries(agentName);
            this.cerAvgSeries.setNotify(false);
            colCERAvg.addSeries(this.cerAvgSeries);

            this.aerAvgSeries = new YIntervalSeries(agentName);
            this.aerAvgSeries.setNotify(false);
            colAERAvg.addSeries(this.aerAvgSeries);

            this.merAvgSeries = new YIntervalSeries(agentName);
            this.merAvgSeries.setNotify(false);
            colMERAvg.addSeries(this.merAvgSeries);

            this.cseAvgSeries = new YIntervalSeries(agentName);
            this.cseAvgSeries.setNotify(false);
            colCSEAvg.addSeries(this.cseAvgSeries);

            this.seAvgSeries = new YIntervalSeries(agentName);
            this.seAvgSeries.setNotify(false);
            colSEAvg.addSeries(this.seAvgSeries);

            this.rpeAvgSeries = new YIntervalSeries(agentName);
            this.rpeAvgSeries.setNotify(false);
            colRPEAvg.addSeries(this.rpeAvgSeries);

            this.ureAvgSeries = new YIntervalSeries(agentName);
            this.ureAvgSeries.setNotify(false);
            colUREAvg.addSeries(this.ureAvgSeries);
        }


        /**
         * clears all the series data for the most recent trial.
         */
        public void clearNonAverages() {
            this.cumulativeStepRewardSeries.clear();
            this.cumulativeEpisodeRewardSeries.clear();
            this.averageEpisodeRewardSeries.clear();
            this.medianEpisodeRewardSeries.clear();
            this.cumulativeStepEpisodeSeries.clear();
            this.stepEpisodeSeries.clear();
            this.medianEpisodeRewardSeries.clear();
            this.cumulativeRewardInEpisodeSeries.clear();
            this.cumulativeEpisodeUserReactionSeries.clear();
        }


        /**
         * Causes all average trial data series to tell their plots that they've updated and need to be refreshed
         */
        public void fireAllAverages() {
            this.csrAvgSeries.setNotify(true);
            this.csrAvgSeries.fireSeriesChanged();
            this.csrAvgSeries.setNotify(false);

            this.cerAvgSeries.setNotify(true);
            this.cerAvgSeries.fireSeriesChanged();
            this.cerAvgSeries.setNotify(false);

            this.aerAvgSeries.setNotify(true);
            this.aerAvgSeries.fireSeriesChanged();
            this.aerAvgSeries.setNotify(false);

            this.merAvgSeries.setNotify(true);
            this.merAvgSeries.fireSeriesChanged();
            this.merAvgSeries.setNotify(false);

            this.cseAvgSeries.setNotify(true);
            this.cseAvgSeries.fireSeriesChanged();
            this.cseAvgSeries.setNotify(false);

            this.seAvgSeries.setNotify(true);
            this.seAvgSeries.fireSeriesChanged();
            this.seAvgSeries.setNotify(false);

            this.rpeAvgSeries.setNotify(true);
            this.rpeAvgSeries.fireSeriesChanged();
            this.rpeAvgSeries.setNotify(false);

            this.ureAvgSeries.setNotify(true);
            this.ureAvgSeries.fireSeriesChanged();
            this.ureAvgSeries.setNotify(false);
        }


    }


    /**
     * A class for a mutable boolean
     *
     * @author James MacGlashan
     */
    protected class MutableBoolean {

        /**
         * The boolean value
         */
        public boolean b;

        /**
         * Initializes with the given Boolean value
         *
         * @param b
         */
        public MutableBoolean(boolean b) {
            this.b = b;
        }
    }


}
