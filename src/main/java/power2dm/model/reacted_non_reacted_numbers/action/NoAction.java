package power2dm.model.reacted_non_reacted_numbers.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain;
import power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMEnvironmentSimulator;

import java.util.List;

import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.*;

/**
 * Created by suat on 08-Apr-16.
 */
public class NoAction extends SimpleAction implements FullActionModel {

    public NoAction(String name, Domain domain) {
        super(name, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        ObjectInstance state = s.getFirstObjectOfClass(CLASS_STATE);
        int timing = state.getIntValForAttribute(ATT_TIME);
//        int timingIntAmount = state.getIntValForAttribute(ATT_TIMING_INT);
//        int totalIntAmount = state.getIntValForAttribute(ATT_TOTAL_INT);
        int reactedIntAmount = state.getIntValForAttribute(ATT_REACTED_INT);

        ReactNonReactP2DMEnvironmentSimulator simulator = ((ReactNonReactP2DMDomain) domain).getSimulator();
        simulator.updateEnvironment();

        // update the state by updating state's parameters
        s = s.setObjectsValue(state.getName(), ATT_TIME, timing + 1);
//        s = s.setObjectsValue(state.getName(), ATT_TIMING_INT, 0);
//        s = s.setObjectsValue(state.getName(), ATT_TOTAL_INT, totalIntAmount);
        s = s.setObjectsValue(state.getName(), ATT_REACTED_INT, reactedIntAmount);
        s = s.setObjectsValue(state.getName(), ATT_LOCATION, simulator.getLocation().ordinal());

        return s;
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
