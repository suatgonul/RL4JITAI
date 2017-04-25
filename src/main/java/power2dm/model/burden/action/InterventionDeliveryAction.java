package power2dm.model.burden.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.model.burden.state.P2DMState;

import java.util.List;

import static power2dm.model.burden.BurdenP2DMDomain.ATT_TIME;
import static power2dm.model.burden.BurdenP2DMDomain.CLASS_STATE;

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
        int reactedInt = ((P2DMState) s).getReactedInt();
        int nonReactedInt = ((P2DMState) s).getNonReactedInt();

        return s;
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
