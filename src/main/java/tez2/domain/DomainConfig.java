package tez2.domain;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;

public class DomainConfig {
    public static final String ATT_LOCATION = "Location";
    public static final String ATT_QUARTER_HOUR_OF_DAY = "QuarterHourOfDay";
    public static final String ATT_ACTIVITY = "Activity";
    public static final String ATT_PHONE_USAGE = "PhoneUsage";
    public static final String ATT_EMOTIONAL_STATUS = "EmotionalStatus";

    public static final String ATT_PART_OF_DAY = "PartOfDay";
    public static final String ATT_DAY_TYPE = "DayType";
    public static final String ATT_HABIT_STRENGTH = "HabitStrength";
    public static final String ATT_BEHAVIOR_FREQUENCY = "BehaviorFrequency";
    public static final String ATT_REMEMBER_BEHAVIOR = "BehaviorRemembered";

    public static final String CLASS_STATE = "state";

    public static final String ACTION_JITAI_1 = "JITAI_1";
    public static final String ACTION_JITAI_2 = "JITAI_2";
    public static final String ACTION_JITAI_3 = "JITAI_3";
    public static final String ACTION_NO_ACTION = "NO_INT";
    public static final String ACTION_SEND_JITAI = "SEND_JITAI";


    public static Attribute getAtt(String attributeName, Domain domain) {
        Attribute att = null;
        switch (attributeName) {
            case ATT_LOCATION:
                att = new Attribute(domain, ATT_LOCATION, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 2, 1);
                break;
            case ATT_QUARTER_HOUR_OF_DAY:
                att = new Attribute(domain, ATT_ACTIVITY, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 96, 1);
                break;
            case ATT_ACTIVITY:
                att = new Attribute(domain, ATT_ACTIVITY, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 5, 1);
                break;
            case ATT_PHONE_USAGE:
                att = new Attribute(domain, ATT_PHONE_USAGE, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 2, 1);
                break;
            case ATT_EMOTIONAL_STATUS:
                att = new Attribute(domain, ATT_EMOTIONAL_STATUS, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 2, 1);
                break;
            case ATT_PART_OF_DAY:
                att = new Attribute(domain, ATT_PART_OF_DAY, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 4, 1);
                break;
            case ATT_DAY_TYPE:
                att = new Attribute(domain, ATT_DAY_TYPE, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 1, 1);
                break;
            case ATT_HABIT_STRENGTH:
                att = new Attribute(domain, ATT_HABIT_STRENGTH, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 10, 1);
                break;
            case ATT_BEHAVIOR_FREQUENCY:
                att = new Attribute(domain, ATT_BEHAVIOR_FREQUENCY, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 10, 1);
                break;
            case ATT_REMEMBER_BEHAVIOR:
                att = new Attribute(domain, ATT_REMEMBER_BEHAVIOR, Attribute.AttributeType.INT);
                att.setDiscValuesForRange(0, 1, 1);
                break;
        }
        if(att == null) {
            throw new RuntimeException("Unrecognized attribute: " + attributeName);
        }
        return att;
    }
}
