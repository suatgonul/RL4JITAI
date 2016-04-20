package power2dm.environment.reacted_non_reacted_numbers;

import power2dm.Location;
import power2dm.UserPreference;

import java.util.Random;

/**
 * Created by suat on 14-Apr-16.
 */
public class P2DMEnvironmentSimulator {
    private P2DMDomain domain;

    private UserPreference preferences = new UserPreference();
    private boolean fixedReaction = true;

    private int time = 0;
    private Location location = Location.HOME;
    private int[] burdenCoefficient = new int[]{10, 30, 50, 70, 90};
    private int lastInterventionTime = 0;

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

    public boolean simulateUserReactionToIntervention(int reactedTotal) {
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

    public void updateEnvironment() {
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

    public void setBurdenCoefficient(int i) {
        if (i == 1) {
            burdenCoefficient = new int[]{10, 30, 50, 70, 90};
        } else if (i == 0) {
            burdenCoefficient = new int[]{20, 20, 20, 20, 10};
        }
    }

    private void resetEnvironment() {
        time = 0;
        lastInterventionTime = 0;
        location = Location.HOME;
    }
}
