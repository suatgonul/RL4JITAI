package tez2.experiment;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.performance.ExperimentalEnvironment;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentServer;
import org.apache.commons.io.FileUtils;
import tez2.algorithm.collaborative_learning.StateClassifier;
import tez2.algorithm.collaborative_learning.js.SparkJsStateClassifier;
import tez2.algorithm.collaborative_learning.omi.SparkOmiStateClassifier;
import tez2.environment.simulator.SimulatedWorld;
import tez2.experiment.debug.Reporter;
import tez2.experiment.performance.*;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class StaticSelfManagementExperimenter {


    private static DecimalFormat qValPrecision = new DecimalFormat("#0.0000");
    /**
     * The debug code used for debug printing. This experimenter will print with the debugger the number of trials completed for each agent.
     */
    public int debugCode = 63634013;
    /**
     * The test {@link Environment} in which experiments will be performed.
     */
    protected Environment testEnvironment;
    /**
     * The {@link EnvironmentServer} that wraps the test {@link Environment}
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
    protected StaticSelfManagementRewardPlotter plotter = null;
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
     * Initializes.
     * The trialLength will be interpreted as the number of episodes, but it can be reinterpreted as a total number of steps per trial using the
     * {@link #toggleTrialLengthInterpretation(boolean)}.
     *
     * @param testEnvironment the test {@link Environment} in which experiments will be performed.
     * @param nTrials         the number of trials
     * @param trialLength     the length of the trials (by default in episodes, but can be intereted as maximum step length)
     * @param agentFactories  factories to generate the agents to be tested.
     */
    public StaticSelfManagementExperimenter(Environment testEnvironment, int nTrials, int trialLength, LearningAgentFactory... agentFactories) {

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
        this.plotter = new StaticSelfManagementRewardPlotter(this.agentFactories[0].getAgentName(), chartWidth, chartHeight, columns, maxWindowHeight, trialMode, metrics);
        this.plotter.setSignificanceForCI(this.plotCISignificance);
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
    private int personaIndex;
    public void startExperiment(int personaIndex) {
        this.personaIndex = personaIndex;

        if (this.completedExperiment) {
            System.out.println("Experiment was already run and has completed. If you want to run a new experiment create a new Experiment object.");
            return;
        }

        if (this.plotter == null) {

            TrialMode trialMode = TrialMode.MOSTRECENTANDAVERAGE;
            if (this.nTrials == 1) {
                trialMode = TrialMode.MOSTRECENTTTRIALONLY;
            }

            this.plotter = new StaticSelfManagementRewardPlotter(this.agentFactories[0].getAgentName(), 500, 250, 2, 500, trialMode);

        }


        //this.domain.addActionObserverForAllAction(plotter);
        this.environmentSever = new EnvironmentServer(this.testEnvironment);

        if (this.displayPlots) {
            this.plotter.startGUI();
        }

        cleanOutputDirectory();
        for (int i = 0; i < this.agentFactories.length; i++) {

            if (i > 0) {
                this.plotter.startNewAgent(this.agentFactories[i].getAgentName());
            }

            if (this.testEnvironment instanceof ExperimentalEnvironment) {
                ((ExperimentalEnvironment) this.testEnvironment).startNewExperiment();
            }
            for (int j = 0; j < this.nTrials; j++) {

                //DPrint.cl(this.debugCode, "Beginning " + this.agentFactories[i].getAgentName() + " trial " + (j + 1) + "/" + this.nTrials);

                if (this.trialLengthIsInEpisodes) {
                    this.runEpisodeBoundTrial(this.agentFactories[i], j);
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
    protected void runEpisodeBoundTrial(LearningAgentFactory agentFactory, int trialNo) {

        LearningAgent agent = agentFactory.generateAgent();

        this.plotter.startNewTrial();

        List<SelfManagementEpisodeAnalysis> episodeAnalysisList = new ArrayList<>();
        Reporter reporter = new Reporter(Experiment.runId + "output/" + agentFactory.getAgentName() + ".txt");
        reporter.report("New Trial");
        StringBuilder sb;

        /*if (agentFactory.getAgentName().contains("colla")) {
            this.trialLength = 50;
        } else {
            this.trialLength = tempLength;
        }*/
        long trialStartTime = System.currentTimeMillis();
        long elapsedTrialTime = 0;
        for (int i = 0; i < this.trialLength; i++) {
            long episodeStarttime = System.currentTimeMillis();
            OmiEpisodeAnalysis ea = (OmiEpisodeAnalysis) agent.runLearningEpisode(this.environmentSever);
            ea.setTrialNo(trialNo);
            //if(personaIndex == 0) {
                if (i >= 0) {
                    ea.getJsEpisodeAnalysis().printEpisodeAnalysis();
                    System.out.println("Episode: " + (i + 1) + " completed in " + (System.currentTimeMillis() - episodeStarttime) + " milliseconds");
                    System.out.println("Elapsed trial time: " + elapsedTrialTime + " milliseconds");
                }
            //}


            episodeAnalysisList.add(ea);
            //if (agentFactory.getAgentName().contains("colla")) {
                elapsedTrialTime += (System.currentTimeMillis()-episodeStarttime);
                //System.out.println("Episode: " + (i + 1) + " completed in " + (System.currentTimeMillis()-episodeStarttime) + " milliseconds");
                //System.out.println("Elapsed trial time: " + elapsedTrialTime + " milliseconds");
            //}

            this.plotter.populateAgentDatasets(ea);
            this.plotter.endEpisode();
            this.environmentSever.resetEnvironment();
        }
        ((SimulatedWorld) this.environmentSever.getEnvironmentDelegate()).endTrial();
        //if(trialNo % 50 == 0) {
            System.out.println("Trial " + trialNo + " completed in " + (System.currentTimeMillis() - trialStartTime) + " milliseconds");
        //}


        //long updateStartTime = System.currentTimeMillis();
        //H2OStateClassifier.getInstance().updateLearningModel(episodeAnalysisList);
        if(StateClassifier.classifierModeIncludes("generate") || StateClassifier.classifierModeIncludes("generate-omi")) {
            SparkOmiStateClassifier.getInstance().updateLearningModel(episodeAnalysisList);
            //System.out.println("Model update completed in " + (System.currentTimeMillis() - updateStartTime) + " milliseconds");
        }

        reporter.finalizeReporting();

        this.plotter.endTrial();

    }


    /**
     * Runs a trial for an agent generated by the given factor when interpreting trial length as a number of total steps.
     *
     * @param agentFactory the agent factory used to generate the agent to test.
     */
    protected void runStepBoundTrial(LearningAgentFactory agentFactory) {
        LearningAgent agent = agentFactory.generateAgent();

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

    private void cleanOutputDirectory() {
        try {
            FileUtils.cleanDirectory(new File(Experiment.runId + "output"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StaticSelfManagementRewardPlotter getPlotterForExperiment() {
        return plotter;
    }
}
