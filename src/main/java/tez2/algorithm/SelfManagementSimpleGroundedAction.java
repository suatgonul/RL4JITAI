package tez2.algorithm;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.common.SimpleGroundedAction;
import tez2.domain.action.SelfManagementAction;

/**
 * Created by suat on 18-May-17.
 */
public class SelfManagementSimpleGroundedAction extends SimpleGroundedAction {
    private SelfManagementAction.SelectedBy selectedBy;

    public SelfManagementSimpleGroundedAction(Action action) {
        super(action);
    }

    public SelfManagementAction.SelectedBy getSelectedBy() {
        return selectedBy;
    }

    public void setSelectedBy(SelfManagementAction.SelectedBy selectedBy) {
        this.selectedBy = selectedBy;
    }
}
