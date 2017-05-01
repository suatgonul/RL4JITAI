package tez.domain;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import tez.simulator.context.Context;

/**
 * Created by suatgonul on 4/28/2017.
 */
public class ExtendedEnvironmentOutcome extends EnvironmentOutcome {

    private boolean userReaction;
    private Context userContext;

    public ExtendedEnvironmentOutcome(State o, GroundedAction a, State op, double r, boolean terminated, Context userContext, boolean userReaction) {
        super(o, a, op, r, terminated);
        this.userContext = userContext;
        this.userReaction = userReaction;
    }

    public boolean getUserReaction() {
        return userReaction;
    }

    public Context getUserContext() {
        return userContext;
    }
}
