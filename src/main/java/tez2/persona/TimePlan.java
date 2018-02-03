package tez2.persona;

import org.joda.time.DateTime;
import tez.persona.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/2/2016.
 * Data structure keeping the activities to performed by a person during the day
 */
public class TimePlan {
    private List<Activity> activities = new ArrayList<Activity>();

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public DateTime getStart() {
        return activities.get(0).getStart();
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public DateTime getEndTime() {
        return activities.get(activities.size()-1).getEndTime();
    }
}
