package power2dm.model.burden;

import burlap.oomdp.singleagent.Action;
import power2dm.model.EnvironmentSimulator;
import power2dm.model.Location;
import power2dm.model.UserPreference;

import java.util.Random;

import static power2dm.model.burden.BurdenP2DMDomain.ACTION_INT_DELIVERY;

/**
 * Created by suat on 14-Apr-16.
 */
public class BurdenP2DMEnvironmentSimulator extends EnvironmentSimulator {
    private UserPreference preferences = new UserPreference();
    private boolean fixedReaction = false;
    private double burdenCoefficient = 1;

    private int time = 0;
    private Location location = Location.HOME;

    public BurdenP2DMEnvironmentSimulator(BurdenP2DMDomain domain) {
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

    public double getBurdenCoefficient() {
        return burdenCoefficient;
    }

    public boolean simulateUserReactionToIntervention(Action act) {
        if (time < 7) {
            return false;
        }
        if (location.equals(Location.ON_THE_WAY)) {
            return false;
        }


        boolean userHasPreference = preferences.doesUserHasPreference(time, location);

        // if we are in an AFTERNOON state we should consider user preference on EVENING
        Random r = new Random();
        double rDouble = r.nextDouble();

        boolean result;
        if (userHasPreference) {
            if (!fixedReaction) {
                if (rDouble > burdenCoefficient ) {
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = true;
            }
        } else {
            if (!fixedReaction) {
                if (rDouble > 0.75) {
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

    @Override
    public void updateEnvironment(Action act) {
        super.updateEnvironment(act);

        // update burden coefficient
        if(act.getName().equals(ACTION_INT_DELIVERY)) {
            burdenCoefficient = 1;
        } else {
            burdenCoefficient *= 0.5;
        }
    }

    @Override
    public void resetEnvironment() {
        super.resetEnvironment();

        burdenCoefficient = 1;
    }
}
