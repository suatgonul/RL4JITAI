package power2dm.model.burden.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.model.P2DMDomain;
import power2dm.model.burden.BurdenP2DMEnvironmentSimulator;
import power2dm.model.burden.state.P2DMState;

import java.util.List;

import static power2dm.model.burden.BurdenP2DMDomain.*;
import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.CLASS_STATE;

/**
 * Created by suat on 08-Apr-16.
 */
public class NoAction extends SimpleAction implements FullActionModel {

    public NoAction(String name, Domain domain) {
        super(name, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        P2DMState st = (P2DMState) s;
        ObjectInstance state = st.getFirstObjectOfClass(CLASS_STATE);
        int timing = state.getIntValForAttribute(ATT_TIME);
        int reactedInt = ((P2DMState) s).getReactedInt();
        int nonReactedInt = ((P2DMState) s).getNonReactedInt();

        BurdenP2DMEnvironmentSimulator simulator = (BurdenP2DMEnvironmentSimulator) ((P2DMDomain) domain).getSimulator();
        simulator.updateEnvironment(s, groundedAction);

        // update the state by updating state's parameters
        s = s.setObjectsValue(state.getName(), ATT_TIME, timing + 1);
        s = s.setObjectsValue(state.getName(), ATT_BURDEN_COEFF, simulator.getBurdenCoefficient());
        s = s.setObjectsValue(state.getName(), ATT_LOCATION, simulator.getLocation().ordinal());
        ((P2DMState) s).setReactedInt(reactedInt);
        ((P2DMState) s).setNonReactedInt(nonReactedInt);

        return s;
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
