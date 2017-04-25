package tez.algorithm;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;

import java.util.Random;

/**
 * Created by suatgonul on 12/23/2016.
 */
public class ReactionRewardFunction implements RewardFunction {
    private Environment environment;

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
        int sReactedInt = s.getObject(CLASS_STATE).getIntValForAttribute(ATT_REACTED_INT);
        int sPrimeReactedInt = sPrime.getObject(CLASS_STATE).getIntValForAttribute(ATT_REACTED_INT);
        int sNonReactedInt = s.getObject(CLASS_STATE).getIntValForAttribute(ATT_NON_REACTED_INT);
        int sPrimeNonReactedInt = sPrime.getObject(CLASS_STATE).getIntValForAttribute(ATT_NON_REACTED_INT);

        if(a.action.getName().equals(ACTION_INT_DELIVERY)) {
            if (sPrimeReactedInt > sReactedInt) {
                return sPrimeReactedInt * 2;
            } if(sPrimeNonReactedInt > sNonReactedInt) {
                return sPrimeNonReactedInt * -2;
            }
        }
        return 0;
        return new Random().nextInt(3) == 2.0 ? new Random().nextInt(100) + 1 : new Random().nextInt(80) + 1;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
