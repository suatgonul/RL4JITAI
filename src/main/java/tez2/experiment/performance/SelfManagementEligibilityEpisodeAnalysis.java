package tez2.experiment.performance;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import org.joda.time.LocalTime;
import tez2.domain.SelfManagementAction;
import tez2.environment.context.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class SelfManagementEligibilityEpisodeAnalysis extends OmiEpisodeAnalysis {
    public List<String> interferenceList;
    public List<SelfManagementAction.SelectedBy> selectedByList;


    public SelfManagementEligibilityEpisodeAnalysis(State initialState) {
        super(initialState);
        interferenceList = new ArrayList<>();
        selectedByList = new ArrayList<>();
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, LocalTime stateTime, Context userContext, boolean userReaction, String interference, SelfManagementAction.SelectedBy selectedBy) {
        interferenceList.add(interference);
        selectedByList.add(selectedBy);
        super.recordTransitionTo(usingAction, nextState, r, qValues, stateTime, userContext, userReaction);
    }
}
