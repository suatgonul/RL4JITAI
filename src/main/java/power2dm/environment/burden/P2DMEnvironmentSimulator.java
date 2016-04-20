package power2dm.environment.burden;

import burlap.oomdp.singleagent.Action;
import power2dm.Location;
import power2dm.UserPreference;

import java.util.Random;

import static power2dm.environment.burden.P2DMDomain.ACTION_INT_DELIVERY;

/**
 * Created by suat on 14-Apr-16.
 */
public class P2DMEnvironmentSimulator {
    private P2DMDomain domain;

    private UserPreference preferences = new UserPreference();
    private boolean fixedReaction = true;
    private double burdenCoefficient = 1;

    private int time = 0;
    private Location location = Location.HOME;

    public P2DMEnvironmentSimulator(P2DMDomain domain) {
        this.domain = domain;
        setUserPreferences();
        resetEnvironment();
    }

    private void setUserPreferences() {
        preferences.createPreference(19, 21, Location.HOME);
    }

    public Location getLocation() {
        return location;
    }

    public boolean simulateUserReactionToIntervention() {
        if (time < 7) {
            return false;
        }
        if (location.equals(Location.ON_THE_WAY)) {
            return false;
        }


        boolean userHasPreference = preferences.doesUserHasPreference(time, location);

        // if we are in an AFTERNOON state we should consider user preference on EVENING
        Random r = new Random();
        int low = 0;
        int high = 100;
        int rInt = r.nextInt(high - low) + low;

        boolean result;
        if (userHasPreference) {
            if (!fixedReaction) {
                if (rInt >= burdenCoefficient) {
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = true;
            }
        } else {
            if (!fixedReaction) {
                if (rInt >= 80) {
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = false;
            }

        }
        return result;
    }

    public void updateEnvironment(Action act) {
        // update time
        time++;

        // update location
        if (time < 8 || time > 18) {
            location = Location.HOME;
        } else if (time == 8 || time == 18) {
            location = Location.ON_THE_WAY;
        } else {
            location = Location.WORK;
        }

        // update burden coefficient
        if(act.getName().equals(ACTION_INT_DELIVERY)) {
            burdenCoefficient = 1;
        } else {
            burdenCoefficient *= 0.5;
        }

        if (time == 24) {
            resetEnvironment();
        }
    }


    private void resetEnvironment() {
        time = 0;
        location = Location.HOME;
    }
}
