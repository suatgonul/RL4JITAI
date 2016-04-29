package power2dm.model.burden;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
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

    public BurdenP2DMEnvironmentSimulator() {
        setUserPreferences();
        resetEnvironment();
    }

    private void setUserPreferences() {
        preferences.createPreference(19, 21, Location.HOME);
//        preferences.createPreference(9, 17, Location.WORK);
    }

    public Location getLocation() {
        return location;
    }

    public double getBurdenCoefficient() {
        return burdenCoefficient;
    }

    public boolean simulateUserReactionToIntervention(State s, GroundedAction groundedAction) {
        if (time < 7) {
            return false;
        }
        if (location.equals(Location.ON_THE_WAY)) {
            return false;
        }


        boolean userHasPreference = preferences.doesUserHasPreference(time, location);

        Random r = new Random();
        double rDouble = r.nextDouble();

        boolean result;
        if (userHasPreference) {
            System.out.printf("Random: %f", rDouble);
            if (!fixedReaction) {
                if (rDouble > burdenCoefficient ) {
                    result = true;
                } else {
                    result = false;
                    System.out.printf("Random: %f Burden: %f", rDouble, burdenCoefficient);
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
    public void updateEnvironment(State s, GroundedAction groundedAction) {
        super.updateEnvironment(s, groundedAction);

        // update burden coefficient
        if(groundedAction.actionName().equals(ACTION_INT_DELIVERY)) {
            burdenCoefficient = 1;
        } else {
            burdenCoefficient *= 0.1;
        }
    }

    @Override
    public void resetEnvironment() {
        super.resetEnvironment();

        burdenCoefficient = 1;
    }
}
