package tez.algorithm;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

/**
 * Created by suatgonul on 12/23/2016.
 */
public class DayTerminalFunction implements TerminalFunction {
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
