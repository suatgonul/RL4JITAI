package power2dm.model.reacted_non_reacted_numbers;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.statehashing.HashableState;
import power2dm.reporting.EpisodeAnalyser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain.*;

/**
 * Created by suat on 27-Apr-16.
 */
public class ReactNonReactEpisodeAnalyser extends EpisodeAnalyser {

    @Override
    public List<State> getStatesForTime(int time) {
        List<State> statesForTime = new ArrayList<State>();

        for (Map.Entry<HashableState, QLearningStateNode> stateEntry : qLearning.getAllQValues().entrySet()) {
            State state = stateEntry.getKey();
            ObjectInstance stateInstance = state.getObject(CLASS_STATE);

            int stateTime = stateInstance.getIntValForAttribute(ATT_TIME);
            if (stateTime == time) {
                // sort the list based on the number of reacted interventions
                int i = 0;
                for (; i < statesForTime.size(); i++) {
                    ObjectInstance currentStateInstance = statesForTime.get(i).getObject(CLASS_STATE);

                    int newStateReacted = stateInstance.getIntValForAttribute(ATT_REACTED_INT);
                    int currentStateReacted = currentStateInstance.getIntValForAttribute(ATT_REACTED_INT);

                    if (newStateReacted < currentStateReacted) {
                        break;
                    } else if (newStateReacted == currentStateReacted) {
                        int newStateNonReacted = stateInstance.getIntValForAttribute(ATT_NON_REACTED_INT);
                        int currentStateNonReacted = currentStateInstance.getIntValForAttribute(ATT_NON_REACTED_INT);

                        if (newStateNonReacted < currentStateNonReacted) {
                            break;
                        }

                        if (newStateNonReacted == currentStateNonReacted) {
                            int newStateLocation = stateInstance.getIntValForAttribute(ATT_LOCATION);
                            int currentStateLocation = currentStateInstance.getIntValForAttribute(ATT_LOCATION);

                            if (newStateLocation <= currentStateLocation) {
                                break;
                            }
                        }
                    }
                }
                statesForTime.add(i, state);
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
                System.out.print("Time: " + i);
                ObjectInstance stateInstance = s.getObject(CLASS_STATE);
                System.out.print(" React: " + stateInstance.getIntValForAttribute(ATT_REACTED_INT) + ", Non-React: " + stateInstance.getIntValForAttribute(ATT_NON_REACTED_INT) + " Loc: " + stateInstance.getIntValForAttribute(ATT_LOCATION) + ", ");

                System.out.print("qVals:");
                for (QValue qVal : qLearning.getQs(s)) {
                    System.out.printf("\tAct: " + qVal.a.actionName().substring(0, 3) + " %.2f", qVal.q);
                }
                if (ea != null) {
                    if (ea.stateSequence.get(i).equals(((HashableState) s).s)) {
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
