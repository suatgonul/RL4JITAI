package power2dm.model.habit.hour.reporting;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.statehashing.HashableState;
import power2dm.algorithm.LearningProvider;
import power2dm.model.habit.hour.HabitP2DMEnvironmentSimulator;
import power2dm.reporting.EpisodeAnalyser;
import power2dm.reporting.P2DMEpisodeAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static power2dm.model.habit.hour.HabitP2DMDomain.*;

/**
 * Created by suat on 27-Apr-16.
 */
public class HabitEpisodeAnalyser extends EpisodeAnalyser {

    private HabitP2DMEnvironmentSimulator environmentSimulator;

    public HabitEpisodeAnalyser(HabitP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }

    @Override
    public List<State> getStatesForTime(int time) {
        List<State> statesForTime = new ArrayList<State>();

        for (Map.Entry<HashableState, QLearningStateNode> stateEntry : ((LearningProvider) qLearning).getAllQValues().entrySet()) {
            HashableState newState = stateEntry.getKey();
            ObjectInstance newStateInstance = newState.getObject(CLASS_STATE);

            int stateTime = newStateInstance.getIntValForAttribute(ATT_HOUR_OF_DAY);
            if (stateTime == time) {
                // sort the list based on the number of reacted interventions
                int i = 0;
                for (; i < statesForTime.size(); i++) {
                    boolean newStateEntry = newStateInstance.getBooleanValForAttribute(ATT_CAL_INTAKE_ENTRY);

                    if (newStateEntry == false) {
                        break;
                    }
                }
                statesForTime.add(i, newState);
            }
        }

        return statesForTime;
    }

    @Override
    public void printQValuesForPreferredRange(EpisodeAnalysis ea, int episode) {
        int startingHour = ea.getState(0).getObject(CLASS_STATE).getIntValForAttribute(ATT_HOUR_OF_DAY);
//        System.out.println("Episode: " + episode + " Starting Hour: " + startingHour + " - " + (environmentSimulator.isHabitActive(episode + 1) ? " Habit" : "Non-Habit"));
        System.out.println("Episode: " + episode + " Starting Hour: " + startingHour + " - " + (environmentSimulator.isHabitActive() ? " Habit" : "Non-Habit") + " // Gain Offset: " + environmentSimulator.getHabitGainOffset());
        for (int i = 0; i < ea.numTimeSteps() - 1; i++) {
            List<State> states = getStatesForTime(i + startingHour);
            boolean foundChosenAction = false;
            for (State s : states) {
                if (ea != null) {
                    if ((ea.stateSequence.get(i)).equals(((HashableState) s).s)) {
                        System.out.print("Time: " + (i + startingHour));
                        ObjectInstance stateInstance = s.getObject(CLASS_STATE);
                        System.out.print(" Calorie Intake:" + stateInstance.getBooleanValForAttribute(ATT_CAL_INTAKE_ENTRY));
//                System.out.print(" React: " + st.getReactedInt() + ", Non-React: " + st.getNonReactedInt() + ", ");
                        System.out.print(" Habit coeff: " + stateInstance.getIntValForAttribute(ATT_HABIT_COEFF));

                        System.out.print(" qVals:");
                        for (QValue qVal : qLearning.getQs(s)) {
                            System.out.printf("\tAct: " + qVal.a.actionName().substring(0, 3) + " %f", qVal.q);
                        }
//                if (ea != null) {
//                    if ((ea.stateSequence.get(i)).equals(((HashableState) s).s)) {
//                    if (((P2DMState) ea.stateSequence.get(i)).equals(s)) {
                        GroundedAction act = ea.actionSequence.get(i);
                        System.out.print("\t(x): Act: " + act.actionName().substring(0, 3) + " Rew: " + ea.rewardSequence.get(i));
                        String selectionMechanism = isRandomActionSelected(ea.stateSequence.get(i), ea.actionSequence.get(i));
                        System.out.print(selectionMechanism);
                        foundChosenAction = true;
                    }
                }
//                System.out.println();
            }
            System.out.println();
            if (!foundChosenAction) {
                System.out.println("Could not found chosen action");
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

    @Override
    public P2DMEpisodeAnalysis appendEpisodeSummaryData(EpisodeAnalysis ea, int episodeNo) {
        P2DMEpisodeAnalysis p2dmEa = super.appendEpisodeSummaryData(ea, episodeNo);
        boolean isHabitActive = environmentSimulator.isHabitActive();
        boolean isInterventionDelivered = isInterventionDelivered(ea);
        boolean isCalorieIntakeEntered = environmentSimulator.getCalorieIntakeEntry();
        double entryProbabilityWhenInterventionDelivery = environmentSimulator.getEntryProbabilityWhenInterventionDelivery();
        int habitGainOffset = environmentSimulator.getHabitGainOffset();

        HabitEpisodeAnalysis hea = new HabitEpisodeAnalysis(p2dmEa, isHabitActive, isInterventionDelivered, isCalorieIntakeEntered, entryProbabilityWhenInterventionDelivery, habitGainOffset);
        return hea;
    }

    private boolean isInterventionDelivered(EpisodeAnalysis ea) {
        int i = 0;
        if (ea.actionSequence.size() >= 5) {
            i = ea.actionSequence.size() - 5;
        }
        for (; i < ea.actionSequence.size(); i++) {
            if (ea.actionSequence.get(i).actionName().equals(ACTION_INT_DELIVERY)) {
                return true;
            }
        }
        return false;
    }
}
