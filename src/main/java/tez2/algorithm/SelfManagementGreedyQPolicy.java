package tez2.algorithm;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.states.State;
import tez2.algorithm.collaborative_learning.StateClassifier;
import tez2.algorithm.collaborative_learning.js.SparkJsStateClassifier;
import tez2.domain.DomainConfig;
import tez2.domain.SelfManagementAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by suat on 18-May-17.
 */
public class SelfManagementGreedyQPolicy extends GreedyQPolicy {

    @Override
    public AbstractGroundedAction getAction(State s) {
        List<QValue> qValues = filterQValues(this.qplanner.getQs(s), s);
        List<QValue> maxActions = new ArrayList<QValue>();
        maxActions.add(qValues.get(0));
        double maxQ = qValues.get(0).q;
        SelfManagementAction.SelectedBy selectedBy = SelfManagementAction.SelectedBy.QLEARNING;
        for (int i = 1; i < qValues.size(); i++) {
            QValue q = qValues.get(i);
            if (q.q == maxQ) {
                maxActions.add(q);
                selectedBy = SelfManagementAction.SelectedBy.RANDOM;
            } else if (q.q > maxQ) {
                maxActions.clear();
                maxActions.add(q);
                maxQ = q.q;
                selectedBy = SelfManagementAction.SelectedBy.QLEARNING;
            }
        }

//        if(selectedBy == SelfManagementAction.SelectedBy.RANDOM) {
//            if (maxQ != 0) {
//                selectedBy = SelfManagementAction.SelectedBy.QLEARNING;
//            }
//        }

        int selected = rand.nextInt(maxActions.size());

        if (StateClassifier.classifierModeIncludes("use") || StateClassifier.classifierModeIncludes("use-js")) {
            if (qValues.size() == 3 && maxActions.size() >= 2) {
                boolean jitai1Checked = false;
                boolean jitai2Checked = false;

                String actionName = maxActions.get(selected).a.actionName();
                while (!actionName.contentEquals(DomainConfig.ACTION_NO_ACTION)) {
                    actionName = maxActions.get(selected).a.actionName();

                    double ratio = SparkJsStateClassifier.getJitaiRatio(actionName);
                    if (new Random().nextDouble() < ratio) {
                        break;
                    }

                    if (actionName.equals(DomainConfig.ACTION_JITAI_1)) {
                        jitai1Checked = true;
                    } else if (actionName.equals(DomainConfig.ACTION_JITAI_2)) {
                        jitai2Checked = true;
                    }

                    if (jitai1Checked && jitai2Checked) {
                        break;
                    }

                    selected = new Random().nextInt(maxActions.size());
                    actionName = maxActions.get(selected).a.actionName();
               }

                selectedBy = SelfManagementAction.SelectedBy.GUESSED_RANDOM;
            }
        }

        //return translated action parameters if the action is parameterized with objects in a object identifier independent domain
        SelfManagementSimpleGroundedAction a = (SelfManagementSimpleGroundedAction) maxActions.get(selected).a;
        a.setSelectedBy(selectedBy);
        AbstractGroundedAction srcA = maxActions.get(selected).a;
        return AbstractObjectParameterizedGroundedAction.Helper.translateParameters(srcA, maxActions.get(selected).s, s);
    }

    private List<QValue> filterQValues(List<QValue> allQValues, State s) {
        if (s instanceof ActionRestrictingState) {
            List<QValue> filteredQValues = new ArrayList<>();
            for (QValue qVal : allQValues) {
                if (((SelfManagementSimpleGroundedAction) qVal.a).action.applicableInState(s, null)) {
                    filteredQValues.add(qVal);
                }
            }
            return filteredQValues;
        } else {
            return allQValues;
        }
    }
}
