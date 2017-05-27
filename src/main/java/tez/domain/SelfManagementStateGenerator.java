package tez.domain;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.states.State;
import tez.environment.SelfManagementEnvironment;
import tez.environment.simulator.SimulatedWorld;

/**
 * Created by suatgonul on 4/30/2017.
 */
public class SelfManagementStateGenerator implements StateGenerator {

    private SelfManagementEnvironment selfManagementEnvironment;

    public SelfManagementStateGenerator(SelfManagementEnvironment selfManagementEnvironment) {
        this.selfManagementEnvironment = selfManagementEnvironment;
    }

    @Override
    public State generateState() {
        return selfManagementEnvironment.getStateFromCurrentContext();
    }
}
