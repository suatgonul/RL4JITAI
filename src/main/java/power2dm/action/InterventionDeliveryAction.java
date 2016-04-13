package power2dm.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.P2DMDomain;
import power2dm.Timing;
import power2dm.UserPreference;

import java.util.List;
import java.util.Random;

import static power2dm.P2DMDomain.*;

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
        int timing = state.getIntValForAttribute(ATT_TIMING);
//        int timingIntAmount = state.getIntValForAttribute(ATT_TIMING_INT);
//        int totalIntAmount = state.getIntValForAttribute(ATT_TOTAL_INT);
        int timingReactedInt = state.getIntValForAttribute(ATT_TIMING_REACTED_INT);

        //if user reacts to intervention we should determine the next state accordingly
        boolean userReacted = getReactionProbability(timing);
        if (userReacted) {
            timingReactedInt++;
        }

        // update the state by updating state's parameters
        s = s.setObjectsValue(state.getName(), ATT_TIMING, Timing.values()[(timing + 1) % Timing.values().length].ordinal());
//        s = s.setObjectsValue(state.getName(), ATT_TIMING_INT, timingIntAmount + 1);
//        s = s.setObjectsValue(state.getName(), ATT_TOTAL_INT, totalIntAmount + 1);
        s = s.setObjectsValue(state.getName(), ATT_TIMING_REACTED_INT, timingReactedInt);


        return s;
    }

    private boolean getReactionProbability(int timing) {
        UserPreference preferences = ((P2DMDomain) domain).getUserPreference();
        // if we are in an AFTERNOON state we should consider user preference on EVENING
        Timing targetTiming = Timing.values()[(timing + 1) % Timing.values().length];

        Random r = new Random();
        int low = 0;
        int high = 100;
        int rInt = r.nextInt(high - low) + low;

        if (preferences.getPreference(targetTiming) == 1) {
            if (rInt >= 25) {
                return true;
            } else {
                return false;
            }
        } else {
            if (rInt >= 90) {
                return true;
            } else {
                return false;
            }
        }
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
