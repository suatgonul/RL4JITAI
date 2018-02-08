package tez2.domain.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.environment.Environment;
import tez.environment.SelfManagementEnvironment;
import tez2.algorithm.ActionRestrictingState;
import tez2.algorithm.SelfManagementSimpleGroundedAction;
import tez2.environment.simulator.JitaiSelectionEnvironment;
import tez2.environment.simulator.SimulatedWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by suatgonul on 4/24/2017.
 */
public abstract class SelfManagementAction extends SimpleAction implements FullActionModel {
    private Environment environment;
    private SelectedBy selectedBy;

    public SelfManagementAction(String name, Domain domain) {
        super(name, domain);
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public SelectedBy getSelectedBy() {
        return selectedBy;
    }

    public void setSelectedBy(SelectedBy selectedBy) {
        this.selectedBy = selectedBy;
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        // advance the environment state
        return ((SelfManagementEnvironment) environment).getNextState();
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }

    @Override
    public List<GroundedAction> getAllApplicableGroundedActions(State s) {
        GroundedAction ga = new SelfManagementSimpleGroundedAction(this);
        return this.applicableInState(s, ga) ? Arrays.asList(ga) : new ArrayList<GroundedAction>(0);
    }

    public enum SelectedBy {
        QLEARNING, STATE_CLASSIFIER, RANDOM
    }
}
