package tez.persona;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/2/2016.
 * Data structure keeping the activities to performed by a person during the day
 */
public class TimePlan {
    private List<Activity> activities = new ArrayList<Activity>();

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public DateTime getEndTime() {
        return activities.get(activities.size()-1).getEndTime();
    }
}
