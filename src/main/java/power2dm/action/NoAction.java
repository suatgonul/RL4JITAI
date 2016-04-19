package power2dm.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import power2dm.P2DMDomain;
import power2dm.P2DMEnvironmentSimulator;

import java.util.ArrayList;
import java.util.List;

import static power2dm.P2DMDomain.*;

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

        P2DMEnvironmentSimulator simulator = ((P2DMDomain) domain).getSimulator();
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

        List<TransitionProbability> transitions = new ArrayList<TransitionProbability>();

        ObjectInstance state = s.getFirstObjectOfClass(CLASS_STATE);
        int timing = state.getIntValForAttribute(ATT_TIME);
        int reactedInt = state.getIntValForAttribute(ATT_REACTED_INT);
        int nonReactedInt = state.getIntValForAttribute(ATT_NON_REACTED_INT);

        State ns = s.copy();
        ns = ns.setObjectsValue(state.getName(), ATT_TIME, timing + 1);
        ns = ns.setObjectsValue(state.getName(), ATT_REACTED_INT, reactedInt);
        ns = ns.setObjectsValue(state.getName(), ATT_NON_REACTED_INT, nonReactedInt);
        ns = ns.setObjectsValue(state.getName(), ATT_LOCATION, 0);
        TransitionProbability tp = new TransitionProbability(ns, 1);
        transitions.add(tp);

        return transitions;
    }
}
