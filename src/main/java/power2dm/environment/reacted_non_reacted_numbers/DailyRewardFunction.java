package power2dm.environment.reacted_non_reacted_numbers;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

import static power2dm.environment.reacted_non_reacted_numbers.P2DMDomain.*;

/**
 * Created by suat on 08-Apr-16.
 */
public class DailyRewardFunction implements RewardFunction {

    public double reward(State s, GroundedAction a, State sPrime) {
//        int sTotalInt = s.getObject(CLASS_STATE).getIntValForAttribute(ATT_TOTAL_INT);
//        int sPrimeTotalInt = sPrime.getObject(CLASS_STATE).getIntValForAttribute(ATT_TOTAL_INT);
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
    }
}
