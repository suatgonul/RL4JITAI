package tez2.domain;

/**
 * Created by suatgonul on 12/23/2016.
 */

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

public class DayTerminalFunction implements TerminalFunction {
    public boolean isTerminal(State s) {
        return s instanceof TerminalState;
    }
}
