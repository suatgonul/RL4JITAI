package power2dm.model;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * Created by suat on 27-Apr-16.
 */
public abstract class EnvironmentSimulator {
    protected P2DMDomain domain;
    protected UserPreference preferences = new UserPreference();
    protected int time = 0;
    protected Location location = Location.HOME;

    public abstract boolean simulateUserReactionToIntervention(State s, GroundedAction groundedAction);

    public void updateEnvironment(State s, GroundedAction groundedAction) {
        time++;
        if (time < 8 || time > 18) {
            location = Location.HOME;
        } else if (time == 8 || time == 18) {
            location = Location.ON_THE_WAY;
        } else {
            location = Location.WORK;
        }

        if (time == 24) {
            resetEnvironment();
        }
    }

    public void resetEnvironment() {
        location = Location.HOME;
        time = 0;
    }

    public void setDomain(P2DMDomain domain) {
        this.domain = domain;
    }
}
