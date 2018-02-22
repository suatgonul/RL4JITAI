package tez2.algorithm.collaborative_learning.js;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import tez2.domain.DomainConfig;
import tez2.environment.context.EmotionalStatus;
import tez2.environment.context.Location;
import tez2.environment.context.PhoneUsage;
import tez2.environment.context.PhysicalActivity;

import java.io.Serializable;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suat on 23-May-17.
 */
public class JsStateBean implements Serializable {
    private int rememberBehavior;
    private int behaviorFrequency;
    private int habitStrength;
    private int partOfDay;
    private int dayType;


    public JsStateBean(State state) {
        ObjectInstance o = state.getObjectsOfClass(CLASS_STATE).get(0);
        this.rememberBehavior = o.getIntValForAttribute(ATT_REMEMBER_BEHAVIOR);
        this.behaviorFrequency = o.getIntValForAttribute(ATT_BEHAVIOR_FREQUENCY);
        this.habitStrength = o.getIntValForAttribute(ATT_HABIT_STRENGTH);
        this.partOfDay = o.getIntValForAttribute(ATT_PART_OF_DAY);
        this.dayType = o.getIntValForAttribute(ATT_DAY_TYPE);
    }

    public int getRememberBehavior() {
        return rememberBehavior;
    }

    public void setRememberBehavior(int rememberBehavior) {
        this.rememberBehavior = rememberBehavior;
    }

    public int getBehaviorFrequency() {
        return behaviorFrequency;
    }

    public void setBehaviorFrequency(int behaviorFrequency) {
        this.behaviorFrequency = behaviorFrequency;
    }

    public int getHabitStrength() {
        return habitStrength;
    }

    public void setHabitStrength(int habitStrength) {
        this.habitStrength = habitStrength;
    }

    public int getPartOfDay() {
        return partOfDay;
    }

    public void setPartOfDay(int partOfDay) {
        this.partOfDay = partOfDay;
    }

    public int getDayType() {
        return dayType;
    }

    public void setDayType(int dayType) {
        this.dayType = dayType;
    }
}
