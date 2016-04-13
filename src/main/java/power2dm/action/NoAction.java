package power2dm.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.Timing;

import java.util.List;
import java.util.Random;

import static power2dm.P2DMDomain.*;

/**
 * Created by suat on 08-Apr-16.
 */
public class NoAction extends SimpleAction implements FullActionModel {

    public NoAction(String name, Domain domain){
        super(name, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        ObjectInstance state = s.getFirstObjectOfClass(CLASS_STATE);
        int timing = state.getIntValForAttribute(ATT_TIMING);
//        int timingIntAmount = state.getIntValForAttribute(ATT_TIMING_INT);
//        int totalIntAmount = state.getIntValForAttribute(ATT_TOTAL_INT);
        int timingReactedIntAmount = state.getIntValForAttribute(ATT_TIMING_REACTED_INT);

        //get random action
        Random r = new Random();
        int low = 0;
        int high = 1;
//        r.nextInt(high - low) + low;

        // update the state by updating state's parameters
        s = s.setObjectsValue(state.getName(), ATT_TIMING, Timing.values()[(timing + 1) % Timing.values().length].ordinal());
//        s = s.setObjectsValue(state.getName(), ATT_TIMING_INT, 0);
//        s = s.setObjectsValue(state.getName(), ATT_TOTAL_INT, totalIntAmount);
        s = s.setObjectsValue(state.getName(), ATT_TIMING_REACTED_INT, timingReactedIntAmount);

        return s;
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
