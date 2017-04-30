package tez.algorithm;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.states.State;
import tez.simulator.RealWorld;

/**
 * Created by suatgonul on 4/30/2017.
 */
public class SelfManagementStateGenerator implements StateGenerator {

    private RealWorld realWorld;

    public SelfManagementStateGenerator(RealWorld realWorld) {
        this.realWorld = realWorld;
    }

    @Override
    public State generateState() {
        return realWorld.getStateFromCurrentContext();
    }
}
