package power2dm.algorithm;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.ATT_TIME;
import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.CLASS_STATE;

/**
 * Created by suat on 08-Apr-16.
 */
public class DailyTerminalFunction implements TerminalFunction {
    public boolean isTerminal(State s) {
        int time = s.getObject(CLASS_STATE).getIntValForAttribute(ATT_TIME);

        // if the timing is evening
        if (time == 24) {
            return true;
        } else {
            return false;
        }
    }
}
