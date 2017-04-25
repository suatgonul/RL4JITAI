package power2dm.model.habit.year.weighted.action;

import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.algorithm.P2DMDomain;
import power2dm.model.habit.year.weighted.HabitYearP2DMDomain;
import power2dm.model.habit.year.weighted.HabitYearP2DMEnvironmentSimulator;

import java.util.List;

import static power2dm.model.habit.year.weighted.HabitYearP2DMDomain.*;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitYearInterventionDeliveryAction extends SimpleAction implements FullActionModel {

    public HabitYearInterventionDeliveryAction(String name, P2DMDomain domain) {
        super(name, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        HabitYearP2DMEnvironmentSimulator simulator = ((HabitYearP2DMDomain) domain).getEnvironmentSimulator();
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
        HabitYearP2DMEnvironmentSimulator simulator = ((HabitYearP2DMDomain) domain).getEnvironmentSimulator();
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_FREQUENCY, simulator.calculateCalorieIntakeEntryFrequency());
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_AUTOMATION_RATIO, simulator.getAutomationRatio());
        ns = ns.setObjectsValue(state.getName(), ATT_CAL_INTAKE_PERIOD, simulator.getPeriod());
//        s = s.setObjectsValue(state.getName(), ATT_LOCATION, simulator.getContext().ordinal());

        return ns;
    }
}
