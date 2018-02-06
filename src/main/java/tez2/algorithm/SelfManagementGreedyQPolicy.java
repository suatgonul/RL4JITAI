package tez2.algorithm;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.states.State;
import tez2.domain.action.SelfManagementAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 18-May-17.
 */
public class SelfManagementGreedyQPolicy extends GreedyQPolicy {

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
        int selected = rand.nextInt(maxActions.size());
        //return translated action parameters if the action is parameterized with objects in a object identifier independent domain
        SelfManagementSimpleGroundedAction a = (SelfManagementSimpleGroundedAction) maxActions.get(selected).a;
        a.setSelectedBy(selectedBy);
        AbstractGroundedAction srcA = maxActions.get(selected).a;
        return AbstractObjectParameterizedGroundedAction.Helper.translateParameters(srcA, maxActions.get(selected).s, s);
    }
}
