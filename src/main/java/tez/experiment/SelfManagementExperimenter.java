package tez.experiment;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.performance.ExperimentalEnvironment;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentServer;
import tez.experiment.performance.SelfManagementPerformanceMetric;
import tez.experiment.performance.SelfManagementRewardPlotter;
import tez.simulator.context.DayType;
import tez.simulator.context.Location;

import java.text.DecimalFormat;

import static tez.algorithm.SelfManagementDomainGenerator.*;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class SelfManagementExperimenter {


    /**
     * The test {@link burlap.oomdp.singleagent.environment.Environment} in which experiments will be performed.
     */
    protected Environment testEnvironment;


    /**
     * The {@link burlap.oomdp.singleagent.environment.EnvironmentServer} that wraps the test {@link burlap.oomdp.singleagent.environment.Environment}
     * and tells a {@link burlap.behavior.singleagent.auxiliary.performance.PerformancePlotter} about the individual interactions.
     */
    protected EnvironmentServer environmentSever;


    /**
     * The array of agent factories for the agents to be compared.
     */
    protected LearningAgentFactory[] agentFactories;


    /**
     * The number of trials that each agent is evaluated
     */
    protected int nTrials;


    /**
     * The length of each trial
     */
    protected int trialLength;


    /**
     * Whether the trial length specifies a number of episodes (which is the default) or the total number of steps
     */
    protected boolean trialLengthIsInEpisodes = true;


    /**
     * The PerformancePlotter used to collect and plot results
     */
    protected SelfManagementRewardPlotter plotter = null;


    /**
     * Whether the performance should be visually plotted (by default they will)
     */
    protected boolean displayPlots = true;


    /**
     * The delay in milliseconds between autmatic refreshes of the plots
     */
    protected int plotRefresh = 1000;


    /**
     * The signficance value for the confidence interval in the plots. The default is 0.05 which correspodns to a 95% CI
     */
    protected double plotCISignificance = 0.05;


    /**
     * Whether the experimenter has completed.
     */
    protected boolean completedExperiment = false;


    /**
     * The debug code used for debug printing. This experimenter will print with the debugger the number of trials completed for each agent.
     */
    public int debugCode = 63634013;


    private static DecimalFormat qValPrecision = new DecimalFormat("#0.0000");


    /**
     * Initializes.
     * The trialLength will be interpreted as the number of episodes, but it can be reinterpreted as a total number of steps per trial using the
     * {@link #toggleTrialLengthInterpretation(boolean)}.
     *
     * @param testEnvironment the test {@link burlap.oomdp.singleagent.environment.Environment} in which experiments will be performed.
     * @param nTrials         the number of trials
     * @param trialLength     the length of the trials (by default in episodes, but can be intereted as maximum step length)
     * @param agentFactories  factories to generate the agents to be tested.
     */
    public SelfManagementExperimenter(Environment testEnvironment, int nTrials, int trialLength, LearningAgentFactory... agentFactories) {

        if (agentFactories.length == 0) {
            throw new RuntimeException("Zero agent factories provided. At least one must be given for an experiment");
        }

        this.testEnvironment = testEnvironment;
        this.nTrials = nTrials;
        this.trialLength = trialLength;
        this.agentFactories = agentFactories;
    }


    /**
     * Setsup the plotting confiruation.
     *
     * @param chartWidth      the width of each chart/plot
     * @param chartHeight     the height of each chart//plot
     * @param columns         the number of columns of the plots displayed. Plots are filled in columns first, then move down the next row.
     * @param maxWindowHeight the maximum window height allowed before a scroll view is used.
     * @param trialMode       which plots to use; most recent trial, average over all trials, or both. If both, the most recent plot will be inserted into the window first, then the average.
     * @param metrics         the metrics that should be plotted. The metrics will appear in the window in the order that they are specified (columns first)
     */
    public void setUpPlottingConfiguration(int chartWidth, int chartHeight, int columns, int maxWindowHeight, TrialMode trialMode, SelfManagementPerformanceMetric... metrics) {

        if (trialMode.averagesEnabled() && this.nTrials == 1) {
            trialMode = TrialMode.MOSTRECENTTTRIALONLY;
        }

        this.displayPlots = true;
        this.plotter = new SelfManagementRewardPlotter(this.agentFactories[0].getAgentName(), chartWidth, chartHeight, columns, maxWindowHeight, trialMode, metrics);
        this.plotter.setRefreshDelay(this.plotRefresh);
        this.plotter.setSignificanceForCI(this.plotCISignificance);
    }


    /**
     * Sets the delay in milliseconds between automatic plot refreshes
     *
     * @param delayInMS the delay in milliseconds
     */
    public void setPlotRefreshDelay(int delayInMS) {
        this.plotRefresh = delayInMS;
        if (this.plotter != null) {
            this.plotter.setRefreshDelay(delayInMS);
        }
    }


    /**
     * Sets the significance used for confidence intervals.
     * The default is 0.05 which corresponds to a 95% CI.
     *
     * @param significance the significance for confidence intervals to use
     */
    public void setPlotCISignificance(double significance) {
        this.plotCISignificance = significance;
        if (this.plotter != null) {
            this.plotter.setSignificanceForCI(significance);
        }
    }


    /**
     * Toggles whether plots should be displayed or not.
     *
     * @param shouldPlotResults if true, then plots will be displayed; if false plots will not be displayed.
     */
    public void toggleVisualPlots(boolean shouldPlotResults) {
        this.displayPlots = shouldPlotResults;
    }


    /**
     * Changes whether the trial length provided in the constructor is interpreted as the number of episodes or total number of steps.
     *
     * @param lengthRepresentsEpisodes if true, interpret length as number of episodes; if false interprete as total number of steps.
     */
    public void toggleTrialLengthInterpretation(boolean lengthRepresentsEpisodes) {
        this.trialLengthIsInEpisodes = lengthRepresentsEpisodes;
    }


    /**
     * Starts the experiment and runs all trails for all agents.
     */
    public void startExperiment() {

        if (this.completedExperiment) {
            System.out.println("Experiment was already run and has completed. If you want to run a new experiment create a new Experiment object.");
            return;
        }

        if (this.plotter == null) {

            TrialMode trialMode = TrialMode.MOSTRECENTANDAVERAGE;
            if (this.nTrials == 1) {
                trialMode = TrialMode.MOSTRECENTTTRIALONLY;
            }

            this.plotter = new SelfManagementRewardPlotter(this.agentFactories[0].getAgentName(), 500, 250, 2, 500, trialMode);

        }


        //this.domain.addActionObserverForAllAction(plotter);
        this.environmentSever = new EnvironmentServer(this.testEnvironment, plotter);

        if (this.displayPlots) {
            this.plotter.startGUI();
        }

        for (int i = 0; i < this.agentFactories.length; i++) {

            if (i > 0) {
                this.plotter.startNewAgent(this.agentFactories[i].getAgentName());
            }

            if (this.testEnvironment instanceof ExperimentalEnvironment) {
                ((ExperimentalEnvironment) this.testEnvironment).startNewExperiment();
            }
            for (int j = 0; j < this.nTrials; j++) {

                DPrint.cl(this.debugCode, "Beginning " + this.agentFactories[i].getAgentName() + " trial " + (j + 1) + "/" + this.nTrials);

                if (this.trialLengthIsInEpisodes) {
                    this.runEpisodeBoundTrial(this.agentFactories[i]);
                } else {
                    this.runStepBoundTrial(this.agentFactories[i]);
                }
            }

        }

        this.plotter.endAllAgents();

        this.completedExperiment = true;

    }

    /**
     * Runs a trial for an agent generated by the given factory when interpreting trial length as a number of episodes.
     *
     * @param agentFactory the agent factory used to generate the agent to test.
     */
    protected void runEpisodeBoundTrial(LearningAgentFactory agentFactory) {

        //temporarily disable plotter data collection to avoid possible contamination for any actions taken by the agent generation
        //(e.g., if there is pre-test training)
        this.plotter.toggleDataCollection(false);

        LearningAgent agent = agentFactory.generateAgent();

        this.plotter.toggleDataCollection(true); //turn it back on to begin

        this.plotter.startNewTrial();

        for (int i = 0; i < this.trialLength; i++) {
            QValueEpisodeAnalysis ea = (QValueEpisodeAnalysis) agent.runLearningEpisode(this.environmentSever);
            this.plotter.endEpisode();
            this.environmentSever.resetEnvironment();

            System.out.println("Episode " + i);

            for (int j = 0; j < ea.rewardSequence.size(); j++) {
                ObjectInstance o = ea.stateSequence.get(j).getObjectsOfClass(CLASS_STATE).get(0);
                int hourOfDay = o.getIntValForAttribute(ATT_HOUR_OF_DAY);
                Location location = Location.values()[o.getIntValForAttribute(ATT_LOCATION)];
                DayType dayType = DayType.values()[o.getIntValForAttribute(ATT_DAY_TYPE)];
                if (hourOfDay > 20) {
                    int actionNo;
                    System.out.print("(" + hourOfDay + ", " + location + ", " + dayType + ") (");
                    for (QValue qv : ea.qValuesForStates.get(j)) {
                        actionNo = qv.a.actionName().equals(ACTION_INT_DELIVERY) ? 1 : 0;
                        System.out.print(actionNo + ": " + qValPrecision.format(qv.q) + ", ");
                    }
                    actionNo = ea.actionSequence.get(j).actionName().equals(ACTION_INT_DELIVERY) ? 1 : 0;
                    System.out.println(") A:" + actionNo + ", R:" + ea.rewardSequence.get(j));
                }
            }
        }

        this.plotter.endTrial();

    }


    /**
     * Runs a trial for an agent generated by the given factor when interpreting trial length as a number of total steps.
     *
     * @param agentFactory the agent factory used to generate the agent to test.
     */
    protected void runStepBoundTrial(LearningAgentFactory agentFactory) {

        //temporarily disable plotter data collection to avoid possible contamination for any actions taken by the agent generation
        //(e.g., if there is pre-test training)
        this.plotter.toggleDataCollection(false);

        LearningAgent agent = agentFactory.generateAgent();

        this.plotter.toggleDataCollection(true); //turn it back on to begin

        this.plotter.startNewTrial();

        int stepsRemaining = this.trialLength;
        while (stepsRemaining > 0) {
            EpisodeAnalysis ea = agent.runLearningEpisode(this.environmentSever, stepsRemaining);
            stepsRemaining -= ea.numTimeSteps() - 1; //-1  because we want to subtract the number of actions, not the number of states seen
            this.plotter.endEpisode();
            this.environmentSever.resetEnvironment();
        }

        this.plotter.endTrial();

    }


}
