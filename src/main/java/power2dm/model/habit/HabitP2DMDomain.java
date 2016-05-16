package power2dm.model.habit;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import power2dm.algorithm.P2DMDomain;
import power2dm.model.habit.action.HabitInterventionDeliveryAction;
import power2dm.model.habit.action.HabitNoAction;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitP2DMDomain extends P2DMDomain {
    public static final String ATT_HOUR_OF_DAY = "hourOfDay";
    public static final String ATT_CAL_INTAKE_ENTRY = "calorieIntakeEntry";
    public static final String ATT_LOCATION = "location";

    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private NewHabitP2DMEnvironmentSimulator environmentSimulator;

    public HabitP2DMDomain() {
        super(null);
        initializeDomain();
    }

    protected void initializeDomain() {
        Attribute timingAtt = new Attribute(this, ATT_HOUR_OF_DAY, Attribute.AttributeType.INT);
        timingAtt.setDiscValuesForRange(0, 23, 1);
        Attribute habitCoeffAtt = new Attribute(this, ATT_CAL_INTAKE_ENTRY, Attribute.AttributeType.BOOLEAN);
//        Attribute locationAtt = new Attribute(this, ATT_LOCATION, Attribute.AttributeType.INT);
//        locationAtt.setDiscValuesForRange(0, 3, 1);


        ObjectClass stateClass = new ObjectClass(this, CLASS_STATE);
        stateClass.addAttribute(timingAtt);
        stateClass.addAttribute(habitCoeffAtt);
//        stateClass.addAttribute(locationAtt);

        new HabitInterventionDeliveryAction(ACTION_INT_DELIVERY, this);
        new HabitNoAction(ACTION_NO_ACTION, this);

//        simulator = new HabitP2DMEnvironmentSimulator();
    }

    public State getInitialState() {
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_STATE), CLASS_STATE));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_HOUR_OF_DAY, 0);
        o.setValue(ATT_CAL_INTAKE_ENTRY, false);
//        o.setValue(ATT_LOCATION, 0);

        return s;
    }

    public NewHabitP2DMEnvironmentSimulator getEnvironmentSimulator() {
        return environmentSimulator;
    }

    public void setEnvironmentSimulator(NewHabitP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }
}
