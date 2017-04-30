package tez.algorithm.action;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.environment.Environment;
import tez.simulator.RealWorld;

import java.util.List;

/**
 * Created by suatgonul on 4/24/2017.
 */
public abstract class SelfManagementAction extends SimpleAction implements FullActionModel {
    private Environment environment;

    public SelfManagementAction(String name, Domain domain) {
        super(name, domain);
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        // advance the environment state
        return ((RealWorld) environment).getNextState();
    }

    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
        return null;
    }
}
