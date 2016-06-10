package power2dm.model.habit.year.weighted.reporting;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.statehashing.HashableState;
import power2dm.algorithm.LearningProvider;
import power2dm.model.habit.year.weighted.HabitYearP2DMEnvironmentSimulator;
import power2dm.reporting.EpisodeAnalyser;
import power2dm.reporting.P2DMEpisodeAnalysis;

import java.util.List;
import java.util.Set;

import static power2dm.model.habit.year.weighted.HabitYearP2DMDomain.*;

/**
 * Created by suat on 27-Apr-16.
 */
public class HabitYearEpisodeAnalyser extends EpisodeAnalyser {

    private HabitYearP2DMEnvironmentSimulator environmentSimulator;

    public HabitYearEpisodeAnalyser(HabitYearP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }

    @Override
    public List<State> getStatesForTime(int time) {
        return null;
    }

    @Override
    public void printQValuesForPreferredRange(EpisodeAnalysis ea, int episode) {
//        System.out.println("Episode: " + episode + " Starting Hour: " + startingHour + " - " + (environmentSimulator.isHabitActive(episode + 1) ? " Habit" : "Non-Habit"));
        System.out.println("Episode: " + episode + " - " + (environmentSimulator.isHabitActive() ? " Habit" : "Non-Habit") + " // Gain Offset: " + environmentSimulator.getHabitGainOffset());
        for (int i = 0; i < ea.numTimeSteps() - 1; i++) {
            Set<HashableState> states = ((LearningProvider) qLearning).getAllQValues().keySet();

            boolean foundChosenAction = false;
            for (HashableState s : states) {
                if (ea != null) {
                    if ((ea.stateSequence.get(i)).equals(s.s)) {
//                        if(ea.rewardSequence.get(i) < 0 ) {
                            HabitYearEpisodeAnalysis hyea = (HabitYearEpisodeAnalysis) environmentSimulator.getEpisodeAnalysis();
                            ObjectInstance stateInstance = s.getObject(CLASS_STATE);
                            System.out.print("Frequency: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_FREQUENCY));
                            System.out.print(" Automation: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_AUTOMATION_RATIO));
                            System.out.print(" Period: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_PERIOD));
                            System.out.print(" Calorie Intake:" + hyea.getCalorieIntakeList().get(i));
                            System.out.print(" Rnd: " + hyea.getGeneratedRandomList().get(i));
                            System.out.print(" Habit coeff: " + hyea.getHabitGainList().get(i));
                            System.out.print(" qVals:");
                            for (QValue qVal : qLearning.getQs(s)) {
                                System.out.printf("\tAct: " + qVal.a.actionName().substring(0, 3) + " %f", qVal.q);
                            }
                            GroundedAction act = ea.actionSequence.get(i);
                            System.out.print("\t(x): Act: " + act.actionName().substring(0, 3) + " Rew: " + ea.rewardSequence.get(i));
                            String selectionMechanism = isRandomActionSelected(ea.stateSequence.get(i), ea.actionSequence.get(i));
                            System.out.print(selectionMechanism);
                            System.out.println();
//                        }
                        foundChosenAction = true;
                        break;
                    }
                }
//                System.out.println();
            }
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
        HabitYearEpisodeAnalysis hea = (HabitYearEpisodeAnalysis) environmentSimulator.getEpisodeAnalysis();
        hea.appendAndMergeEpisodeAnalysis(ea);
        return hea;
    }
}
