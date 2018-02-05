package tez2.environment.context;

import org.joda.time.LocalTime;
import tez2.environment.context.EmotionalStatus;
import tez2.environment.context.Location;
import tez2.environment.context.PhoneUsage;
import tez2.environment.context.PhysicalActivity;

/**
 * Created by suatgonul on 12/24/2016.
 */
public class Context {
    private String deviceIdentifier;
    private LocalTime time;
    private Location location;
    private PhysicalActivity physicalActivity;
    private EmotionalStatus emotionalStatus;
    private PhoneUsage phoneUsage;
    private boolean phoneCheckSuitability;

    public Context() {
        this.time = LocalTime.now();
        this.location = Location.HOME;
        this.physicalActivity = PhysicalActivity.SEDENTARY;
        this.emotionalStatus = EmotionalStatus.NEUTRAL;
        this.phoneUsage = PhoneUsage.SCREEN_OFF;
        this.phoneCheckSuitability = false;
    }

    public Context(String deviceIdentifier) {
        this();
        this.deviceIdentifier = deviceIdentifier;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
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

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Context copy() {
        Context context = new Context();
        context.setTime(getTime());
        context.setPhoneUsage(getPhoneUsage());
        context.setEmotionalStatus(getEmotionalStatus());
        context.setLocation(getLocation());
        context.setPhysicalActivity(getPhysicalActivity());
        context.setPhoneCheckSuitability(getPhoneCheckSuitability());
        context.setDeviceIdentifier(getDeviceIdentifier());
        return context;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(")
                .append(getTime()).append(", ")
                .append(getLocation()).append(", ")
                .append(getPhysicalActivity()).append(", ")
                .append(getPhoneUsage())
                .append(")");
        return sb.toString();
    }
}
