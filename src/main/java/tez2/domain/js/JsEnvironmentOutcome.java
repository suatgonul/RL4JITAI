package tez2.domain.js;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import tez2.environment.context.Context;

/**
 * Created by suatgonul on 4/28/2017.
 */
public class JsEnvironmentOutcome extends EnvironmentOutcome {

    private boolean userReaction;
    private int habitStrength;
    private int behaviorFrequency;
    private boolean rememberBehavior;

    public JsEnvironmentOutcome(State o, GroundedAction a, State op, double r, boolean terminated, int habitStrength, int behaviorFrequency, boolean rememberBehavior, boolean userReaction) {
        super(o, a, op, r, terminated);
        this.habitStrength = habitStrength;
        this.behaviorFrequency = behaviorFrequency;
        this.rememberBehavior = rememberBehavior;
        this.userReaction = userReaction;
    }

    public boolean getUserReaction() {
        return userReaction;
    }

    public int getHabitStrength() {
        return habitStrength;
    }

    public int getBehaviorFrequency() {
        return behaviorFrequency;
    }

    public boolean getRememberBehavior() {
        return rememberBehavior;
    }
}
