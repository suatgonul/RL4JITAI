package power2dm.model.habit.year.periodic;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import power2dm.algorithm.P2DMDomain;
import power2dm.model.habit.year.periodic.action.HabitYearPeriodicInterventionDeliveryAction;
import power2dm.model.habit.year.periodic.action.HabitYearPeriodicNoAction;
import power2dm.model.habit.year.periodic.environment.HabitYearPeriodicP2DMEnvironmentSimulator;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitYearPeriodicP2DMDomain extends P2DMDomain {
    public static final String ATT_CALORIE_INTAKE_FREQUENCY_FIRST_PERIOD = "calorieIntakeFrequencyFirstPeriod";
    public static final String ATT_CALORIE_INTAKE_FREQUENCY_SECOND_PERIOD = "calorieIntakeFrequencySecondPeriod";
    public static final String ATT_CALORIE_INTAKE_FREQUENCY_THIRD_PERIOD = "calorieIntakeFrequencyThirdPeriod";
    public static final String ATT_CALORIE_INTAKE_FREQUENCY_FOURTH_PERIOD = "calorieIntakeFrequencyFourthPeriod";
    public static final String ATT_CALORIE_INTAKE_FREQUENCY_FIFTH_PERIOD = "calorieIntakeFrequencyFifthPeriod";
    public static final String ATT_CAL_INTAKE_AUTOMATION_RATIO_FIRST_PERIOD = "calorieIntakeAutomationRatioFirstPeriod";
    public static final String ATT_CAL_INTAKE_AUTOMATION_RATIO_SECOND_PERIOD = "calorieIntakeAutomationRatioSecondPeriod";
    public static final String ATT_CAL_INTAKE_AUTOMATION_RATIO_THIRD_PERIOD = "calorieIntakeAutomationRatioThirdPeriod";
    public static final String ATT_CAL_INTAKE_AUTOMATION_RATIO_FOURTH_PERIOD = "calorieIntakeAutomationRatioFourthPeriod";
    public static final String ATT_CAL_INTAKE_AUTOMATION_RATIO_FIFTH_PERIOD = "calorieIntakeAutomationRatioFifthPeriod";
    public static final String ATT_CAL_INTAKE_PERIOD = "calorieIntakePeriod";

    public static final String CLASS_STATE = "state";

    public static final String ACTION_INT_DELIVERY = "intervention_delivery";
    public static final String ACTION_NO_ACTION = "no_action";

    private HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator;

    public HabitYearPeriodicP2DMDomain() {
        super(null);
        initializeDomain();
    }

    protected void initializeDomain() {
        Attribute calorieIntakeFrequencyAttFirstPeriod = new Attribute(this, ATT_CALORIE_INTAKE_FREQUENCY_FIRST_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeFrequencyAttSecondPeriod = new Attribute(this, ATT_CALORIE_INTAKE_FREQUENCY_SECOND_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeFrequencyAttThirdPeriod = new Attribute(this, ATT_CALORIE_INTAKE_FREQUENCY_THIRD_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeFrequencyAttFourthPeriod = new Attribute(this, ATT_CALORIE_INTAKE_FREQUENCY_FOURTH_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeFrequencyAttFifthPeriod = new Attribute(this, ATT_CALORIE_INTAKE_FREQUENCY_FIFTH_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeAutomationRatioAttFirstPeriod = new Attribute(this, ATT_CAL_INTAKE_AUTOMATION_RATIO_FIRST_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeAutomationRatioAttSecondPeriod = new Attribute(this, ATT_CAL_INTAKE_AUTOMATION_RATIO_SECOND_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeAutomationRatioAttThirdPeriod = new Attribute(this, ATT_CAL_INTAKE_AUTOMATION_RATIO_THIRD_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeAutomationRatioAttFourthPeriod = new Attribute(this, ATT_CAL_INTAKE_AUTOMATION_RATIO_FOURTH_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeAutomationRatioAttFifthPeriod = new Attribute(this, ATT_CAL_INTAKE_AUTOMATION_RATIO_FIFTH_PERIOD, Attribute.AttributeType.INT);
        Attribute calorieIntakeDurationAtt = new Attribute(this, ATT_CAL_INTAKE_PERIOD, Attribute.AttributeType.INT);
//        Attribute locationAtt = new Attribute(this, ATT_LOCATION, Attribute.AttributeType.INT);
//        locationAtt.setDiscValuesForRange(0, 3, 1);


        ObjectClass stateClass = new ObjectClass(this, CLASS_STATE);
        stateClass.addAttribute(calorieIntakeFrequencyAttFirstPeriod);
        stateClass.addAttribute(calorieIntakeFrequencyAttSecondPeriod);
        stateClass.addAttribute(calorieIntakeFrequencyAttThirdPeriod);
        stateClass.addAttribute(calorieIntakeFrequencyAttFourthPeriod);
//        stateClass.addAttribute(calorieIntakeFrequencyAttFifthPeriod);
        stateClass.addAttribute(calorieIntakeAutomationRatioAttFirstPeriod);
        stateClass.addAttribute(calorieIntakeAutomationRatioAttSecondPeriod);
        stateClass.addAttribute(calorieIntakeAutomationRatioAttThirdPeriod);
        stateClass.addAttribute(calorieIntakeAutomationRatioAttFourthPeriod);
//        stateClass.addAttribute(calorieIntakeAutomationRatioAttFifthPeriod);
        stateClass.addAttribute(calorieIntakeDurationAtt);
//        stateClass.addAttribute(locationAtt);

        new HabitYearPeriodicInterventionDeliveryAction(ACTION_INT_DELIVERY, this);
        new HabitYearPeriodicNoAction(ACTION_NO_ACTION, this);

//        simulator = new HabitYearP2DMEnvironmentSimulator();
    }

    public State getInitialState() {
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(getObjectClass(CLASS_STATE), CLASS_STATE));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_CALORIE_INTAKE_FREQUENCY_FIRST_PERIOD, -1);
        o.setValue(ATT_CALORIE_INTAKE_FREQUENCY_SECOND_PERIOD, -1);
        o.setValue(ATT_CALORIE_INTAKE_FREQUENCY_THIRD_PERIOD, -1);
        o.setValue(ATT_CALORIE_INTAKE_FREQUENCY_FOURTH_PERIOD, -1);
        //o.setValue(ATT_CALORIE_INTAKE_FREQUENCY_FIFTH_PERIOD, -1);
        o.setValue(ATT_CAL_INTAKE_AUTOMATION_RATIO_FIRST_PERIOD, -1);
        o.setValue(ATT_CAL_INTAKE_AUTOMATION_RATIO_SECOND_PERIOD, -1);
        o.setValue(ATT_CAL_INTAKE_AUTOMATION_RATIO_THIRD_PERIOD, -1);
        o.setValue(ATT_CAL_INTAKE_AUTOMATION_RATIO_FOURTH_PERIOD, -1);
        //o.setValue(ATT_CAL_INTAKE_AUTOMATION_RATIO_FIFTH_PERIOD, -1);
        o.setValue(ATT_CAL_INTAKE_PERIOD, 0);
//        o.setValue(ATT_LOCATION, 0);

        return s;
    }

    public HabitYearPeriodicP2DMEnvironmentSimulator getEnvironmentSimulator() {
        return environmentSimulator;
    }

    public void setEnvironmentSimulator(HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }
}
