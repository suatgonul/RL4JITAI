package tez.experiment.performance;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez.domain.SelfManagementDomainGenerator;
import tez.simulator.context.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class SelfManagementEpisodeAnalysis extends EpisodeAnalysis {
    public List<List<QValue>> qValuesForStates;
    public List<Context> userContexts;
    public List<Boolean> userReactions;
    public int totalNumberOfPotentialPositiveReaction;
    public int actionDeliveredForPotentialReaction;


    public SelfManagementEpisodeAnalysis(State initialState) {
        super(initialState);
        qValuesForStates = new ArrayList<>();
        userContexts = new ArrayList<>();
        userReactions = new ArrayList<>();
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, Context userContext, boolean userReaction) {
        List<QValue> copyList = new ArrayList<>();
        for (QValue qv : qValues) {
            copyList.add(new QValue(qv));
        }

        qValuesForStates.add(copyList);
        userContexts.add(userContext);
        userReactions.add(userReaction);
        if (userReaction == true) {
            totalNumberOfPotentialPositiveReaction++;
            if(usingAction.actionName().equals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
                actionDeliveredForPotentialReaction++;
            }
        }

        super.recordTransitionTo(usingAction, nextState, r);
    }
}
