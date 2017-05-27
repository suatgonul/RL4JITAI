package tez.algorithm.collaborative_learning;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import tez.environment.context.*;

import java.io.Serializable;

import static tez.domain.SelfManagementDomainGenerator.*;

/**
 * Created by suat on 23-May-17.
 */
public class StateBean implements Serializable {
    private int time;
    private String dayType;
    private String location;
    private String activity;
    private String phoneUsage;
    private String stateOfMind;
    private String emotionalStatus;

    public StateBean(State state) {
        ObjectInstance o = state.getObjectsOfClass(CLASS_STATE).get(0);
        String time = o.getStringValForAttribute(ATT_ACTIVITY_TIME);
        int hour = Integer.valueOf(time.split(":")[0]);
        int minute = Integer.valueOf(time.split(":")[1]);
        this.time = hour * 60 + minute;
        this.dayType = DayType.values()[o.getIntValForAttribute(ATT_DAY_TYPE)].toString();
        this.location = Location.values()[o.getIntValForAttribute(ATT_LOCATION)].toString();
        this.activity = PhysicalActivity.values()[o.getIntValForAttribute(ATT_ACTIVITY)].toString();
        this.phoneUsage = PhoneUsage.APPS_ACTIVE.values()[o.getIntValForAttribute(ATT_PHONE_USAGE)].toString();
        this.stateOfMind = StateOfMind.values()[o.getIntValForAttribute(ATT_STATE_OF_MIND)].toString();
        this.emotionalStatus = EmotionalStatus.values()[o.getIntValForAttribute(ATT_EMOTIONAL_STATUS)].toString();
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
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

    public String getStateOfMind() {
        return stateOfMind;
    }

    public void setStateOfMind(String stateOfMind) {
        this.stateOfMind = stateOfMind;
    }

    public String getEmotionalStatus() {
        return emotionalStatus;
    }

    public void setEmotionalStatus(String emotionalStatus) {
        this.emotionalStatus = emotionalStatus;
    }
}
