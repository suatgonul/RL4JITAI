package tez.algorithm;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import tez.persona.Activity;
import tez.persona.TimePlan;
import tez.simulator.RealWorld;

import java.util.UUID;

import static tez.model.Constants.ATT_LOCATION;
import static tez.model.Constants.ATT_TIME;

/**
 * Created by suatgonul on 12/24/2016.
 */
public class SelfManagementEnvironment extends SimulatedEnvironment {
    private RealWorld realWorld;
    private TimePlan currentTimePlan;

    public SelfManagementEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf) {
        super(domain, rf, tf);
    }

    public void setRealWorld(RealWorld realWorld) {
        this.realWorld = realWorld;
    }

    public void setCurrentTimePlan(TimePlan currentTimePlan) {
        this.currentTimePlan = currentTimePlan;
    }

    private State generateInitialState() {
        Activity firstActivity = currentTimePlan.getActivities().get(0);
        String objectInstanceId = UUID.randomUUID().toString();
        State state = new MutableState();
        state.setObjectsValue(objectInstanceId, ATT_TIME, realWorld.getCurrentTime());
        state.setObjectsValue(objectInstanceId, ATT_LOCATION, firstActivity.getContext().getLocation());
//        state.setObjectsValue(objectInstanceId, ATT_, firstActivity.getContext().);
//        state.setObjectsValue(objectInstanceId, ATT_LOCATION, firstActivity.getContext().getLocation());
//        state.setObjectsValue(objectInstanceId, ATT_LOCATION, firstActivity.getContext().getLocation());
//        state.setObjectsValue(objectInstanceId, ATT_LOCATION, firstActivity.getContext().getLocation());
        return state;
    }
}
