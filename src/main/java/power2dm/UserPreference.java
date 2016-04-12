package power2dm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suat on 12-Apr-16.
 */
public class UserPreference {
    private Map<Timing, Integer> preferences = new HashMap<Timing, Integer>();

    public UserPreference() {
        for(Timing timing : Timing.values()) {
            preferences.put(timing, 0);
        }
    }

    public void setPreference(Timing timing, int preference) {
        preferences.put(timing, preference);
    }

    public int getPreference(Timing timing) {
        return preferences.get(timing);
    }
}
