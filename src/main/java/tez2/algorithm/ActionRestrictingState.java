package tez2.algorithm;

import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.singleagent.Action;

import java.util.List;

public class ActionRestrictingState extends MutableState {
    private List<Action> allowedActions;

    public ActionRestrictingState(List<Action> allowedActions) {
        super();
        this.allowedActions = allowedActions;
    }

    public List<Action> getAllowedActions() {
        return this.allowedActions;
    }
}
