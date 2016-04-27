package power2dm.model.burden.state;

import burlap.oomdp.core.states.MutableState;

/**
 * Created by suat on 20-Apr-16.
 */
public class P2DMState extends MutableState {
    private int reactedInt = 0;
    private int nonReactedInt = 0;

    public P2DMState() {
        super();
    }

    private P2DMState(P2DMState s) {
        super(s);
    }

    public int getReactedInt() {
        return reactedInt;
    }

    public void setReactedInt(int reactedInt) {
        this.reactedInt = reactedInt;
    }

    public int getNonReactedInt() {
        return nonReactedInt;
    }

    public void setNonReactedInt(int nonReactedInt) {
        this.nonReactedInt = nonReactedInt;
    }

    @Override
    public P2DMState copy() {
        P2DMState s = new P2DMState(this);
        s.reactedInt = this.reactedInt;
        s.nonReactedInt = this.nonReactedInt;
        return s;
    }

    public boolean specialEquals(Object other) {
        if(!super.equals(other)) {
           return false;
        }

        if(!(other instanceof P2DMState)){
            return false;
        }

        P2DMState otherState = (P2DMState) other;
        if(reactedInt != otherState.reactedInt) {
            return false;
        }

        if(nonReactedInt != otherState.nonReactedInt) {
            return false;
        }

        return true;
    }
}
