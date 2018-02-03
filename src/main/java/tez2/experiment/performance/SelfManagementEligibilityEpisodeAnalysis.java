package tez2.experiment.performance;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez.domain.action.SelfManagementAction;
import tez.environment.context.Context;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class SelfManagementEligibilityEpisodeAnalysis extends SelfManagementEpisodeAnalysis {
    public List<String> interferenceList;
    public List<SelfManagementAction.SelectedBy> selectedByList;


    public SelfManagementEligibilityEpisodeAnalysis(State initialState) {
        super(initialState);
        interferenceList = new ArrayList<>();
        selectedByList = new ArrayList<>();
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, Context userContext, boolean userReaction, String interference, SelfManagementAction.SelectedBy selectedBy) {
        interferenceList.add(interference);
        selectedByList.add(selectedBy);
        super.recordTransitionTo(usingAction, nextState, r, qValues, userContext, userReaction);
    }
}
