package power2dm.model.habit.year.periodic.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain;
import power2dm.model.habit.year.periodic.environment.HabitYearPeriodicP2DMEnvironmentSimulator;

import java.util.List;

import static power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain.*;
import static power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain.ATT_CAL_INTAKE_AUTOMATION_RATIO_FOURTH_PERIOD;
import static power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain.ATT_CAL_INTAKE_PERIOD;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitYearPeriodicNoAction extends SimpleAction implements FullActionModel {

    public HabitYearPeriodicNoAction(String name, Domain domain) {
        super(name, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        HabitYearPeriodicP2DMEnvironmentSimulator simulator = ((HabitYearPeriodicP2DMDomain) domain).getEnvironmentSimulator();
        simulator.applyAction(s, groundedAction);
        return getNextState(s, groundedAction);
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }

    private State getNextState(State s, GroundedAction groundedAction) {
        State ns = s.copy();
        ObjectInstance state = ns.getFirstObjectOfClass(CLASS_STATE);

        // update the state by updating state's parameters
        HabitYearPeriodicP2DMEnvironmentSimulator simulator = ((HabitYearPeriodicP2DMDomain) domain).getEnvironmentSimulator();
        ns = ns.setObjectsValue(state.getName(), ATT_CALORIE_INTAKE_FREQUENCY_FIRST_PERIOD, simulator.getFrequencyForPeriod(0));
        ns = ns.setObjectsValue(state.getName(), ATT_CALORIE_INTAKE_FREQUENCY_SECOND_PERIOD, simulator.getFrequencyForPeriod(1));
        ns = ns.setObjectsValue(state.getName(), ATT_CALORIE_INTAKE_FREQUENCY_THIRD_PERIOD, simulator.getFrequencyForPeriod(2));
        ns = ns.setObjectsValue(state.getName(), ATT_CALORIE_INTAKE_FREQUENCY_FOURTH_PERIOD, simulator.getFrequencyForPeriod(3));
        //ns = ns.setObjectsValue(state.getName(), ATT_CALORIE_INTAKE_FREQUENCY_FIFTH_PERIOD, simulator.getFrequencyForPeriod(4));
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_AUTOMATION_RATIO_FIRST_PERIOD, simulator.getAutomationRatioForPeriod(0));
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_AUTOMATION_RATIO_SECOND_PERIOD, simulator.getAutomationRatioForPeriod(1));
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_AUTOMATION_RATIO_THIRD_PERIOD, simulator.getAutomationRatioForPeriod(2));
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_AUTOMATION_RATIO_FOURTH_PERIOD, simulator.getAutomationRatioForPeriod(3));
        //ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_AUTOMATION_RATIO_FIFTH_PERIOD, simulator.getAutomationRatioForPeriod(4));
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_PERIOD, simulator.getPeriod());
//        s = s.setObjectsValue(state.getName(), ATT_LOCATION, simulator.getContext().ordinal());

        return ns;
    }
}
