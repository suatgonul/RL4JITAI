package power2dm.model;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.SADomain;

/**
 * Created by suat on 08-Apr-16.
 */
public abstract class P2DMDomain extends SADomain {

    private EnvironmentSimulator simulator;

    public P2DMDomain() {
        super();
        initializeDomain();
    }

    /**
     * The implementations of this method should initiate the domain with {@link burlap.oomdp.core.Attribute}, {@link
     * burlap.oomdp.core.ObjectClass} and {@link burlap.oomdp.singleagent.Action} declarations that are associated the
     * initialized domain
     */
    protected abstract void initializeDomain();

    /**
     * Initalizes the start state for the domain by appropriately configuring the state parameters specific to domain
     *
     * @return
     */
    public abstract State getInitialState();

    public void setSimulator(EnvironmentSimulator simulator) {
        this.simulator = simulator;
    }

    public EnvironmentSimulator getSimulator() {
        return simulator;
    }
}
