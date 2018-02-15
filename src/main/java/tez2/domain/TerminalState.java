package tez2.domain;

import burlap.oomdp.core.states.MutableState;

/**
 * Created by suatgonul on 4/25/2017.
 */
public class TerminalState extends MutableState {
    @Override
    public TerminalState copy(){
        return new TerminalState();
    }
}
