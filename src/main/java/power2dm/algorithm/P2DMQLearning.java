package power2dm.algorithm;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static power2dm.P2DMDomain.*;


/**
 * Created by suat on 15-Apr-16.
 */
public class P2DMQLearning extends QLearning {

    private Map<State, List<QValue>> episodeMaxQValues = new HashMap<State, List<QValue>>();

    public P2DMQLearning(Domain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate) {
        super(domain, gamma, hashingFactory, qInit, learningRate);
    }

    public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps, int episodeNo) {
        EpisodeAnalysis ea = super.runLearningEpisode(env, maxSteps);

        if (episodeNo >= 49500) printQValuesForPreferredRange(ea, episodeNo);

        populateMaxQValues(ea);
        return ea;
    }

    private void populateMaxQValues(EpisodeAnalysis ea) {
        for (int si = 0; si < ea.stateSequence.size(); si++) {
            State st = ea.stateSequence.get(si);
            List<QValue> qs = this.getQs(st);
            List<QValue> maxQValues = new ArrayList<QValue>();
            maxQValues.add(qs.get(0));
            double maxQ = qs.get(0).q;

            for (int i = 1; i < qs.size(); i++) {
                QValue q = qs.get(i);
                if (q.q == maxQ) {
                    maxQValues.add(q);
                } else if (q.q > maxQ) {
                    maxQValues.clear();
                    maxQValues.add(q);
                }
            }
            episodeMaxQValues.put(stateHash(st), maxQValues);
        }
    }

    private void printQValuesForPreferredRange(EpisodeAnalysis ea, int episodeNo) {
        System.out.println("Episode: " + episodeNo);
        for (int i = 0; i <= 23; i++) {
            List<State> states = getStatesForTime(i);
            for (State s : states) {
                System.out.print("Time: " + i);
                ObjectInstance stateInstance = s.getObject(CLASS_STATE);
                System.out.print(" React: " + stateInstance.getIntValForAttribute(ATT_REACTED_INT) + ", Non-React: " + stateInstance.getIntValForAttribute(ATT_NON_REACTED_INT) + " Loc: " + stateInstance.getIntValForAttribute(ATT_LOCATION) + ", ");

                System.out.print("qVals:");
                for (QValue qVal : getQs(s)) {
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

    private String isRandomActionSelected(State st, GroundedAction selectedAction) {
        List<QValue> maxQValuesForState = episodeMaxQValues.get(stateHash(st));
        if (maxQValuesForState != null) {
            if (maxQValuesForState.size() == 1) {
                if (maxQValuesForState.get(0).a.actionName().equals(selectedAction.actionName())) {
                    return "   (Systematic)";
                } else {
                    return "   (Random)";
                }
            } else {
                for (QValue qVal : maxQValuesForState) {
                    if (qVal.a.actionName().equals(selectedAction.actionName())) {
                        return "   (Max-Random)";
                    }
                }
            }
        } else {
            return "   (Blind-Random)";
        }
        return "   (????????)";
    }

    private List<State> getStatesForTime(int time) {
        List<State> statesForTime = new ArrayList<State>();

        for (Map.Entry<HashableState, QLearningStateNode> stateEntry : qIndex.entrySet()) {
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
}
