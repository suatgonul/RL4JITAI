package tez2.experiment;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.performance.ExperimentalEnvironment;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.debugtools.DPrint;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentServer;
import org.apache.commons.io.FileUtils;
import power2dm.reporting.visualization.VisualizationMetadata;
import tez2.algorithm.collaborative_learning.SparkStateClassifier;
import tez2.experiment.debug.Reporter;
import tez2.experiment.performance.*;
import tez2.experiment.performance.visualization.ReactionHitRatioVisualizer;
import tez2.experiment.performance.visualization.ReactionNumbersVisualizer;
import tez2.experiment.performance.visualization.Visualizer;

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
    private int tempLength;


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
        this.tempLength = trialLength;
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

        LearningAgent agent = agentFactory.generateAgent();

        this.plotter.startNewTrial();

        List<OmiEpisodeAnalysis> episodeAnalysisList = new ArrayList<>();
        Reporter reporter = new Reporter("output/" + agentFactory.getAgentName() + ".txt");
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
            episodeAnalysisList.add(ea);
            //if (agentFactory.getAgentName().contains("colla")) {
                elapsedTrialTime += (System.currentTimeMillis()-episodeStarttime);
                System.out.println("Episode: " + (i + 1) + " completed in " + (System.currentTimeMillis()-episodeStarttime) + " milliseconds");
                System.out.println("Elapsed trial time: " + elapsedTrialTime + " milliseconds");
            //}

            this.plotter.populateAgentDatasets(ea);
            this.plotter.endEpisode();
            this.environmentSever.resetEnvironment();

            /*if (ea instanceof SelfManagementEpisodeAnalysis) {
                if (i < 50 || i > this.trialLength - 50) {
                    //System.out.println("Episode " + (i + 1));
                    reporter.report("Episode " + (i + 1));
                    for (int j = 0; j < ea.rewardSequence.size(); j++) {
                        sb = new StringBuilder();

                        // Context details from the state object
                        ObjectInstance o = ea.stateSequence.get(j).getObjectsOfClass(CLASS_STATE).get(0);
                        Location location = Location.values()[o.getIntValForAttribute(ATT_LOCATION)];
                        DayType dayType = DayType.values()[o.getIntValForAttribute(ATT_DAY_TYPE)];

                        // Context details from the context object
                        Context context = ea.userContexts.get(j);
                        Location location_c = context.getLocation();
                        PhoneUsage phoneUsage_c = context.getPhoneUsage();
                        EmotionalStatus emotionalStatus_c = context.getEmotionalStatus();
                        PhysicalActivity physicalActivity_c = context.getPhysicalActivity();

                        //if (hourOfDay > 16) {

                        SelfManagementDomain smDomain = ((SimulatedWorld) environmentSever.getEnvironmentDelegate()).getDomain();
                        SelfManagementDomain.DomainComplexity complexity = smDomain.getComplexity();

                        if (complexity == SelfManagementDomain.DomainComplexity.EASY) {
                            int hourOfDay = o.getIntValForAttribute(ATT_HOUR_OF_DAY);
                            System.out.print("(" + hourOfDay + ", " + location + ", " + dayType + ") ");

                        } else if (complexity == SelfManagementDomain.DomainComplexity.MEDIUM) {
                            String quarterHourOfDay = o.getStringValForAttribute(ATT_QUARTER_HOUR_OF_DAY);
                            PhysicalActivity activity = PhysicalActivity.values()[o.getIntValForAttribute(ATT_ACTIVITY)];
                            System.out.print("(" + quarterHourOfDay + ", " + activity + ", " + location + ", " + dayType + ") ");

                        } else if (complexity == SelfManagementDomain.DomainComplexity.HARD) {
                            String activityTime = o.getStringValForAttribute(ATT_ACTIVITY_TIME);
                            PhysicalActivity activity = PhysicalActivity.values()[o.getIntValForAttribute(ATT_ACTIVITY)];
                            PhoneUsage phoneUsage = PhoneUsage.values()[o.getIntValForAttribute(ATT_PHONE_USAGE)];
                            EmotionalStatus emotionalStatus = EmotionalStatus.values()[o.getIntValForAttribute(ATT_EMOTIONAL_STATUS)];
                            //System.out.print("(" + activityTime + ", " + location + ", " + activity + ", " + dayType + ", " + stateOfMind + ", " + emotionalStatus + ") ");
                            sb.append("(" + activityTime + ", " + location + ", " + activity + ", " + dayType + ", " + emotionalStatus + ") ");
                        }

                        //System.out.print("(" + location_c + ", " + physicalActivity_c + ", " + phoneUsage_c + ", " + stateOfMind_c + ", " + emotionalStatus_c + ") ");
                        sb.append("(" + location_c + ", " + physicalActivity_c + ", " + phoneUsage_c + ", " + emotionalStatus_c + ") ");
                        int actionNo;
                        for (QValue qv : ea.qValuesForStates.get(j)) {
                            actionNo = qv.a.actionName().equals(ACTION_INT_DELIVERY) ? 1 : 0;

                            //System.out.print(actionNo + ": " + qValPrecision.format(qv.q) + ", ");
                            sb.append(actionNo + ": " + qValPrecision.format(qv.q) + ", ");
                        }
                        actionNo = ea.actionSequence.get(j).actionName().equals(ACTION_INT_DELIVERY) ? 1 : 0;
                        //System.out.print(") A:" + actionNo + ", R:" + ea.rewardSequence.get(j));
                        sb.append(") A:" + actionNo + ", R:" + ea.rewardSequence.get(j));
                        if (ea instanceof SelfManagementEligibilityEpisodeAnalysis) {
                            //System.out.println(ea.userReactions.get(j) == true ? " (X)" : "" + " Inter: " + ((SelfManagementEligibilityEpisodeAnalysis) ea).interferenceList.get(j));
                            String interference = ((SelfManagementEligibilityEpisodeAnalysis) ea).interferenceList.get(j);
                            SelfManagementAction.SelectedBy selectedBy = ((SelfManagementEligibilityEpisodeAnalysis) ea).selectedByList.get(j);
                            sb.append(ea.userReactions.get(j) == true ? " (X) Inter: " + interference + " Selected by:" + selectedBy : "" + " Inter: " + interference + " Selected by: " + selectedBy);
                        } else {
                            //System.out.println(ea.userReactions.get(j) == true ? " (X)" : "");
                            sb.append(ea.userReactions.get(j) == true ? " (X)" : "");
                        }
                        reporter.report(sb.toString());
                    }
                }
            }*/
        }
        System.out.println("Trial completed in " + (System.currentTimeMillis() - trialStartTime) + " milliseconds");

        //long updateStartTime = System.currentTimeMillis();
        //H2OStateClassifier.getInstance().updateLearningModel(episodeAnalysisList);
        //SparkStateClassifier.getInstance().updateLearningModel(episodeAnalysisList);
        //System.out.println("Model update completed in " + (System.currentTimeMillis() - updateStartTime) + " milliseconds");

        reporter.finalizeReporting();

        //TODO do it properly
        /*VisualizationMetadata visualizerMetadata = new VisualizationMetadata();
        visualizerMetadata
                .setMetadataForVisualizer(ReactionHitRatioVisualizer.class, Visualizer.METADATA_LEARNING_ALGORITHM, agentFactory.getAgentName())
                .setMetadataForVisualizer(ReactionHitRatioVisualizer.class, Visualizer.METADATA_POLICY, agent);
        Visualizer visualizer = new ReactionHitRatioVisualizer(visualizerMetadata.getVisualizerMetadata(ReactionHitRatioVisualizer.class));
        visualizer.createRewardGraph(episodeAnalysisList);
        visualizer = new ReactionNumbersVisualizer(visualizerMetadata.getVisualizerMetadata(ReactionNumbersVisualizer.class));
        visualizer.createRewardGraph(episodeAnalysisList);*/
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
            FileUtils.cleanDirectory(new File("output"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
