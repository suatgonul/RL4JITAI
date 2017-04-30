package tez.algorithm;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.environment.Environment;
import tez.algorithm.action.InterventionDeliveryAction;
import tez.algorithm.action.NoAction;
import tez.algorithm.action.SelfManagementAction;

/**
 * Created by suatgonul on 4/20/2017.
 */
public class SelfManagementDomainGenerator implements DomainGenerator {
    public static final String ATT_HOUR_OF_DAY = "hourOfDay";
    public static final String ATT_QUARTER_HOUR_OF_DAY = "quarterHourOfDay";
    public static final String ATT_ACTIVITY_TIME = "activityTime";
    public static final String ATT_LOCATION = "location";
    public static final String ATT_DAY_TYPE = "dayType";
    public static final String ATT_ACTIVITY = "activity";
    public static final String ATT_PHONE_USAGE = "phoneUsage";
    public static final String ATT_STATE_OF_MIND = "stateOfMind";
    public static final String ATT_EMOTIONAL_STATUS = "emotionalStatus";

    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private SelfManagementDomain.DomainComplexity complexity;
    private Domain domain;
    private Environment environment;

    public SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity complexity) {
        this.complexity = complexity;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;

        for (Action a : domain.getActions()) {
            ((SelfManagementAction) a).setEnvironment(environment);
        }
    }

    @Override
    public Domain generateDomain() {
        domain = new SelfManagementDomain(complexity);

        Attribute locationAtt = new Attribute(domain, ATT_LOCATION, Attribute.AttributeType.INT);
        locationAtt.setDiscValuesForRange(0, 2, 1);
        Attribute dayTypeAtt = new Attribute(domain, ATT_DAY_TYPE, Attribute.AttributeType.INT);
        dayTypeAtt.setDiscValuesForRange(0, 1, 1);

        ObjectClass stateClass = new ObjectClass(domain, CLASS_STATE);
        if (complexity == SelfManagementDomain.DomainComplexity.EASY) {
            Attribute timingAtt = new Attribute(domain, ATT_HOUR_OF_DAY, Attribute.AttributeType.INT);
            timingAtt.setDiscValuesForRange(0, 23, 1);

            stateClass.addAttribute(timingAtt);
            stateClass.addAttribute(dayTypeAtt);
            stateClass.addAttribute(locationAtt);

        } else if (complexity == SelfManagementDomain.DomainComplexity.MEDIUM) {
            Attribute timingAtt = new Attribute(domain, ATT_QUARTER_HOUR_OF_DAY, Attribute.AttributeType.STRING);
            Attribute activityAtt = new Attribute(domain, ATT_ACTIVITY, Attribute.AttributeType.INT);
            activityAtt.setDiscValuesForRange(0, 5, 1);

            stateClass.addAttribute(timingAtt);
            stateClass.addAttribute(dayTypeAtt);
            stateClass.addAttribute(locationAtt);
            stateClass.addAttribute(activityAtt);

        } else if (complexity == SelfManagementDomain.DomainComplexity.HARD) {
            Attribute timingAtt = new Attribute(domain, ATT_ACTIVITY_TIME, Attribute.AttributeType.STRING);
            Attribute activityAtt = new Attribute(domain, ATT_ACTIVITY, Attribute.AttributeType.INT);
            activityAtt.setDiscValuesForRange(0, 5, 1);
            Attribute phoneUsageAtt = new Attribute(domain, ATT_PHONE_USAGE, Attribute.AttributeType.INT);
            phoneUsageAtt.setDiscValuesForRange(0, 2, 1);
            Attribute emotionalStatusAtt = new Attribute(domain, ATT_EMOTIONAL_STATUS, Attribute.AttributeType.INT);
            emotionalStatusAtt.setDiscValuesForRange(0, 5, 1);
            Attribute stateOfMindAtt = new Attribute(domain, ATT_STATE_OF_MIND, Attribute.AttributeType.INT);
            stateOfMindAtt.setDiscValuesForRange(0, 2, 1);

            stateClass.addAttribute(timingAtt);
            stateClass.addAttribute(dayTypeAtt);
            stateClass.addAttribute(locationAtt);
            stateClass.addAttribute(activityAtt);
            stateClass.addAttribute(phoneUsageAtt);
            stateClass.addAttribute(emotionalStatusAtt);
            stateClass.addAttribute(stateOfMindAtt);
        }

        new InterventionDeliveryAction(ACTION_INT_DELIVERY, domain);
        new NoAction(ACTION_NO_ACTION, domain);

        return domain;
    }

/*    public State getInitialState() {
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_HOUR_OF_DAY, 0);
        o.setValue(ATT_DAY_TYPE, DayType.WEEKDAY.ordinal());
        o.setValue(ATT_LOCATION, Location.HOME.ordinal());

        return s;
    }*/

}
