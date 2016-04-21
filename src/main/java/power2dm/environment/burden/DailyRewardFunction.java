package power2dm.environment.burden;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import power2dm.environment.burden.state.P2DMState;

import static power2dm.environment.reacted_non_reacted_numbers.P2DMDomain.*;

/**
 * Created by suat on 08-Apr-16.
 */
public class DailyRewardFunction implements RewardFunction {

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
