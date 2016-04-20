package power2dm.environment.burden;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.singleagent.SADomain;
import power2dm.environment.burden.action.InterventionDeliveryAction;
import power2dm.environment.burden.action.NoAction;
import power2dm.environment.burden.state.P2DMState;

/**
 * Created by suat on 08-Apr-16.
 */
public class P2DMDomain extends SADomain {
    public static final String ATT_TIME = "time";
    public static final String ATT_BURDEN_COEFF = "burden_coefficient";
    public static final String ATT_LOCATION = "location";

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
        Attribute burdenCoeffAtt = new Attribute(this, ATT_BURDEN_COEFF, Attribute.AttributeType.REAL);
        burdenCoeffAtt.setLims(0,1);
        Attribute locationAtt = new Attribute(this, ATT_LOCATION, Attribute.AttributeType.INT);
        locationAtt.setDiscValuesForRange(0, 3, 1);

        ObjectClass stateClass = new ObjectClass(this, CLASS_STATE);
        stateClass.addAttribute(timingAtt);
        stateClass.addAttribute(burdenCoeffAtt);
        stateClass.addAttribute(locationAtt);

        new InterventionDeliveryAction(ACTION_INT_DELIVERY, this);
        new NoAction(ACTION_NO_ACTION, this);

        simulator = new P2DMEnvironmentSimulator(this);
    }

    public P2DMState getInitialState() {
        P2DMState s = new P2DMState();
        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_STATE), CLASS_STATE));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_TIME, 0);
        o.setValue(ATT_BURDEN_COEFF, 1);
        o.setValue(ATT_LOCATION, 0);

        return s;
    }

    public P2DMEnvironmentSimulator getSimulator() {
        return simulator;
    }
}
