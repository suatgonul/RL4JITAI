package power2dm;

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

    private Map<Integer, List<QValue>> episodeMaxQValues = new HashMap<Integer, List<QValue>>();

    public P2DMQLearning(Domain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate) {
        super(domain, gamma, hashingFactory, qInit, learningRate);
    }

    @Override
    public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {
//        populateMaxQValues();

        System.out.println("\nQ-Values Before Running Episode:");
        printQValuesForPreferredRange();

        EpisodeAnalysis ea = super.runLearningEpisode(env, maxSteps);

        System.out.println("\nQ-Values After Running Episode:");
        printQValuesForPreferredRange(ea);
        return ea;
    }

    private List<QValue> populateMaxQValues(HashableState s) {
        List<QValue> qs = this.getQs(s);
        List<QValue> maxQValues = new ArrayList<QValue>();

        maxQValues.add(qs.get(0));
        double maxQ = qs.get(0).q;
        for(int i = 1; i < qs.size(); i++){
            QValue q = qs.get(i);
            if(q.q == maxQ){
                maxQValues.add(q);
            }
            else if(q.q > maxQ){
                maxQValues.clear();
                maxQValues.add(q);
            }
        }
        return maxQValues;
    }

    private void printQValuesForPreferredRange() {
        printQValuesForPreferredRange(null);
    }

    private void printQValuesForPreferredRange(EpisodeAnalysis ea) {
        for (int i = 0; i <= 23; i++) {
            List<State> states = getStatesForTime(i);
            for (State s : states) {
                System.out.print("Time: " + i);
                ObjectInstance stateInstance = s.getObject(CLASS_STATE);
                System.out.print(" React: " + stateInstance.getIntValForAttribute(ATT_REACTED_INT) + ", Loc: " + stateInstance.getIntValForAttribute(ATT_LOCATION) + ", ");

                for (QValue qVal : getQs(s)) {
                    System.out.print("\tAct: " + qVal.a.actionName().substring(0, 3) + " " + qVal.q);
                }
                if (ea != null) {
                    if (ea.stateSequence.get(i).equals(((HashableState) s).s)) {
                        GroundedAction act = ea.actionSequence.get(i);
                        System.out.print("\t(x): Act: " + act.actionName().substring(0, 3) + " Rew: " + ea.rewardSequence.get(i));
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
                        int newStateLocation = stateInstance.getIntValForAttribute(ATT_LOCATION);
                        int currentStateLocation = currentStateInstance.getIntValForAttribute(ATT_LOCATION);
                        if (newStateLocation <= currentStateLocation) {
                            break;
                        }
                    }
                }
                statesForTime.add(i, state);
            }
        }

        return statesForTime;
    }
}
