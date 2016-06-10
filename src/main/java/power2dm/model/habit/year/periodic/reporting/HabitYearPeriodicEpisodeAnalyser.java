package power2dm.model.habit.year.periodic.reporting;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.statehashing.HashableState;
import power2dm.algorithm.LearningProvider;
import power2dm.model.habit.year.periodic.environment.HabitYearPeriodicP2DMEnvironmentSimulator;
import power2dm.reporting.EpisodeAnalyser;
import power2dm.reporting.P2DMEpisodeAnalysis;

import java.util.List;
import java.util.Set;

import static power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain.*;

/**
 * Created by suat on 27-Apr-16.
 */
public class HabitYearPeriodicEpisodeAnalyser extends EpisodeAnalyser {

    private HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator;

    public HabitYearPeriodicEpisodeAnalyser(HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }

    @Override
    public List<State> getStatesForTime(int time) {
        return null;
    }

    @Override
    public void printQValuesForPreferredRange(EpisodeAnalysis ea, int episode) {
//        System.out.println("Episode: " + episode + " Starting Hour: " + startingHour + " - " + (environmentSimulator.isHabitActive(episode + 1) ? " Habit" : "Non-Habit"));
        if(episode < 20000)
            return;

        Set<HashableState> states = ((LearningProvider) qLearning).getAllQValues().keySet();
        System.out.println("Episode: " + episode + " State size: " + states.size());

        for (int i = 0; i < ea.numTimeSteps() - 1; i++) {

            boolean foundChosenAction = false;
            for (HashableState s : states) {
                if (ea != null) {
                    if ((ea.stateSequence.get(i)).equals(s.s)) {
//                        if(ea.rewardSequence.get(i) < 0 ) {
                        HabitYearPeriodicEpisodeAnalysis hyea = (HabitYearPeriodicEpisodeAnalysis) environmentSimulator.getEpisodeAnalysis();
                        ObjectInstance stateInstance = s.getObject(CLASS_STATE);
                        System.out.print("Frequency P1: " + stateInstance.getIntValForAttribute(ATT_CALORIE_INTAKE_FREQUENCY_FIRST_PERIOD));
                        System.out.print(" P2: " + stateInstance.getIntValForAttribute(ATT_CALORIE_INTAKE_FREQUENCY_SECOND_PERIOD));
                        System.out.print(" P3: " + stateInstance.getIntValForAttribute(ATT_CALORIE_INTAKE_FREQUENCY_THIRD_PERIOD));
                        System.out.print(" P4: " + stateInstance.getIntValForAttribute(ATT_CALORIE_INTAKE_FREQUENCY_FOURTH_PERIOD));
                        //System.out.print(" P5: " + stateInstance.getIntValForAttribute(ATT_CALORIE_INTAKE_FREQUENCY_FIFTH_PERIOD));
                        System.out.print(" Automation P1: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_AUTOMATION_RATIO_FIRST_PERIOD));
                        System.out.print(" P2: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_AUTOMATION_RATIO_SECOND_PERIOD));
                        System.out.print(" P3: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_AUTOMATION_RATIO_THIRD_PERIOD));
                        System.out.print(" P4: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_AUTOMATION_RATIO_FOURTH_PERIOD));
                        //System.out.print(" P5: " + stateInstance.getIntValForAttribute(ATT_CAL_INTAKE_AUTOMATION_RATIO_FIFTH_PERIOD));
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
        HabitYearPeriodicEpisodeAnalysis hea = (HabitYearPeriodicEpisodeAnalysis) environmentSimulator.getEpisodeAnalysis();
        hea.appendAndMergeEpisodeAnalysis(ea);
        return hea;
    }
}
