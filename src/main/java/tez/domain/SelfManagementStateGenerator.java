package tez.domain;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.states.State;
import tez.environment.simulator.SimulatedWorld;

/**
 * Created by suatgonul on 4/30/2017.
 */
public class SelfManagementStateGenerator implements StateGenerator {

    private SimulatedWorld simulatedWorld;

    public SelfManagementStateGenerator(SimulatedWorld simulatedWorld) {
        this.simulatedWorld = simulatedWorld;
    }

    @Override
    public State generateState() {
        return simulatedWorld.getStateFromCurrentContext();
    }
}
