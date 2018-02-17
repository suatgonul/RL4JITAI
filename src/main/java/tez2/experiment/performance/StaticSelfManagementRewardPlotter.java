package tez2.experiment.performance;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.oomdp.singleagent.GroundedAction;
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
import org.joda.time.LocalTime;
import tez2.domain.DomainConfig;
import tez2.environment.context.*;
import tez2.experiment.performance.js.JsEpisodeAnalysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by suatgonul on 4/27/2017.
 */
public class StaticSelfManagementRewardPlotter extends JFrame {

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
     * All agent plot series for the average of all trial's cumulative reward per step
     */
    public YIntervalSeriesCollection allAgents_cumulativeRewardInAllStepsAvg;

    /**
     * All agent plot series for the average of all trial's cumulative reward per episode
     */
    public YIntervalSeriesCollection allAgents_cumulativeRewardInAllEpisodesAvg;

    /**
     * All agent plot series for the average of all trial's average reward per episode
     */
    public YIntervalSeriesCollection allAgents_averageRewardInEachEpisodeAvg;

    /**
     * All agent plot series for the average of all trial's median reward per episode
     */
    public YIntervalSeriesCollection allAgents_medianRewardInEachEpisodeAvg;

    /**
     * All agent plot series for the average of all trial's cumulative steps per episode
     */
    public YIntervalSeriesCollection allAgents_cumulativeStepsInAllEpisodesAvg;

    /**
     * All agent plot series for the average of all trial's steps per episode
     */
    public YIntervalSeriesCollection allAgents_stepsInEachEpisodeAvg;

    public YIntervalSeriesCollection allAgents_rewardInEachEpisodeAvg;
    public YIntervalSeriesCollection allAgents_reactionInEachEpisodeAvg;
    public YIntervalSeriesCollection allAgents_cumulativeReactionInAllEpisodesAvg;

    /**
     * MI Paper related collections
     */
    public YIntervalSeriesCollection allAgents_ratioOfJitaisPerTimeOfDay; //Hypothesis 3
    public YIntervalSeriesCollection allAgents_totalJitaisPerEpisode; //Hypothesis 2 and 5
    public XYSeriesCollection allAgents_totalNumberOfJitaisTypes; //Hypothesis 3
    public XYSeriesCollection allAgents_totalNumberOfJitaisPerTimeOfDay;
    public XYSeriesCollection allAgents_ratioOfJitaisPhysicalActivity; // Hypothesis 4
    public XYSeriesCollection allAgents_ratioOfJitaisPhoneUsage;
    public XYSeriesCollection allAgents_ratioOfJitaisLocation;
    public XYSeriesCollection allAgents_ratioOfJitaisEmotionalStatus;


    /**
     * A set specifying the performance metrics that will be plotted
     */
    protected Set<SelfManagementPerformanceMetric> metricsSet = new HashSet<SelfManagementPerformanceMetric>();

    /**
     * specifies whether the most recent trial, average of all trials, or both plots will be displayed
     */
    protected TrialMode trialMode;

    /**
     * The last time step at which the plots' series data was updated
     */
    protected int lastTimeStepUpdate = 0;

    /**
     * The last episode at which the plot's series data was updated
     */
    protected int lastEpisode = 0;


    /**
     * the current episode that was recorded
     */
    protected int curEpisode = 0;

    /**
     * the significance level used for confidence intervals. The default is 0.05 (corresponding to a 95% CI).
     */
    protected double significance = 0.05;


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
    public StaticSelfManagementRewardPlotter(String firstAgentName, int chartWidth, int chartHeight, int columns, int maxWindowHeight,
                                             TrialMode trialMode, SelfManagementPerformanceMetric... metrics) {

        this.curAgentName = firstAgentName;

        this.agentTrials = new HashMap<String, List<Trial>>();
        this.agentTrials.put(this.curAgentName, new ArrayList<tez2.experiment.performance.StaticSelfManagementRewardPlotter.Trial>());

        allAgents_cumulativeRewardInAllStepsAvg = new YIntervalSeriesCollection();
        allAgents_cumulativeRewardInAllEpisodesAvg = new YIntervalSeriesCollection();
        allAgents_averageRewardInEachEpisodeAvg = new YIntervalSeriesCollection();
        allAgents_medianRewardInEachEpisodeAvg = new YIntervalSeriesCollection();
        allAgents_cumulativeStepsInAllEpisodesAvg = new YIntervalSeriesCollection();
        allAgents_stepsInEachEpisodeAvg = new YIntervalSeriesCollection();
        allAgents_rewardInEachEpisodeAvg = new YIntervalSeriesCollection();
        allAgents_reactionInEachEpisodeAvg = new YIntervalSeriesCollection();
        allAgents_cumulativeReactionInAllEpisodesAvg = new YIntervalSeriesCollection();
        allAgents_ratioOfJitaisPerTimeOfDay = new YIntervalSeriesCollection();

        allAgents_totalJitaisPerEpisode = new YIntervalSeriesCollection();
        allAgents_totalNumberOfJitaisTypes = new XYSeriesCollection();
        allAgents_totalNumberOfJitaisPerTimeOfDay = new XYSeriesCollection();
        allAgents_ratioOfJitaisPhysicalActivity = new XYSeriesCollection();
        allAgents_ratioOfJitaisPhoneUsage = new XYSeriesCollection();
        allAgents_ratioOfJitaisLocation = new XYSeriesCollection();
        allAgents_ratioOfJitaisEmotionalStatus = new XYSeriesCollection();

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
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Reward", "Time Step", "Cumulative Reward", allAgents_cumulativeRewardInAllStepsAvg);
            } else if (m == SelfManagementPerformanceMetric.CUMULATIVE_REWARD_PER_EPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Reward", "Episode", "Cumulative Reward", allAgents_cumulativeRewardInAllEpisodesAvg);
            } else if (m == SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Average Reward", "Episode", "Average Reward", allAgents_averageRewardInEachEpisodeAvg);
            } else if (m == SelfManagementPerformanceMetric.MEDIANEPISODEREWARD) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Median Reward", "Episode", "Median Reward", allAgents_medianRewardInEachEpisodeAvg);
            } else if (m == SelfManagementPerformanceMetric.CUMULATIVESTEPSPEREPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Steps", "Episode", "Cumulative Steps", allAgents_cumulativeStepsInAllEpisodesAvg);
            } else if (m == SelfManagementPerformanceMetric.STEPSPEREPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Number of Steps", "Episode", "Number of Steps", allAgents_stepsInEachEpisodeAvg);
            } else if (m == SelfManagementPerformanceMetric.REWARD_PER_EPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Episode Reward", "Episode", "Episode Reward", allAgents_rewardInEachEpisodeAvg);
            } else if (m == SelfManagementPerformanceMetric.USER_REACTION_PER_EPISODE) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Episode User Reaction", "Episode", "Episode User Reaction", allAgents_reactionInEachEpisodeAvg);
            } else if (m == SelfManagementPerformanceMetric.CUMULATIVE_REACTION) {
                this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Reaction", "Episode", "Cumulative Reaction", allAgents_cumulativeReactionInAllEpisodesAvg);
            }
        }

        /*int totalChartHeight = ((metrics.length / columns) + 1) * (chartHeight + 10);
        if (totalChartHeight > maxWindowHeight) {
            JScrollPane scrollPane = new JScrollPane(plotContainer);
            scrollPane.setPreferredSize(new Dimension(chartWidth * columns + 50, maxWindowHeight));
            this.add(scrollPane);
        } else {
            this.add(plotContainer);
        }*/
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
     * Launches the GUI and automatic refresh thread.
     */
    public void startGUI() {
//        this.pack();
//        this.setVisible(true);
    }

    public void populateAgentDatasets(EpisodeAnalysis ea) {
        for (int i = 0; i < ea.rewardSequence.size(); i++) {
            this.curTrial.stepIncrement(ea.rewardSequence.get(i), ea.actionSequence.get(i), ((OmiEpisodeAnalysis) ea).stateTimes.get(i), ((OmiEpisodeAnalysis) ea).userContexts.get(i));
        }

        JsEpisodeAnalysis jsEa = ((OmiEpisodeAnalysis) ea).jsEpisodeAnalysis;
        for (int i =0; i<jsEa.rewardSequence.size(); i++) {
            this.curTrial.jsStepIncrement(jsEa.actionSequence.get(i));
        }
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
        this.curTrial = new Trial();
        this.lastTimeStepUpdate = 0;
        this.lastEpisode = 0;
        this.curEpisode = 0;
    }

    /**
     * Informs the plotter that all data for the current trial as been collected.
     */
    public void endTrial() {
        this.agentTrials.get(this.curAgentName).add(curTrial);
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
                synchronized (this) {
                    endTrialsForCurrentAgent();
                    curAgentName = agentName;
                    agentTrials.put(curAgentName, new ArrayList<StaticSelfManagementRewardPlotter.Trial>());
                    curAgentDatasets = new AgentDatasets(curAgentName);
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
                synchronized (this) {
                    endTrialsForCurrentAgent();
                }
            }
        });
    }

    /**
     * Adds the most recent trial (if enabled) chart and trial average (if enabled) chart into the provided container.
     * The GridBagConstraints will aumatically be incremented to the next position after this method returns.
     *
     * @param plotContainer     the contain in which to insert the plot(s).
     * @param c                 the current grid bag contraint locaiton in which the plots should be inserted.
     * @param columns           the number of columns to fill in the plot container
     * @param chartWidth        the width of any single plot
     * @param chartHeight       the height of any single plot
     * @param title             the title to label thep plot; if average trial plots are enabled the word "Average" will be prepended to the title for the average plot.
     * @param xlab              the xlab axis of the plot
     * @param ylab              the y lab axis of the plot
     * @param averageCollection the YIntervalSeriesCollection dataset with which the trial average plot is associated
     */
    protected void insertChart(Container plotContainer, GridBagConstraints c, int columns, int chartWidth, int chartHeight,
                               String title, String xlab, String ylab, YIntervalSeriesCollection averageCollection) {

        if (this.trialMode.averagesEnabled()) {
            final JFreeChart chartCSRAvg = ChartFactory.createXYLineChart("Average " + title, xlab, ylab, averageCollection);
            ((XYPlot) chartCSRAvg.getPlot()).setRenderer(this.createDeviationRenderer());
            ChartPanel chartPanelCSRAvg = new ChartPanel(chartCSRAvg);
            chartPanelCSRAvg.setPreferredSize(new Dimension(chartWidth, chartHeight));
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
     * Informs the plotter that all trials for the current agent have been collected and causes the average plots to be set and displayed.
     */
    protected void endTrialsForCurrentAgent() {

        final String aName = this.curAgentName;

        if (!this.trialMode.averagesEnabled()) {
            return;
        }


        List<Trial> trials = agentTrials.get(aName);
        int[] n = minStepAndEpisodes(trials);


        if (this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVEREWARDPERSTEP)) {
            for (int i = 0; i < n[0]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_cumulativeStepReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_cumulativeRewardInAllStepsAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }


        if (this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVE_REWARD_PER_EPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_cumulativeEpisodeReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_cumulativeRewardInAllEpisodesAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }


        if (this.metricsSet.contains(SelfManagementPerformanceMetric.AVERAGEEPISODEREWARD)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_averageEpisodeReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_averageRewardInEachEpisodeAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.MEDIANEPISODEREWARD)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_medianEpisodeReward.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_medianRewardInEachEpisodeAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVESTEPSPEREPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_cumulativeStepEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_cumulativeStepsInAllEpisodesAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.STEPSPEREPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_stepEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_stepsInEachEpiodesAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.REWARD_PER_EPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_rewardInEachEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_rewardInEachEpisodeAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.USER_REACTION_PER_EPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_userReactionInEachEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_reactionInEachEpisodeAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.CUMULATIVE_REACTION)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_cumulativeReactionInAllEpisodes.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_cumulativeReactionInallEpisodesAvgSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }

        // MI Paper-related additions
        if (this.metricsSet.contains(SelfManagementPerformanceMetric.AVG_TOTAL_JITAIS_PER_EPISODE)) {
            for (int i = 0; i < n[1]; i++) {
                DescriptiveStatistics avgi = new DescriptiveStatistics();
                for (Trial t : trials) {
                    avgi.addValue(t.trialRawData_jitaiCountEachEpisode.get(i));
                }
                double[] ci = getCI(avgi, this.significance);
                curAgentDatasets.agentDataset_jitaiCountEachEpisodeSeries.add(i, ci[0], ci[1], ci[2]);
            }
        }
        if (this.metricsSet.contains(SelfManagementPerformanceMetric.TOTAL_NUMBER_OF_JITAI_TYPES)) {
            int totalCountInTrials;
            for (int i = 0; i < 3; i++) {
                totalCountInTrials = 0;
                for (Trial t : trials) {
                    totalCountInTrials += t.trialRawData_jitaiTypeCountsInTrial.get(i);
                }
                curAgentDatasets.agentDataset_totalCountOfJitaiTypes.add(i, totalCountInTrials);
            }
        }

        if (this.metricsSet.contains(SelfManagementPerformanceMetric.RATIO_JITAIS_PER_STATE_PARAM)) {
            int totalCountInTrials;
            // time of day
            for (int i = 0; i < 96; i++) {
                totalCountInTrials = 0;
                for (Trial t : trials) {
                    totalCountInTrials += t.trialRawData_jitaiCountPerTimeOfDayInTrial.get(i);
                }
                curAgentDatasets.agentDataset_totalCountOfJitaisPerTimeOfDay.add(i, totalCountInTrials);
            }

            // physical activity
            for (int i = 0; i < PhysicalActivity.values().length; i++) {
                totalCountInTrials = 0;
                for (Trial t : trials) {
                    totalCountInTrials += t.trialRawData_jitaiCountPerPhysicalActivityInTrial.get(i);
                }
                curAgentDatasets.agentDataset_totalCountOfJitaisPerPhysicalActivity.add(i, totalCountInTrials);
            }

            // location
            for (int i = 0; i < Location.values().length; i++) {
                totalCountInTrials = 0;
                for (Trial t : trials) {
                    totalCountInTrials += t.trialRawData_jitaiCountPerLocationInTrial.get(i);
                }
                curAgentDatasets.agentDataset_totalCountOfJitaisPerLocation.add(i, totalCountInTrials);
            }

            // phone usage
            for (int i = 0; i < PhoneUsage.values().length; i++) {
                totalCountInTrials = 0;
                for (Trial t : trials) {
                    totalCountInTrials += t.trialRawData_jitaiCountPerPhoneUsageInTrial.get(i);
                }
                curAgentDatasets.agentDataset_totalCountOfJitaisPerPhoneUsage.add(i, totalCountInTrials);
            }

            // emotional status
            for (int i = 0; i < EmotionalStatus.values().length; i++) {
                totalCountInTrials = 0;
                for (Trial t : trials) {
                    totalCountInTrials += t.trialRawData_jitaiCountPerEmotionalStatusInTrial.get(i);
                }
                curAgentDatasets.agentDataset_totalCountOfJitaisPerEmotionalStatus.add(i, totalCountInTrials);
            }
        }

        curAgentDatasets.fireAllAverages();
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
        public List<Double> trialRawData_cumulativeStepReward = new ArrayList<Double>();

        /**
         * Stores the cumulative reward by episode
         */
        public List<Double> trialRawData_cumulativeEpisodeReward = new ArrayList<Double>();

        /**
         * Stores the average reward by episode
         */
        public List<Double> trialRawData_averageEpisodeReward = new ArrayList<Double>();

        /**
         * Stores the median reward by episode
         */
        public List<Double> trialRawData_medianEpisodeReward = new ArrayList<Double>();

        /**
         * Stores the cumulative steps by episode
         */
        public List<Double> trialRawData_cumulativeStepEpisode = new ArrayList<Double>();

        /**
         * Stores the steps by episode
         */
        public List<Double> trialRawData_stepEpisode = new ArrayList<Double>();

        public List<Integer> trialRawData_userReactionInEachEpisode = new ArrayList<>();
        public List<Double> trialRawData_rewardInEachEpisode = new ArrayList<>();
        public List<Double> trialRawData_cumulativeReactionInAllEpisodes = new ArrayList<>();

        /**
         * MI paper related data
         */
        public List<Integer> trialRawData_jitaiCountEachEpisode = new ArrayList<>();
        public List<Integer> trialRawData_jitaiTypeCountsInTrial = new ArrayList<>();
        public List<Integer> trialRawData_jitaiCountPerTimeOfDayInTrial = new ArrayList<>();
        public List<Integer> trialRawData_jitaiCountPerPhysicalActivityInTrial = new ArrayList<>();
        public List<Integer> trialRawData_jitaiCountPerLocationInTrial = new ArrayList<>();
        public List<Integer> trialRawData_jitaiCountPerPhoneUsageInTrial = new ArrayList<>();
        public List<Integer> trialRawData_jitaiCountPerEmotionalStatusInTrial = new ArrayList<>();


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
         * MI-related
         */
        public int curEpisodeJitais = 0;


        public Trial() {
            // initialize jitai type count list
            for(int i=0; i<3; i++) {
                trialRawData_jitaiTypeCountsInTrial.add(0);
            }

            // initialize time of day count list
            for(int i=0; i<96; i++) {
                trialRawData_jitaiCountPerTimeOfDayInTrial.add(0);
            }

            // initialize physical activity count list
            for(int i=0; i<PhysicalActivity.values().length; i++) {
                trialRawData_jitaiCountPerPhysicalActivityInTrial.add(0);
            }

            // initialize location count list
            for(int i=0; i<Location.values().length; i++) {
                trialRawData_jitaiCountPerLocationInTrial.add(0);
            }

            // initialize phone usage count list
            for(int i=0; i<PhoneUsage.values().length; i++) {
                trialRawData_jitaiCountPerPhoneUsageInTrial.add(0);
            }

            // initialize emotional status count list
            for(int i=0; i<EmotionalStatus.values().length; i++) {
                trialRawData_jitaiCountPerEmotionalStatusInTrial.add(0);
            }
        }

        /**
         * Updates all datastructures with
         */
        public void stepIncrement(double r, GroundedAction a, LocalTime stateTime, Context context) {
            accumulate(this.trialRawData_cumulativeStepReward, r);
            this.curEpisodeReward += r;
            this.curEpisodeSteps++;
            this.curEpisodeRewards.add(r);
            if (r > 0)
                this.currentEpisodeUserReaction++;

            int querterIndex = getQuarterStateRepresentation(stateTime);
            if(a.actionName().equals(DomainConfig.ACTION_SEND_JITAI)) {
                // populate time of day jitai counts
                trialRawData_jitaiCountPerTimeOfDayInTrial.set(querterIndex, trialRawData_jitaiCountPerTimeOfDayInTrial.get(querterIndex) + 1);
                // populate physical activity jitai counts
                trialRawData_jitaiCountPerPhysicalActivityInTrial.set(context.getPhysicalActivity().ordinal(), trialRawData_jitaiCountPerPhysicalActivityInTrial.get(context.getPhysicalActivity().ordinal()) + 1);
                // populate location jitai counts
                trialRawData_jitaiCountPerLocationInTrial.set(context.getLocation().ordinal(), trialRawData_jitaiCountPerLocationInTrial.get(context.getLocation().ordinal()) + 1);
                // populate phone usage jitai counts
                trialRawData_jitaiCountPerPhoneUsageInTrial.set(context.getPhoneUsage().ordinal(), trialRawData_jitaiCountPerPhoneUsageInTrial.get(context.getPhoneUsage().ordinal()) + 1);
                // populate emotional status jitai counts
                trialRawData_jitaiCountPerEmotionalStatusInTrial.set(context.getEmotionalStatus().ordinal(), trialRawData_jitaiCountPerEmotionalStatusInTrial.get(context.getEmotionalStatus().ordinal()) + 1);
            }
        }

        public void jsStepIncrement(GroundedAction action) {
            // MI-related
            if(!action.actionName().equals(DomainConfig.ACTION_NO_ACTION)) {
                curEpisodeJitais++;

                // populate jitai type counts
                if(action.actionName().equals(DomainConfig.ACTION_JITAI_1)) {
                    trialRawData_jitaiTypeCountsInTrial.set(0, trialRawData_jitaiTypeCountsInTrial.get(0) + 1);
                } else if(action.actionName().equals(DomainConfig.ACTION_JITAI_2)) {
                    trialRawData_jitaiTypeCountsInTrial.set(1, trialRawData_jitaiTypeCountsInTrial.get(1) + 1);
                } else if(action.actionName().equals(DomainConfig.ACTION_JITAI_3)) {
                    trialRawData_jitaiTypeCountsInTrial.set(2, trialRawData_jitaiTypeCountsInTrial.get(2) + 1);
                }
            }
        }


        /**
         * Completes the last episode and sets up the datastructures for the next episode
         */
        public void setupForNewEpisode() {
            accumulate(this.trialRawData_cumulativeEpisodeReward, this.curEpisodeReward);
            accumulate(this.trialRawData_cumulativeStepEpisode, this.curEpisodeSteps);

            double avgER = this.curEpisodeReward / (double) this.curEpisodeSteps;
            this.trialRawData_averageEpisodeReward.add(avgER);
            this.trialRawData_stepEpisode.add((double) this.curEpisodeSteps);
            this.trialRawData_userReactionInEachEpisode.add(this.currentEpisodeUserReaction);
            this.trialRawData_rewardInEachEpisode.add(this.curEpisodeReward);
            accumulate(this.trialRawData_cumulativeReactionInAllEpisodes, this.currentEpisodeUserReaction);

            //MI-related
            this.trialRawData_jitaiCountEachEpisode.add(this.curEpisodeJitais);

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

            this.trialRawData_medianEpisodeReward.add(med);


            this.totalSteps += this.curEpisodeSteps;
            this.totalEpisodes++;

            this.curEpisodeReward = 0.;
            this.curEpisodeSteps = 0;
            this.currentEpisodeUserReaction = 0;
            this.curEpisodeRewards.clear();

            //MI-related
            this.curEpisodeJitais = 0;
        }

    }

    protected int getQuarterStateRepresentation(LocalTime stateTime) {
        int minute = stateTime.getHourOfDay() * 60 + stateTime.getMinuteOfHour();
        int quarterIndex = minute / 15;
        return quarterIndex;
    }


    /**
     * A datastructure for maintain the plot series data in the current agent
     *
     * @author James MacGlashan
     */
    protected class AgentDatasets {
        /**
         * All trial's average cumulative reward per step series data
         */
        public YIntervalSeries agentDataset_cumulativeRewardInAllStepsAvgSeries;

        /**
         * All trial's average cumulative reward per episode series data
         */
        public YIntervalSeries agentDataset_cumulativeRewardInAllEpisodesAvgSeries;

        /**
         * All trial's average average reward per episode series data
         */
        public YIntervalSeries agentDataset_averageRewardInEachEpisodeAvgSeries;

        /**
         * All trial's average median reward per episode series data
         */
        public YIntervalSeries agentDataset_medianRewardInEachEpisodeAvgSeries;

        /**
         * All trial's average cumulative steps per episode series data
         */
        public YIntervalSeries agentDataset_cumulativeStepsInAllEpisodesAvgSeries;

        /**
         * All trial's average steps per episode series data
         */
        public YIntervalSeries agentDataset_stepsInEachEpiodesAvgSeries;

        public YIntervalSeries agentDataset_rewardInEachEpisodeAvgSeries;
        public YIntervalSeries agentDataset_reactionInEachEpisodeAvgSeries;
        public YIntervalSeries agentDataset_cumulativeReactionInallEpisodesAvgSeries;

        /**
         * MI-paper related datasets
         */
        public YIntervalSeries agentDataset_jitaiCountEachEpisodeSeries;
        public XYSeries agentDataset_totalCountOfJitaiTypes;
        public XYSeries agentDataset_totalCountOfJitaisPerTimeOfDay;
        public XYSeries agentDataset_totalCountOfJitaisPerPhysicalActivity;
        public XYSeries agentDataset_totalCountOfJitaisPerPhoneUsage;
        public XYSeries agentDataset_totalCountOfJitaisPerLocation;
        public XYSeries agentDataset_totalCountOfJitaisPerEmotionalStatus;


        /**
         * Initializes the datastructures for an agent with the given name
         */
        public AgentDatasets(String agentName) {

            this.agentDataset_cumulativeRewardInAllStepsAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_cumulativeRewardInAllStepsAvgSeries.setNotify(false);
            allAgents_cumulativeRewardInAllStepsAvg.addSeries(this.agentDataset_cumulativeRewardInAllStepsAvgSeries);

            this.agentDataset_cumulativeRewardInAllEpisodesAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_cumulativeRewardInAllEpisodesAvgSeries.setNotify(false);
            allAgents_cumulativeRewardInAllEpisodesAvg.addSeries(this.agentDataset_cumulativeRewardInAllEpisodesAvgSeries);

            this.agentDataset_averageRewardInEachEpisodeAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_averageRewardInEachEpisodeAvgSeries.setNotify(false);
            allAgents_averageRewardInEachEpisodeAvg.addSeries(this.agentDataset_averageRewardInEachEpisodeAvgSeries);

            this.agentDataset_medianRewardInEachEpisodeAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_medianRewardInEachEpisodeAvgSeries.setNotify(false);
            allAgents_medianRewardInEachEpisodeAvg.addSeries(this.agentDataset_medianRewardInEachEpisodeAvgSeries);

            this.agentDataset_cumulativeStepsInAllEpisodesAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_cumulativeStepsInAllEpisodesAvgSeries.setNotify(false);
            allAgents_cumulativeStepsInAllEpisodesAvg.addSeries(this.agentDataset_cumulativeStepsInAllEpisodesAvgSeries);

            this.agentDataset_stepsInEachEpiodesAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_stepsInEachEpiodesAvgSeries.setNotify(false);
            allAgents_stepsInEachEpisodeAvg.addSeries(this.agentDataset_stepsInEachEpiodesAvgSeries);

            this.agentDataset_rewardInEachEpisodeAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_rewardInEachEpisodeAvgSeries.setNotify(false);
            allAgents_rewardInEachEpisodeAvg.addSeries(this.agentDataset_rewardInEachEpisodeAvgSeries);

            this.agentDataset_reactionInEachEpisodeAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_reactionInEachEpisodeAvgSeries.setNotify(false);
            allAgents_reactionInEachEpisodeAvg.addSeries(this.agentDataset_reactionInEachEpisodeAvgSeries);

            this.agentDataset_cumulativeReactionInallEpisodesAvgSeries = new YIntervalSeries(agentName);
            this.agentDataset_cumulativeReactionInallEpisodesAvgSeries.setNotify(false);
            allAgents_cumulativeReactionInAllEpisodesAvg.addSeries(this.agentDataset_cumulativeReactionInallEpisodesAvgSeries);


            // MI related
            this.agentDataset_jitaiCountEachEpisodeSeries = new YIntervalSeries(agentName);
            this.agentDataset_jitaiCountEachEpisodeSeries.setNotify(false);
            allAgents_totalJitaisPerEpisode.addSeries(this.agentDataset_jitaiCountEachEpisodeSeries);

            this.agentDataset_totalCountOfJitaiTypes = new XYSeries(agentName);
            this.agentDataset_totalCountOfJitaiTypes.setNotify(false);
            allAgents_totalNumberOfJitaisTypes.addSeries(this.agentDataset_totalCountOfJitaiTypes);

            this.agentDataset_totalCountOfJitaisPerTimeOfDay = new XYSeries(agentName);
            this.agentDataset_totalCountOfJitaisPerTimeOfDay.setNotify(false);
            allAgents_totalNumberOfJitaisPerTimeOfDay.addSeries(this.agentDataset_totalCountOfJitaisPerTimeOfDay);

            this.agentDataset_totalCountOfJitaisPerPhysicalActivity = new XYSeries(agentName);
            this.agentDataset_totalCountOfJitaisPerPhysicalActivity.setNotify(false);
            allAgents_ratioOfJitaisPhysicalActivity.addSeries(this.agentDataset_totalCountOfJitaisPerPhysicalActivity);

            this.agentDataset_totalCountOfJitaisPerLocation = new XYSeries(agentName);
            this.agentDataset_totalCountOfJitaisPerLocation.setNotify(false);
            allAgents_ratioOfJitaisLocation.addSeries(this.agentDataset_totalCountOfJitaisPerLocation);

            this.agentDataset_totalCountOfJitaisPerPhoneUsage= new XYSeries(agentName);
            this.agentDataset_totalCountOfJitaisPerPhoneUsage.setNotify(false);
            allAgents_ratioOfJitaisPhoneUsage.addSeries(this.agentDataset_totalCountOfJitaisPerPhoneUsage);

            this.agentDataset_totalCountOfJitaisPerEmotionalStatus = new XYSeries(agentName);
            this.agentDataset_totalCountOfJitaisPerEmotionalStatus.setNotify(false);
            allAgents_ratioOfJitaisEmotionalStatus.addSeries(this.agentDataset_totalCountOfJitaisPerEmotionalStatus);
        }


        /**
         * Causes all average trial data series to tell their plots that they've updated and need to be refreshed
         */
        public void fireAllAverages() {
            this.agentDataset_cumulativeRewardInAllStepsAvgSeries.setNotify(true);
            this.agentDataset_cumulativeRewardInAllStepsAvgSeries.fireSeriesChanged();
            this.agentDataset_cumulativeRewardInAllStepsAvgSeries.setNotify(false);

            this.agentDataset_cumulativeRewardInAllEpisodesAvgSeries.setNotify(true);
            this.agentDataset_cumulativeRewardInAllEpisodesAvgSeries.fireSeriesChanged();
            this.agentDataset_cumulativeRewardInAllEpisodesAvgSeries.setNotify(false);

            this.agentDataset_averageRewardInEachEpisodeAvgSeries.setNotify(true);
            this.agentDataset_averageRewardInEachEpisodeAvgSeries.fireSeriesChanged();
            this.agentDataset_averageRewardInEachEpisodeAvgSeries.setNotify(false);

            this.agentDataset_medianRewardInEachEpisodeAvgSeries.setNotify(true);
            this.agentDataset_medianRewardInEachEpisodeAvgSeries.fireSeriesChanged();
            this.agentDataset_medianRewardInEachEpisodeAvgSeries.setNotify(false);

            this.agentDataset_cumulativeStepsInAllEpisodesAvgSeries.setNotify(true);
            this.agentDataset_cumulativeStepsInAllEpisodesAvgSeries.fireSeriesChanged();
            this.agentDataset_cumulativeStepsInAllEpisodesAvgSeries.setNotify(false);

            this.agentDataset_stepsInEachEpiodesAvgSeries.setNotify(true);
            this.agentDataset_stepsInEachEpiodesAvgSeries.fireSeriesChanged();
            this.agentDataset_stepsInEachEpiodesAvgSeries.setNotify(false);

            this.agentDataset_rewardInEachEpisodeAvgSeries.setNotify(true);
            this.agentDataset_rewardInEachEpisodeAvgSeries.fireSeriesChanged();
            this.agentDataset_rewardInEachEpisodeAvgSeries.setNotify(false);

            this.agentDataset_reactionInEachEpisodeAvgSeries.setNotify(true);
            this.agentDataset_reactionInEachEpisodeAvgSeries.fireSeriesChanged();
            this.agentDataset_reactionInEachEpisodeAvgSeries.setNotify(false);

            this.agentDataset_cumulativeReactionInallEpisodesAvgSeries.setNotify(true);
            this.agentDataset_cumulativeReactionInallEpisodesAvgSeries.fireSeriesChanged();
            this.agentDataset_cumulativeReactionInallEpisodesAvgSeries.setNotify(false);
        }
    }
}
