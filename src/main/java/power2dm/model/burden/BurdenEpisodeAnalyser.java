package power2dm.model.burden;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.statehashing.HashableState;
import power2dm.model.burden.state.P2DMState;
import power2dm.reporting.EpisodeAnalyser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static power2dm.model.burden.BurdenP2DMDomain.*;

/**
 * Created by suat on 27-Apr-16.
 */
public class BurdenEpisodeAnalyser extends EpisodeAnalyser {

    @Override
    public List<State> getStatesForTime(int time) {
        List<State> statesForTime = new ArrayList<State>();

        for (Map.Entry<HashableState, QLearningStateNode> stateEntry : qLearning.getAllQValues().entrySet()) {
            HashableState newState = stateEntry.getKey();
            P2DMState newP2DMSt = (P2DMState) newState.s;
            ObjectInstance newStateInstance = newState.getObject(CLASS_STATE);

            int stateTime = newStateInstance.getIntValForAttribute(ATT_TIME);
            if (stateTime == time) {
                // sort the list based on the number of reacted interventions
                int i = 0;
                for (; i < statesForTime.size(); i++) {
                    P2DMState curP2DMState = (P2DMState) ((HashableState) statesForTime.get(i)).s;
//                    P2DMState curP2DMState = (P2DMState) statesForTime.get(i);
                    ObjectInstance currentStateInstance = statesForTime.get(i).getObject(CLASS_STATE);

                    double newStateCoeff = newStateInstance.getRealValForAttribute(ATT_BURDEN_COEFF);
                    double currentStateCoeff = currentStateInstance.getRealValForAttribute(ATT_BURDEN_COEFF);

                    if (newStateCoeff < currentStateCoeff) {
                        break;
                    } else if (newStateCoeff == currentStateCoeff) {
                        int newStateLocation = newStateInstance.getIntValForAttribute(ATT_LOCATION);
                        int currentStateLocation = currentStateInstance.getIntValForAttribute(ATT_LOCATION);

                        if (newStateLocation <= currentStateLocation) {
                            break;
                        }
                    }
                }
                statesForTime.add(i, newState);
            }
        }

        return statesForTime;
    }

    @Override
    public void printQValuesForPreferredRange(EpisodeAnalysis ea, int episode) {
        System.out.println("Episode: " + episode);
        for (int i = 0; i <= 23; i++) {
            List<State> states = getStatesForTime(i);
            for (State s : states) {
                P2DMState st = (P2DMState) ((HashableState) s).s;
                System.out.print("Time: " + i);
                ObjectInstance stateInstance = s.getObject(CLASS_STATE);
                System.out.printf(" Burden: %10f Loc:" + stateInstance.getIntValForAttribute(ATT_LOCATION), stateInstance.getRealValForAttribute(ATT_BURDEN_COEFF));
                System.out.print(" React: " + st.getReactedInt() + ", Non-React: " + st.getNonReactedInt() + ", ");

                System.out.print("qVals:");
                for (QValue qVal : qLearning.getQs(s)) {
                    System.out.printf("\tAct: " + qVal.a.actionName().substring(0, 3) + " %f", qVal.q);
                }
                if (ea != null) {
                    if (((P2DMState) ea.stateSequence.get(i)).equals(((HashableState) s).s)) {
//                    if (((P2DMState) ea.stateSequence.get(i)).equals(s)) {
                        GroundedAction act = ea.actionSequence.get(i);
                        System.out.print("\t(x): Act: " + act.actionName().substring(0, 3) + " Rew: " + ea.rewardSequence.get(i));
                        String selectionMechanism = isRandomActionSelected(ea.stateSequence.get(i), ea.actionSequence.get(i));
                        System.out.print(selectionMechanism);
                    }
                }
                System.out.println();
            }
        }
        if (ea != null) {
            double totalRewardInEpisode = 0;
            for (double reward : ea.rewardSequence) {
                totalRewardInEpisode += reward;
            }
            System.out.println("Total reward: " + totalRewardInEpisode);
        }
    }
}
