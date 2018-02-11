package tez2.experiment.performance;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez2.domain.js.JsEnvironmentOutcome;
import tez2.environment.context.Context;

import java.util.List;

import static tez2.domain.DomainConfig.ACTION_SEND_JITAI;

public class JsEpisodeAnalysis extends SelfManagementEpisodeAnalysis {

    JsEnvironmentOutcome eo;

    public JsEpisodeAnalysis(State initialState) {
        super(initialState);
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, JsEnvironmentOutcome eo) {
        this.eo = eo;
        super.recordTransitionTo(usingAction, nextState, r);
    }
}
