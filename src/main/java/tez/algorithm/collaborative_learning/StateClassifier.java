package tez.algorithm.collaborative_learning;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import tez.domain.SelfManagementRewardFunction;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.*;

import static tez.domain.SelfManagementDomainGenerator.ACTION_NO_ACTION;

/**
 * Created by suat on 22-May-17.
 */
public abstract class StateClassifier {
    protected Map<HashableState, Map<String, Integer>> stateActionCounts = new HashMap<>();

    private SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

    public abstract void updateLearningModel(List<SelfManagementEpisodeAnalysis> ea);

    public abstract Action guessAction(State state);

    protected void updateStateActionCounts(List<SelfManagementEpisodeAnalysis> eaList) {
        for (int t = 0; t < eaList.size(); t++) {
            SelfManagementEpisodeAnalysis ea = eaList.get(t);
            for (int i = 0; i < ea.actionSequence.size(); i++) {
                // do not keep the state as data item if no intervention is delivered
                // i.e. keep only the states and actions where an intervention is delivered (as an indicator of preference)
                double r = ea.rewardSequence.get(i);
                String actionName = ea.actionSequence.get(i).actionName();

                if (r == SelfManagementRewardFunction.getRewardNoIntervention()) {
                    continue;
                } else if (r == SelfManagementRewardFunction.getRewardNonReactionToIntervention()) {
                    actionName = ACTION_NO_ACTION;
                }

                HashableState s = hashingFactory.hashState(ea.stateSequence.get(i));
                Map<String, Integer> actionCounts = stateActionCounts.get(s);
                if (actionCounts == null) {
                    actionCounts = new HashMap<>();
                    stateActionCounts.put(s, actionCounts);
                }

                Integer count = actionCounts.get(actionName);
                if (count == null) {
                    count = 0;
                }
                count++;
                actionCounts.put(actionName, count);
            }
        }
        System.out.println("Number of distinct states: " + stateActionCounts.keySet().size());
    }

    protected List<DataItem> generateDataSetFromDataItems() {
        List<DataItem> dataItems = new ArrayList<>();
        for (Map.Entry<HashableState, Map<String, Integer>> e : stateActionCounts.entrySet()) {
            State s = e.getKey();
            Map<String, Integer> actionCounts = e.getValue();

            List<String> mostPreferredActions = new ArrayList<>();
            int max = Integer.MIN_VALUE;
            for (Map.Entry<String, Integer> e2 : actionCounts.entrySet()) {
                if (e2.getValue() > max) {
                    mostPreferredActions.clear();
                    mostPreferredActions.add(e2.getKey());
                    max = e2.getValue();
                } else if (e2.getValue() == max) {
                    mostPreferredActions.add(e2.getKey());
                }
            }

            Random r = new Random();
            String selectedAction = mostPreferredActions.get(r.nextInt(mostPreferredActions.size()));
            DataItem dataItem = new DataItem(s, selectedAction);
            dataItems.add(dataItem);
        }
        return dataItems;
    }
}
