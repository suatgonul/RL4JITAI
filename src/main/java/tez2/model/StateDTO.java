package tez2.model;

import org.joda.time.DateTime;

/**
 * Created by suatgonul on 12/20/2016.
 */
public class StateDTO {
    private DateTime time;
    private boolean isTerminal;

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }
}
