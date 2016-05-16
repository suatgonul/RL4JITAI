package power2dm.algorithm;

import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;

import java.util.List;
import java.util.Random;

/**
 * Created by suat on 13-May-16.
 */
public class P2DMRandomStateGenerator implements StateGenerator {

    private List<State> reachableStates;
    private Random random;

    /**
     * Will discover the reachable states from which to randomly select. Reachable states found using a {@link burlap.oomdp.statehashing.SimpleHashableStateFactory} with identifier dependence.
     * @param domain the domain from which states will be drawn.
     * @param seedState the seed state from which the reachable states will be found.
     */
    public P2DMRandomStateGenerator(SADomain domain, State seedState, TerminalFunction tf) {
        HashableStateFactory hashFactory = new SimpleHashableStateFactory(false);
        this.reachableStates = StateReachability.getReachableStates(seedState, domain, hashFactory, tf);
        this.random = new Random();
    }

    public State generateState() {
        return this.reachableStates.get(this.random.nextInt(this.reachableStates.size()));
    }
}
