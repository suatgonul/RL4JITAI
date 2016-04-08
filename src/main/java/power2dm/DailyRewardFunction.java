package power2dm;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

import static power2dm.P2DMDomain.ATT_TIMING_REACTED_INT;
import static power2dm.P2DMDomain.ATT_TOTAL_INT;
import static power2dm.P2DMDomain.CLASS_STATE;

/**
 * Created by suat on 08-Apr-16.
 */
public class DailyRewardFunction implements RewardFunction{
    public double reward(State s, GroundedAction a, State sPrime) {
        int sTotalInt = s.getObject(CLASS_STATE).getIntValForAttribute(ATT_TOTAL_INT);
        int sPrimeTotalInt = sPrime.getObject(CLASS_STATE).getIntValForAttribute(ATT_TOTAL_INT);
        int sReactedInt = s.getObject(CLASS_STATE).getIntValForAttribute(ATT_TIMING_REACTED_INT);
        int sPrimeReactedInt = sPrime.getObject(CLASS_STATE).getIntValForAttribute(ATT_TIMING_REACTED_INT);

        if(sPrimeTotalInt > sTotalInt) {
            if(sPrimeReactedInt > sReactedInt) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }
}
