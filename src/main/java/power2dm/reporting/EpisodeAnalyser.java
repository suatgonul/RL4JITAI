package power2dm.reporting;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import power2dm.model.P2DMQLearning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 27-Apr-16.
 */
public abstract class EpisodeAnalyser {
    protected P2DMQLearning qLearning;
    protected Map<State, List<QValue>> episodeMaxQValues = new HashMap<State, List<QValue>>();

    public void setLearningAlgorithm(P2DMQLearning learning) {
        qLearning = learning;
    }

    /**
     * Extracts the maximum Q-Values for the states discovered through the given episode. Extracted (state-value) pairs
     * are collected in the global cache to be used to identify the selected action and the way action was chosen e.g.
     * randomly or systematically.
     *
     * @param ea
     */
    public void populateMaxQValues(EpisodeAnalysis ea) {
        for (int si = 0; si < ea.stateSequence.size(); si++) {
            State st = ea.stateSequence.get(si);
            List<QValue> qs = qLearning.getQs(st);
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
            episodeMaxQValues.put(qLearning.stateHash(st), maxQValues);
        }
    }

    /**
     * Aggregates the states belonging to a particular time (hour) into a list in a sorted way. Sorting should consider
     * the state-specific parameters.
     *
     * @param time the hour of interest during the day
     * @return
     */
    public abstract List<State> getStatesForTime(int time);

    /**
     * Prints details of the path followed by the agent through the given episode. For each timestep all discovered
     * states (i.e. the state parameters) are printed. Also, the state chosen by the agent at each timestep should be
     * indicated in the print statements.
     *
     * @param ea      Base episode analysis keeping the agent's states visited by the agent along with the associated
     *                action and reward values
     * @param episode The index of episode of interest
     */
    public abstract void printQValuesForPreferredRange(EpisodeAnalysis ea, int episode);

    protected String isRandomActionSelected(State st, GroundedAction selectedAction) {
        List<QValue> maxQValuesForState = episodeMaxQValues.get(qLearning.stateHash(st));
        if (maxQValuesForState != null) {
            if (maxQValuesForState.size() == 1) {
                if (maxQValuesForState.get(0).a.actionName().equals(selectedAction.actionName())) {
                    return "   (Systematic)";
                } else {
                    return "   (Random - Non-single)";
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
