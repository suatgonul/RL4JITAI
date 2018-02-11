package tez2.algorithm;

import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.singleagent.Action;
import tez2.persona.ActionPlan;

import java.util.List;

public class ActionRestrictingState extends MutableState {

    private ActionPlan.JitaiNature expectedJitaiType;

    public ActionRestrictingState(ActionPlan.JitaiNature expectedJitaiType) {
        this.expectedJitaiType = expectedJitaiType;
    }

    public ActionRestrictingState(ActionRestrictingState s) {
        super(s);
        expectedJitaiType = s.expectedJitaiType;
    }

    public ActionPlan.JitaiNature getExpectedJitaiType() {
        return expectedJitaiType;
    }

    @Override
    public ActionRestrictingState copy() {
        ActionRestrictingState s = new ActionRestrictingState(this);
        return s;
    }
}
