package tez.experiment.performance;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez.simulator.context.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class SelfManagementEligibilityEpisodeAnalysis extends SelfManagementEpisodeAnalysis {
    public List<String> interferenceList;

    public SelfManagementEligibilityEpisodeAnalysis(State initialState) {
        super(initialState);
        interferenceList = new ArrayList<>();
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, Context userContext, boolean userReaction, String interference) {
        interferenceList.add(interference);
        super.recordTransitionTo(usingAction, nextState, r, qValues, userContext, userReaction);
    }
}
