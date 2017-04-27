package tez.algorithm;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import tez.simulator.RealWorld;

import static tez.algorithm.SelfManagementDomainGenerator.ACTION_INT_DELIVERY;

/**
 * Created by suatgonul on 12/23/2016.
 */
public class ReactionRewardFunction implements RewardFunction {
    private Environment environment;

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
        if (a.action.getName().equals(ACTION_INT_DELIVERY)) {
            boolean userReacted = ((RealWorld) environment).userReacted();
            if(userReacted) {
                return 1000;
            } else {
                return -1;
            }
        }
        return 0;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}