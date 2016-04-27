package power2dm.model.burden.algorithm;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by suat on 15-Apr-16.
 */
public class BurdenP2DMQLearning extends QLearning {

    private Map<State, List<QValue>> episodeMaxQValues = new HashMap<State, List<QValue>>();

    public BurdenP2DMQLearning(Domain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate) {
        super(domain, gamma, hashingFactory, qInit, learningRate);
    }

    public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps, int episodeNo) {
        EpisodeAnalysis ea = super.runLearningEpisode(env, maxSteps);

//        printQValuesForPreferredRange(ea, episodeNo);
//        populateMaxQValues(ea);
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
                return "   (Random)";
            }
        } else {
            return "   (Blind-Random)";
        }
    }
}
