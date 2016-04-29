package power2dm.model.reacted_non_reacted_numbers;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import power2dm.model.EnvironmentSimulator;
import power2dm.model.Location;

import java.util.Random;

import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.ATT_REACTED_INT;
import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.CLASS_STATE;

/**
 * Created by suat on 14-Apr-16.
 */
public class ReactNonReactP2DMEnvironmentSimulator extends EnvironmentSimulator {

    private boolean fixedReaction = true;
    private int[] burdenCoefficient = new int[]{10, 30, 50, 70, 90};
    private int lastInterventionTime = 0;

    public ReactNonReactP2DMEnvironmentSimulator() {
        setUserPreferences();
        resetEnvironment();
    }

    private void setUserPreferences() {
        preferences.createPreference(19, 21, Location.HOME);
    }

    public Location getLocation() {
        return location;
    }

    public boolean simulateUserReactionToIntervention(State s, GroundedAction groundedAction) {
        if (time < 7) {
            return false;
        }
        if (location.equals(Location.ON_THE_WAY)) {
            return false;
        }
        if (time - lastInterventionTime < 2) {
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
                ObjectInstance state = s.getFirstObjectOfClass(CLASS_STATE);
                int reactedTotal = state.getIntValForAttribute(ATT_REACTED_INT);

                if (rInt >= burdenCoefficient[reactedTotal > 4 ? 4 : reactedTotal]) {
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
        if (result == true) {
            lastInterventionTime = time;
        }
        return result;
    }

    @Override
    public void resetEnvironment() {
        super.resetEnvironment();
        lastInterventionTime = 0;
    }
}
