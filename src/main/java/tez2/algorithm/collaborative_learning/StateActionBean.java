package tez2.algorithm.collaborative_learning;

import burlap.oomdp.core.states.State;
import tez.algorithm.collaborative_learning.DataItem;
import tez.algorithm.collaborative_learning.StateBean;

/**
 * Created by suat on 23-May-17.
 */
public class StateActionBean extends StateBean {
    private String action;

    public StateActionBean(DataItem dataItem) {
        super(dataItem.getState());
        this.action = dataItem.getActionName();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
