package tez2.domain.omi;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import org.joda.time.LocalTime;
import tez2.environment.context.Context;

/**
 * Created by suatgonul on 4/28/2017.
 */
public class OmiEnvironmentOutcome extends EnvironmentOutcome {

    private boolean userReaction;
    private Context userContext;
    private LocalTime stateTime;

    public OmiEnvironmentOutcome(State o, GroundedAction a, State op, double r, boolean terminated, Context userContext, boolean userReaction, LocalTime stateTime) {
        super(o, a, op, r, terminated);
        this.userContext = userContext;
        this.userReaction = userReaction;
        this.stateTime = stateTime;
    }

    public boolean getUserReaction() {
        return userReaction;
    }

    public Context getUserContext() {
        return userContext;
    }

    public LocalTime getStateTime() {
        return stateTime;
    }
}
