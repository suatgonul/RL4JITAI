package tez.persona;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/2/2016
 * Represent an activity that is performed by a person during the day. Activities having long duration might have
 * intermediate phone checks with varying durations.
 */
public class Activity {
    private String name;

    private DateTime start;

    // duration of activity in minutes
    private int duration;

    // keeps sporadic phone checks occurring within the duration of this main activity
    private List<Activity> phoneChecks = new ArrayList<Activity>();

    public void setName(String name) {
        this.name = name;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public DateTime getStart() {
        return start;
    }

    public int getDuration() {
        return duration;
    }

    public DateTime getEndTime() {
        return start.plusMinutes(duration);
    }

//    public void addPhoneChecks() {
//
//        if(duration > ) {
//
//        }
//    }
}
