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
    public static final String ATT_TIMING = "timing";
//    public static final String ATT_TIMING_INT = "timing_interventions";
//    public static final String ATT_TOTAL_INT = "total_interventions";
    public static final String ATT_TIMING_REACTED_INT = "timing_reacted_interventions";

//    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private UserPreference preferences = new UserPreference();

    public P2DMDomain() {
        super();
        initializeDomain();
    }

    private void initializeDomain() {
        Attribute timingAtt = new Attribute(this, ATT_TIMING, Attribute.AttributeType.INT);
        timingAtt.setDiscValuesForRange(Timing.START.ordinal(), Timing.EVENING.ordinal(), 1);
//        Attribute timingIntAtt = new Attribute(this, ATT_TIMING_INT, Attribute.AttributeType.INT);
//        timingIntAtt.setDiscValuesForRange(0, 2, 1);
//        Attribute totalIntAtt = new Attribute(this, ATT_TOTAL_INT, Attribute.AttributeType.INT);
//        totalIntAtt.setDiscValuesForRange(0, 6, 1);
        Attribute timingReactedIntAtt = new Attribute(this, ATT_TIMING_REACTED_INT, Attribute.AttributeType.INT);
        timingReactedIntAtt.setDiscValuesForRange(0, 6, 1);

//        ObjectClass agentClass = new ObjectClass(this, CLASS_AGENT);
//        agentClass.addAttribute(timingAtt);
//        agentClass.addAttribute(timingIntAtt);
//        agentClass.addAttribute(totalIntAtt);
//        agentClass.addAttribute(timingReactedIntAtt);

        ObjectClass stateClass = new ObjectClass(this, CLASS_STATE);
        stateClass.addAttribute(timingAtt);
//        stateClass.addAttribute(timingIntAtt);
//        stateClass.addAttribute(totalIntAtt);
        stateClass.addAttribute(timingReactedIntAtt);

        new InterventionDeliveryAction(ACTION_INT_DELIVERY, this);
        new NoAction(ACTION_NO_ACTION, this);

        setUserPreferences();
    }

    private void setUserPreferences() {
        preferences.setPreference(Timing.EVENING, 1);
    }

    public State getInitialState() {
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_STATE), CLASS_STATE));
//        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_AGENT), CLASS_AGENT));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_TIMING, Timing.START.ordinal());
//        o.setValue(ATT_TIMING_INT, 0);
//        o.setValue(ATT_TOTAL_INT, 0);
        o.setValue(ATT_TIMING_REACTED_INT, 0);

        return s;
    }

    public UserPreference getUserPreference() {
        return preferences;
    }
}
