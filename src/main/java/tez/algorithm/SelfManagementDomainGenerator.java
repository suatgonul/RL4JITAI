package tez.algorithm;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.environment.Environment;

/**
 * Created by suatgonul on 4/20/2017.
 */
public class SelfManagementDomainGenerator implements DomainGenerator {
    public static final String ATT_HOUR_OF_DAY = "hourOfDay";
    public static final String ATT_LOCATION = "location";
    public static final String ATT_DAY_TYPE = "dayType";
    public static final String ATT_PHONE_USAGE = "phoneUsage";
    public static final String ATT_STATE_OF_MIND = "stateOfMind";
    public static final String ATT_EMOTIONAL_STATUS = "emotionalStatus";

    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private Domain domain;
    private Environment environment;

    public void setEnvironment(Environment environment) {
        this.environment = environment;

        for(Action a : domain.getActions()) {
            ((SelfManagementAction) a).setEnvironment(environment);
        }
    }

    @Override
    public Domain generateDomain() {
        domain = new SADomain();

        Attribute timingAtt = new Attribute(domain, ATT_HOUR_OF_DAY, Attribute.AttributeType.INT);
        timingAtt.setDiscValuesForRange(0, 23, 1);
        Attribute locationAtt = new Attribute(domain, ATT_LOCATION, Attribute.AttributeType.INT);
        locationAtt.setDiscValuesForRange(0, 2, 1);
        Attribute dayTypeAtt = new Attribute(domain, ATT_DAY_TYPE, Attribute.AttributeType.INT);
        dayTypeAtt.setDiscValuesForRange(0, 1, 1);

        ObjectClass stateClass = new ObjectClass(domain, CLASS_STATE);
        stateClass.addAttribute(timingAtt);
        stateClass.addAttribute(dayTypeAtt);
        stateClass.addAttribute(locationAtt);

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
