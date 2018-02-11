package tez2.domain.rf;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez2.environment.simulator.SimulatedWorld;

import static tez2.domain.DomainConfig.ACTION_SEND_JITAI;

public class JsRewardFunction extends SelfManagementRewardFunction {

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
        if (a.action.getName().equals(ACTION_SEND_JITAI)) {
            boolean userReacted = false;
            if(environment instanceof SimulatedWorld) {
                userReacted = ((SimulatedWorld) environment).isBehaviorPerformed();
            }
            if(userReacted) {
                return REWARD_REACTION_TO_INTERVENTION;
            } else {
                return REWARD_NON_REACTION_TO_INTERVENTION;
            }
        } else {
            return REWARD_NO_INTERVENTION;
        }
    }
}
