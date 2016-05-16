package power2dm.algorithm;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import power2dm.model.Location;
import power2dm.model.UserPreference;

/**
 * Created by suat on 27-Apr-16.
 */
public abstract class EnvironmentSimulator {
    protected P2DMDomain domain;
    protected UserPreference preferences = new UserPreference();
    protected boolean fixedReaction = true;

    protected int dayOfExperiment = 1;
    protected int hourOfDay = 0;
    protected Location location = Location.HOME;

    public abstract boolean simulateUserReactionToIntervention(State s, GroundedAction groundedAction);

    public void updateEnvironment(State s, GroundedAction groundedAction) {
        hourOfDay++;
        if (hourOfDay < 8 || hourOfDay > 18) {
            location = Location.HOME;
        } else if (hourOfDay == 8 || hourOfDay == 18) {
            location = Location.ON_THE_WAY;
        } else {
            location = Location.WORK;
        }

        if (hourOfDay == 24) {
            resetEnvironmentForEpisode();
        }
    }

    public void resetEnvironmentForEpisode() {
        location = Location.HOME;
        hourOfDay = 0;
    }

    public Location getLocation() {
        return location;
    }

    public void setDomain(P2DMDomain domain) {
        this.domain = domain;
    }

    protected void setUserPreferences() {
        preferences.createPreference(19, 21, Location.HOME);
    }
}
