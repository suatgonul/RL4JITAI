package tez.algorithm;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.states.State;
import org.apache.log4j.Logger;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.action.SelfManagementAction;
import tez.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 18-May-17.
 */
public class SelfManagementNoActionFavoringQPolicy extends GreedyQPolicy {

    private static final Logger log = Logger.getLogger(SelfManagementNoActionFavoringQPolicy.class);

    private String deviceIdentifier;

    public SelfManagementNoActionFavoringQPolicy(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public AbstractGroundedAction getAction(State s) {
        List<QValue> qValues = this.qplanner.getQs(s);
        List <QValue> maxActions = new ArrayList<QValue>();
        maxActions.add(qValues.get(0));
        double maxQ = qValues.get(0).q;
        SelfManagementAction.SelectedBy selectedBy = SelfManagementAction.SelectedBy.QLEARNING;
        for(int i = 1; i < qValues.size(); i++){
            QValue q = qValues.get(i);
            if(q.q == maxQ){
                maxActions.add(q);
                selectedBy = SelfManagementAction.SelectedBy.RANDOM;
            }
            else if(q.q > maxQ){
                maxActions.clear();
                maxActions.add(q);
                maxQ = q.q;
                selectedBy = SelfManagementAction.SelectedBy.QLEARNING;
            }
        }

        int selected;
        LogUtil.log_info(log, deviceIdentifier,"max actions size: " + maxActions.size());
        if(maxActions.size() > 1) {
            // 4% of chance to deliver intervention
            boolean interventionAction = rand.nextInt(100) > 95 ? true : false;
            LogUtil.log_info(log, deviceIdentifier,"intervention action: " + interventionAction);
            int interventionActionIndex = 0;
            for(int i=0; i<qValues.size(); i++) {
                if(qValues.get(i).a.actionName().contentEquals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
                    interventionActionIndex = i;
                }
            }
            if(interventionAction) {
                selected = interventionActionIndex;
            } else {
                interventionActionIndex++;
                selected = interventionActionIndex % 2;
            }

        } else {
            selected = rand.nextInt(maxActions.size());
            LogUtil.log_info(log, deviceIdentifier,"specifically selected: " + selected);
            //return translated action parameters if the action is parameterized with objects in a object identifier independent domain
        }

        SelfManagementSimpleGroundedAction a = (SelfManagementSimpleGroundedAction) maxActions.get(selected).a;
        a.setSelectedBy(selectedBy);
        AbstractGroundedAction srcA = maxActions.get(selected).a;
        LogUtil.log_info(log, deviceIdentifier,"selected action: " + a.actionName());
        return AbstractObjectParameterizedGroundedAction.Helper.translateParameters(srcA, maxActions.get(selected).s, s);
    }
}
