package tez2.algorithm.collaborative_learning.js;

import tez2.algorithm.collaborative_learning.DataItem;
import tez2.algorithm.collaborative_learning.omi.OmiStateBean;

/**
 * Created by suat on 23-May-17.
 */
public class JsStateActionBean extends JsStateBean {
    private String action;

    public JsStateActionBean(DataItem dataItem) {
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
