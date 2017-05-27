package tez.environment.context;

import org.joda.time.LocalTime;

/**
 * Created by suatgonul on 12/24/2016.
 */
public class Context {
    private LocalTime time;
    private Location location;
    private PhysicalActivity physicalActivity;
    private EmotionalStatus emotionalStatus;
    private StateOfMind stateOfMind;
    private PhoneUsage phoneUsage;
    private boolean phoneCheckSuitability;

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public PhysicalActivity getPhysicalActivity() {
        return physicalActivity;
    }

    public void setPhysicalActivity(PhysicalActivity physicalActivity) {
        this.physicalActivity = physicalActivity;
    }

    public EmotionalStatus getEmotionalStatus() {
        return emotionalStatus;
    }

    public void setEmotionalStatus(EmotionalStatus emotionalStatus) {
        this.emotionalStatus = emotionalStatus;
    }

    public StateOfMind getStateOfMind() {
        return stateOfMind;
    }

    public void setStateOfMind(StateOfMind stateOfMind) {
        this.stateOfMind = stateOfMind;
    }

    public PhoneUsage getPhoneUsage() {
        return phoneUsage;
    }

    public void setPhoneUsage(PhoneUsage phoneUsage) {
        this.phoneUsage = phoneUsage;
    }

    public boolean getPhoneCheckSuitability() {
        return phoneCheckSuitability;
    }

    public void setPhoneCheckSuitability(boolean phoneCheckSuitability) {
        this.phoneCheckSuitability = phoneCheckSuitability;
    }

    public Context copy() {
        Context context = new Context();
        context.setTime(getTime());
        context.setPhoneUsage(getPhoneUsage());
        context.setEmotionalStatus(getEmotionalStatus());
        context.setStateOfMind(getStateOfMind());
        context.setLocation(getLocation());
        context.setPhysicalActivity(getPhysicalActivity());
        context.setPhoneCheckSuitability(getPhoneCheckSuitability());
        return context;
    }
}
