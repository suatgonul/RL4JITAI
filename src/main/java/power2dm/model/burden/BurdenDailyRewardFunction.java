package power2dm.model.burden;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import power2dm.model.burden.state.P2DMState;

import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.*;

/**
 * Created by suat on 08-Apr-16.
 */
public class BurdenDailyRewardFunction implements RewardFunction {

    public double reward(State s, GroundedAction a, State sPrime) {
        P2DMState curState = (P2DMState) s;
        P2DMState nextState = (P2DMState) sPrime;

        if(a.action.getName().equals(ACTION_INT_DELIVERY)) {
            if (nextState.getReactedInt() > curState.getReactedInt()) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }
}
