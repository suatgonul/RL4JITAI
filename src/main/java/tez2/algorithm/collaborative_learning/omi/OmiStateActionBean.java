package tez2.algorithm.collaborative_learning.omi;

import tez2.algorithm.collaborative_learning.DataItem;

/**
 * Created by suat on 23-May-17.
 */
public class OmiStateActionBean extends OmiStateBean {
    private String action;

    public OmiStateActionBean(DataItem dataItem) {
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
