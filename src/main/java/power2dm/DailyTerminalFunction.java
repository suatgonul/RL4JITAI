package power2dm;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

import static power2dm.P2DMDomain.ATT_TIMING;
import static power2dm.P2DMDomain.CLASS_STATE;

/**
 * Created by suat on 08-Apr-16.
 */
public class DailyTerminalFunction implements TerminalFunction {
    public boolean isTerminal(State s) {
        Timing timing = Timing.values()[s.getObject(CLASS_STATE).getIntValForAttribute(ATT_TIMING)];

        // if the timing is evening
        if (timing.equals(Timing.EVENING)) {
            return true;
        } else {
            return false;
        }
    }
}
