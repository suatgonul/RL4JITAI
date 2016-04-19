package power2dm;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.SADomain;
import power2dm.action.InterventionDeliveryAction;
import power2dm.action.NoAction;

/**
 * Created by suat on 08-Apr-16.
 */
public class P2DMDomain extends SADomain {
    public static final String ATT_TIME = "time";
//    public static final String ATT_TIMING_INT = "timing_interventions";
//    public static final String ATT_TOTAL_INT = "total_interventions";
    public static final String ATT_REACTED_INT = "reacted_interventions";
    public static final String ATT_NON_REACTED_INT = "non_reacted_interventions";
    public static final String ATT_LOCATION = "location";

//    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private P2DMEnvironmentSimulator simulator;

    public P2DMDomain() {
        super();
        initializeDomain();
    }

    private void initializeDomain() {
        Attribute timingAtt = new Attribute(this, ATT_TIME, Attribute.AttributeType.INT);
        timingAtt.setDiscValuesForRange(0, 23, 1);
//        Attribute timingIntAtt = new Attribute(this, ATT_TIMING_INT, Attribute.AttributeType.INT);
//        timingIntAtt.setDiscValuesForRange(0, 2, 1);
//        Attribute totalIntAtt = new Attribute(this, ATT_TOTAL_INT, Attribute.AttributeType.INT);
//        totalIntAtt.setDiscValuesForRange(0, 24, 1);
        Attribute reactedIntAtt = new Attribute(this, ATT_REACTED_INT, Attribute.AttributeType.INT);
        reactedIntAtt.setDiscValuesForRange(0, 24, 1);
        Attribute nonReactedIntAtt = new Attribute(this, ATT_NON_REACTED_INT, Attribute.AttributeType.INT);
        nonReactedIntAtt.setDiscValuesForRange(0, 24, 1);
        Attribute locationAtt = new Attribute(this, ATT_LOCATION, Attribute.AttributeType.INT);
        locationAtt.setDiscValuesForRange(0, 3, 1);

//        ObjectClass agentClass = new ObjectClass(this, CLASS_AGENT);
//        agentClass.addAttribute(timingAtt);
//        agentClass.addAttribute(timingIntAtt);
//        agentClass.addAttribute(totalIntAtt);
//        agentClass.addAttribute(reactedIntAtt);

        ObjectClass stateClass = new ObjectClass(this, CLASS_STATE);
        stateClass.addAttribute(timingAtt);
//        stateClass.addAttribute(timingIntAtt);
//        stateClass.addAttribute(totalIntAtt);
        stateClass.addAttribute(reactedIntAtt);
        stateClass.addAttribute(nonReactedIntAtt);
        stateClass.addAttribute(locationAtt);

        new InterventionDeliveryAction(ACTION_INT_DELIVERY, this);
        new NoAction(ACTION_NO_ACTION, this);

        simulator = new P2DMEnvironmentSimulator(this);
    }

    public State getInitialState() {
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_STATE), CLASS_STATE));
//        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_AGENT), CLASS_AGENT));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_TIME, 0);
//        o.setValue(ATT_TIMING_INT, 0);
//        o.setValue(ATT_TOTAL_INT, 0);
        o.setValue(ATT_REACTED_INT, 0);
        o.setValue(ATT_NON_REACTED_INT, 0);
        o.setValue(ATT_LOCATION, 0);

        return s;
    }

    public P2DMEnvironmentSimulator getSimulator() {
        return simulator;
    }
}
