package tez.domain;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import tez.environment.real.RealWorld;
import tez.environment.simulator.SimulatedWorld;

import static tez.domain.SelfManagementDomainGenerator.ACTION_INT_DELIVERY;

/**
 * Created by suatgonul on 12/23/2016.
 */
public class SelfManagementRewardFunction implements RewardFunction {
    private Environment environment;

    private static double REWARD_REACTION_TO_INTERVENTION = 5;
    private static double REWARD_NON_REACTION_TO_INTERVENTION = -2;
    private static double REWARD_NO_INTERVENTION = -1;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
        if (a.action.getName().equals(ACTION_INT_DELIVERY)) {
            boolean userReacted = false;
            if(environment instanceof SimulatedWorld) {
                userReacted = ((SimulatedWorld) environment).getLastUserReaction();
            } else if (environment instanceof RealWorld){
                userReacted = ((RealWorld) environment).getLastUserReaction();
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

    public static double getRewardReactionToIntervention() {
        return REWARD_REACTION_TO_INTERVENTION;
    }

    public static double getRewardNonReactionToIntervention() {
        return REWARD_NON_REACTION_TO_INTERVENTION;
    }

    public static double getRewardNoIntervention() {
        return REWARD_NO_INTERVENTION;
    }
}
