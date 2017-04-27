package tez.experiment;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.singleagent.environment.Environment;
import tez.simulator.context.DayType;
import tez.simulator.context.Location;

import java.text.DecimalFormat;

import static tez.algorithm.SelfManagementDomainGenerator.*;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class SelfManagementExperimenter extends LearningAlgorithmExperimenter {
    private static DecimalFormat qValPrecision = new DecimalFormat("#0.0000");

    public SelfManagementExperimenter(Environment testEnvironment, int nTrials, int trialLength, LearningAgentFactory... agentFactories) {
        super(testEnvironment, nTrials, trialLength, agentFactories);
    }

    /**
     * Overrides the base methods to access the {@link EpisodeAnalysis} after completing an episode
     *
     * @param agentFactory
     */
    @Override
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
}
