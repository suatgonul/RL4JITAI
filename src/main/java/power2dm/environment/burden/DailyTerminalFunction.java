package power2dm.environment.burden;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

import static power2dm.environment.reacted_non_reacted_numbers.P2DMDomain.ATT_TIME;
import static power2dm.environment.reacted_non_reacted_numbers.P2DMDomain.CLASS_STATE;

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
