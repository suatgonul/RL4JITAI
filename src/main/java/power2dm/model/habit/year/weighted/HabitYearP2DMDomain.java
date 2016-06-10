package power2dm.model.habit.year.weighted;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import power2dm.algorithm.P2DMDomain;
import power2dm.model.habit.year.weighted.action.HabitYearInterventionDeliveryAction;
import power2dm.model.habit.year.weighted.action.HabitYearNoAction;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitYearP2DMDomain extends P2DMDomain {
    public static final String ATT_CAL_INTAKE_FREQUENCY = "calorieIntakeFrequency";
    public static final String ATT_CAL_INTAKE_AUTOMATION_RATIO = "calorieIntakeAutomationRatio";
    public static final String ATT_CAL_INTAKE_PERIOD = "calorieIntakePeriod";

    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private HabitYearP2DMEnvironmentSimulator environmentSimulator;

    public HabitYearP2DMDomain() {
        super(null);
        initializeDomain();
    }

    protected void initializeDomain() {
        Attribute calorieIntakeFrequencyAtt = new Attribute(this, ATT_CAL_INTAKE_FREQUENCY, Attribute.AttributeType.INT);
        Attribute calorieIntakeAutomationRatioAtt = new Attribute(this, ATT_CAL_INTAKE_AUTOMATION_RATIO, Attribute.AttributeType.INT);
        Attribute calorieIntakeDurationAtt = new Attribute(this, ATT_CAL_INTAKE_PERIOD, Attribute.AttributeType.INT);
//        Attribute locationAtt = new Attribute(this, ATT_LOCATION, Attribute.AttributeType.INT);
//        locationAtt.setDiscValuesForRange(0, 3, 1);


        ObjectClass stateClass = new ObjectClass(this, CLASS_STATE);
        stateClass.addAttribute(calorieIntakeFrequencyAtt);
        stateClass.addAttribute(calorieIntakeAutomationRatioAtt);
        stateClass.addAttribute(calorieIntakeDurationAtt);
//        stateClass.addAttribute(locationAtt);

        new HabitYearInterventionDeliveryAction(ACTION_INT_DELIVERY, this);
        new HabitYearNoAction(ACTION_NO_ACTION, this);

//        simulator = new HabitYearP2DMEnvironmentSimulator();
    }

    public State getInitialState() {
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_STATE), CLASS_STATE));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_CAL_INTAKE_FREQUENCY, 0);
        o.setValue(ATT_CAL_INTAKE_AUTOMATION_RATIO, 0);
        o.setValue(ATT_CAL_INTAKE_PERIOD, 0);
//        o.setValue(ATT_LOCATION, 0);

        return s;
    }

    public HabitYearP2DMEnvironmentSimulator getEnvironmentSimulator() {
        return environmentSimulator;
    }

    public void setEnvironmentSimulator(HabitYearP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }
}
