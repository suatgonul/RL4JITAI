package tez2.domain;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import tez2.environment.simulator.SimulatedWorld;

import static tez2.domain.DomainConfig.ACTION_SEND_JITAI;

/**
 * Created by suatgonul on 12/23/2016.
 */
public abstract class SelfManagementRewardFunction implements RewardFunction {
    protected Environment environment;

    protected static double REWARD_REACTION_TO_INTERVENTION = 5;
    protected static double REWARD_NON_REACTION_TO_INTERVENTION = -2;
    protected static double REWARD_NO_INTERVENTION = -1;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
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
