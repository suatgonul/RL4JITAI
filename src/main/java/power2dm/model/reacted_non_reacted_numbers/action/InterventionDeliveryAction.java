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
public class InterventionDeliveryAction extends SimpleAction implements FullActionModel {

    public InterventionDeliveryAction(String name, Domain domain) {
        super(name, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        ObjectInstance state = s.getFirstObjectOfClass(CLASS_STATE);
        int timing = state.getIntValForAttribute(ATT_TIME);
        int reactedInt = state.getIntValForAttribute(ATT_REACTED_INT);
        int nonReactedInt = state.getIntValForAttribute(ATT_NON_REACTED_INT);

        //if user reacts to intervention we should determine the next state accordingly
        ReactNonReactP2DMEnvironmentSimulator simulator = ((ReactNonReactP2DMDomain) domain).getSimulator();
        boolean userReacted = simulator.simulateUserReactionToIntervention(s, groundedAction);
        if (userReacted) {
            reactedInt++;
        } else {
            nonReactedInt++;
        }

        simulator.updateEnvironment(s, groundedAction);

        // update the state by updating state's parameters
        s = s.setObjectsValue(state.getName(), ATT_TIME, timing + 1);
        s = s.setObjectsValue(state.getName(), ATT_REACTED_INT, reactedInt);
        s = s.setObjectsValue(state.getName(), ATT_NON_REACTED_INT, nonReactedInt);
        s = s.setObjectsValue(state.getName(), ATT_LOCATION, simulator.getLocation().ordinal());

        return s;
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
