package tez2.algorithm.collaborative_learning;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import tez2.domain.DomainConfig;
import tez2.environment.context.*;

import java.io.Serializable;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suat on 23-May-17.
 */
public class StateBean implements Serializable {
    private int time;
    private String location;
    private String activity;
    private String phoneUsage;
    private String emotionalStatus;

    public StateBean(State state) {
        ObjectInstance o = state.getObjectsOfClass(CLASS_STATE).get(0);
        this.location = Location.values()[o.getIntValForAttribute(ATT_LOCATION)].toString();
        this.activity = PhysicalActivity.values()[o.getIntValForAttribute(ATT_ACTIVITY)].toString();
        this.phoneUsage = PhoneUsage.APPS_ACTIVE.values()[o.getIntValForAttribute(ATT_PHONE_USAGE)].toString();
        this.emotionalStatus = EmotionalStatus.values()[o.getIntValForAttribute(ATT_EMOTIONAL_STATUS)].toString();
        this.time = o.getIntValForAttribute(ATT_QUARTER_HOUR_OF_DAY);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getPhoneUsage() {
        return phoneUsage;
    }

    public void setPhoneUsage(String phoneUsage) {
        this.phoneUsage = phoneUsage;
    }

    public String getEmotionalStatus() {
        return emotionalStatus;
    }

    public void setEmotionalStatus(String emotionalStatus) {
        this.emotionalStatus = emotionalStatus;
    }
}
