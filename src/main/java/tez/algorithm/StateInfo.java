package tez.algorithm;

import burlap.oomdp.core.states.State;

/**
 * Created by suatgonul on 4/28/2017.
 */
public class StateInfo {
    private boolean userReaction;
    private State userState;

    public StateInfo(State userState, boolean userWouldReact) {
        this.userState = userState;
        this.userReaction = userWouldReact;
    }

    public boolean getUserReaction() {
        return userReaction;
    }

    public State getUserState() {
        return userState;
    }
}
