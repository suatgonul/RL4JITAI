package tez.experiment.performance;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez.domain.SelfManagementDomainGenerator;
import tez.environment.context.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class SelfManagementEpisodeAnalysis extends EpisodeAnalysis {
    public LearningAgentFactory associatedLearningAgentFactory;
    public int trialNo;
    public int episodeNo;

    public List<List<QValue>> qValuesForStates;
    public List<Context> userContexts;
    public List<Boolean> userReactions;
    public int phoneCheckNumber;
    public int actionDeliveredDuringPhoneCheck;

    public SelfManagementEpisodeAnalysis(State initialState) {
        super(initialState);
        qValuesForStates = new ArrayList<>();
        userContexts = new ArrayList<>();
        userReactions = new ArrayList<>();
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, Context userContext, boolean userReaction) {
        qValuesForStates.add(qValues);
        userContexts.add(userContext);
        userReactions.add(userReaction);
        if (userReaction == true) {
            phoneCheckNumber++;
            if (usingAction.actionName().equals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
                actionDeliveredDuringPhoneCheck++;
            }
        }

        super.recordTransitionTo(usingAction, nextState, r);
    }
}
