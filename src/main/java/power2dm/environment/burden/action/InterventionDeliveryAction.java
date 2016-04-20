package power2dm.environment.burden.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.environment.burden.P2DMDomain;
import power2dm.environment.burden.P2DMEnvironmentSimulator;
import power2dm.environment.burden.state.P2DMState;

import java.util.List;

import static power2dm.environment.burden.P2DMDomain.*;

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
        double burdenCoefficient = state.getRealValForAttribute(ATT_BURDEN_COEFF);
        int reactedInt = ((P2DMState) s).getReactedInt();
        int nonReactedInt = ((P2DMState) s).getNonReactedInt();

        //if user reacts to intervention we should determine the next state accordingly
        P2DMEnvironmentSimulator simulator = ((P2DMDomain) domain).getSimulator();
        boolean userReacted = simulator.simulateUserReactionToIntervention();
        if (userReacted) {
            reactedInt++;
        } else {
            nonReactedInt++;
        }

        simulator.updateEnvironment(this);

        // update the state by updating state's parameters
        s = s.setObjectsValue(state.getName(), ATT_TIME, timing + 1);
        s = s.setObjectsValue(state.getName(), ATT_BURDEN_COEFF, burdenCoefficient * 0.5);
        s = s.setObjectsValue(state.getName(), ATT_LOCATION, simulator.getLocation().ordinal());
        ((P2DMState) s).setReactedInt(reactedInt);
        ((P2DMState) s).setNonReactedInt(nonReactedInt);

        return s;
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
