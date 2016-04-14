package power2dm;

import java.util.Random;

/**
 * Created by suat on 14-Apr-16.
 */
public class P2DMEnvironmentSimulator {
    private P2DMDomain domain;

    private UserPreference preferences = new UserPreference();
    private int time = 0;
    private Location location = Location.HOME;
    private int[] willingnessToReact = new int[]{90, 70, 50, 30, 10};
    private int lastInterventionTime = 0;

    public P2DMEnvironmentSimulator(P2DMDomain domain) {
        this.domain = domain;
        setUserPreferences();
        resetEnvironment();
    }

    private void setUserPreferences() {
        preferences.createPreference(18, 20, Location.HOME);
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
            if (rInt < willingnessToReact[reactedTotal > 4 ? 4 : reactedTotal]) {
                result = true;
            } else {
                result = false;
            }
        } else {
            if (rInt >= 50) {
                result = true;
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
    }

    private void resetEnvironment() {
        time = 0;
        location = Location.HOME;
    }
}
