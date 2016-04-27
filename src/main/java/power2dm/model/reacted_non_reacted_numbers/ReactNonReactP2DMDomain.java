package power2dm.model.reacted_non_reacted_numbers;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.SADomain;
import power2dm.model.reacted_non_reacted_numbers.action.InterventionDeliveryAction;
import power2dm.model.reacted_non_reacted_numbers.action.NoAction;

/**
 * Created by suat on 08-Apr-16.
 */
public class ReactNonReactP2DMDomain extends SADomain {
    public static final String ATT_TIME = "time";
    public static final String ATT_REACTED_INT = "reacted_interventions";
    public static final String ATT_NON_REACTED_INT = "non_reacted_interventions";
    public static final String ATT_LOCATION = "location";

    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private ReactNonReactP2DMEnvironmentSimulator simulator;

    public ReactNonReactP2DMDomain() {
        super();
        initializeDomain();
    }

    private void initializeDomain() {
        Attribute timingAtt = new Attribute(this, ATT_TIME, Attribute.AttributeType.INT);
        timingAtt.setDiscValuesForRange(0, 23, 1);
        Attribute reactedIntAtt = new Attribute(this, ATT_REACTED_INT, Attribute.AttributeType.INT);
        reactedIntAtt.setDiscValuesForRange(0, 24, 1);
        Attribute nonReactedIntAtt = new Attribute(this, ATT_NON_REACTED_INT, Attribute.AttributeType.INT);
        nonReactedIntAtt.setDiscValuesForRange(0, 24, 1);
        Attribute locationAtt = new Attribute(this, ATT_LOCATION, Attribute.AttributeType.INT);
        locationAtt.setDiscValuesForRange(0, 3, 1);


        ObjectClass stateClass = new ObjectClass(this, CLASS_STATE);
        stateClass.addAttribute(timingAtt);
        stateClass.addAttribute(reactedIntAtt);
        stateClass.addAttribute(nonReactedIntAtt);
        stateClass.addAttribute(locationAtt);

        new InterventionDeliveryAction(ACTION_INT_DELIVERY, this);
        new NoAction(ACTION_NO_ACTION, this);

        simulator = new ReactNonReactP2DMEnvironmentSimulator(this);
    }

    public State getInitialState() {
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_STATE), CLASS_STATE));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_TIME, 0);
        o.setValue(ATT_REACTED_INT, 0);
        o.setValue(ATT_NON_REACTED_INT, 0);
        o.setValue(ATT_LOCATION, 0);

        return s;
    }

    public ReactNonReactP2DMEnvironmentSimulator getSimulator() {
        return simulator;
    }
}
