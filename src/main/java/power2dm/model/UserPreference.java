package power2dm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 12-Apr-16.
 */
public class UserPreference {
    private List<PreferenceInstance> preferences = new ArrayList<PreferenceInstance>();

    public void createPreference(int start, int end, Location location) {
        preferences.add(new PreferenceInstance(start, end, location));
    }

    public boolean doesUserHasPreference(int time, Location location) {
        for(PreferenceInstance p : preferences) {
            if(p.getTimeRange().isInRange(time) && location.equals(p.getLocation())) {
                return true;
            }
        }
        return false;
    }

    private class PreferenceInstance {
        private PreferenceRange timeRange;
        private Location location;

        public PreferenceInstance(int start, int end, Location location) {
            this.timeRange = new PreferenceRange(start, end);
            this.location = location;
        }

        public PreferenceRange getTimeRange() {
            return timeRange;
        }

        public Location getLocation() {
            return location;
        }
    }

    private class PreferenceRange{
        private int start;
        private int end;

        public PreferenceRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public boolean isInRange(int time) {
            return time >= start && time <= end;
        }
    }
}
