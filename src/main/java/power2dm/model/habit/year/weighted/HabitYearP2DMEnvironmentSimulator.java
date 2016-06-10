package power2dm.model.habit.year.weighted;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import power2dm.algorithm.HabitGainRatio;
import power2dm.model.Location;
import power2dm.model.TaskDifficulty;
import power2dm.model.UserPreference;
import power2dm.model.habit.year.weighted.reporting.HabitYearEpisodeAnalysis;
import power2dm.reporting.P2DMEpisodeAnalysis;

import java.util.LinkedList;
import java.util.Random;

import static power2dm.model.habit.hour.HabitP2DMDomain.ACTION_INT_DELIVERY;

/**
 * Created by suat on 09-May-16.
 */
public class HabitYearP2DMEnvironmentSimulator extends SimulatedEnvironment {
    protected UserPreference preferences = new UserPreference();
    private TaskDifficulty difficulty = TaskDifficulty.MEDIUM;

    private int dayOfExperiment = 1;
    private P2DMEpisodeAnalysis p2dmea = new HabitYearEpisodeAnalysis(0);

    protected Location location = Location.HOME;
    private boolean calorieIntakeEntry = true;
    private boolean dailyInterventionDelivery = false;

    //duration related
    private int totalCalorieIntakeAmount = 0;
    private int periodSize = 30;

    //automation related
    // 0: no entry without intervention
    // 1: entry without intervention
    // -1: intervention
    private LinkedList<Integer> entryWithoutIntervention = new LinkedList<Integer>();

    private boolean isHabitActive = false;
    private int habitGainOffset = 0;
    private int habitWindowSize = 30;
    private int habitLossAmount = 2;
    private LinkedList<Boolean> calorieIntakeEntryQueue = new LinkedList<Boolean>();


    public HabitYearP2DMEnvironmentSimulator(Domain domain, RewardFunction rf, TerminalFunction tf, State initialState) {
        super(domain, rf, tf, initialState);
    }

    public void applyAction(State s, GroundedAction groundedAction) {
        if (groundedAction.actionName().equals(ACTION_INT_DELIVERY)) {
            dailyInterventionDelivery = true;
        }

        simulateCalorieIntakeEntry(groundedAction);
        updateEnvironmentDaily();
    }

    public void updateEnvironmentDaily() {
        dayOfExperiment++;
    }

    private static Random r = new Random();

    private void simulateCalorieIntakeEntry(GroundedAction action) {
        int rInt = r.nextInt((100 - 0) + 1) + 0;
        ((HabitYearEpisodeAnalysis) p2dmea).addGeneratedRandom(rInt);

        if (rInt < HabitGainRatio.get(difficulty, habitGainOffset)) {
            isHabitActive = true;
        } else {
            isHabitActive = false;
        }

        calorieIntakeEntry = false;
        if (isHabitActive || action.actionName().equals(ACTION_INT_DELIVERY)) {
            calorieIntakeEntry = true;
            totalCalorieIntakeAmount++;
            setHabitGainOffset(habitGainOffset + 1);
        } else {
//            setHabitGainOffset(habitGainOffset - habitLossAmount);
            setHabitGainOffset(habitGainOffset - habitLossAmount);
        }

        updateEntryWithoutIntervention(calorieIntakeEntry, action);
        updateCalorieIntakeEntryQueue();
        updateEpisodeAnalysis();
    }

    public void resetEnvironment() {
        super.resetEnvironment();

        p2dmea = new HabitYearEpisodeAnalysis(0);
        dayOfExperiment = 0;
        totalCalorieIntakeAmount = 0;
        habitGainOffset = 0;
        entryWithoutIntervention = new LinkedList<Integer>();
    }

//    private void setHabitForNextEpisode() {
//        if (calorieIntakeEntry == true) {
//            setHabitGainOffset(habitGainOffset + 1);
//            sequentialEntryDays++;
//        } else {
//            setHabitGainOffset((int) (habitGainOffset / habitLossRatio));
//            sequentialEntryDays = 0;
//        }
//    }

    public int getAutomationRatio() {
        double automation = 0;
        int lastIndex = Math.min(dayOfExperiment - 2, habitWindowSize - 1);
        for (int i = lastIndex; i >= 0; i--) {
            if (i == lastIndex) {
                automation += (entryWithoutIntervention.get(i) == 1 ? 1 : 0) * 1;
            } else if (i <= (lastIndex - 1) && i >= (lastIndex - 2)) {
                automation += (entryWithoutIntervention.get(i) == 1 ? 1 : 0) * 0.5;
            } else if (i <= (lastIndex - 3) && i >= (lastIndex - 5)) {
                automation += (entryWithoutIntervention.get(i) == 1 ? 1 : 0) * 0.4;
            } else if (i <= (lastIndex - 6) && i >= (lastIndex - 9)) {
                automation += (entryWithoutIntervention.get(i) == 1 ? 1 : 0) * 0.3;
            } else if (i <= (lastIndex - 10) && i >= (lastIndex - 14)) {
                automation += (entryWithoutIntervention.get(i) == 1 ? 1 : 0) * 0.2;
            } else if (i <= (lastIndex - 15) && i >= (lastIndex - 20)) {
                automation += (entryWithoutIntervention.get(i) == 1 ? 1 : 0) * 0.1;
            }
        }
        return (int) Math.round(automation);
    }

    public int getPeriod() {
        return totalCalorieIntakeAmount / periodSize;
    }

    public int calculateCalorieIntakeEntryFrequency() {
        double frequency = 0;
        int lastIndex = Math.min(dayOfExperiment - 2, habitWindowSize - 1);
        for (int i = lastIndex; i >= 0; i--) {
            if (i == lastIndex) {
                frequency += (calorieIntakeEntryQueue.get(i) ? 1 : 0) * 1;
            } else if (i <= (lastIndex - 1) && i >= (lastIndex - 2)) {
                frequency += (calorieIntakeEntryQueue.get(i) ? 1 : 0) * 0.5;
            } else if (i <= (lastIndex - 3) && i >= (lastIndex - 5)) {
                frequency += (calorieIntakeEntryQueue.get(i) ? 1 : 0) * 0.4;
            } else if (i <= (lastIndex - 6) && i >= (lastIndex - 9)) {
                frequency += (calorieIntakeEntryQueue.get(i) ? 1 : 0) * 0.3;
            } else if (i <= (lastIndex - 10) && i >= (lastIndex - 14)) {
                frequency += (calorieIntakeEntryQueue.get(i) ? 1 : 0) * 0.2;
            } else if (i <= (lastIndex - 15) && i >= (lastIndex - 20)) {
                frequency += (calorieIntakeEntryQueue.get(i) ? 1 : 0) * 0.1;
            }
        }
        return (int) Math.round(frequency);
    }

    public int calculateHabitGain() {
        if (difficulty.equals(TaskDifficulty.EASY)) {
            return (int) Math.min(Math.pow(Math.log10(Math.pow(habitGainOffset + 1, 6)), 0.9) * 6.4 / 42 * 100, 100.0);
        } else if (difficulty.equals(TaskDifficulty.MEDIUM)) {
            if (habitGainOffset <= 33) {
                return (int) (habitGainOffset + Math.log10(habitGainOffset) * 4.6) * 100 / 35;
            } else {
                return (int) (habitGainOffset + Math.log10(habitGainOffset - 33) * 4.6) * 100 / 35;
            }
        }
        return 0;
    }

    private void updateEntryWithoutIntervention(boolean entry, GroundedAction action) {
        int data;
        if (action.actionName().equals(ACTION_INT_DELIVERY)) {
            data = -1;
        } else {
            if (entry == true) {
                data = 1;
            } else {
                data = 0;
            }
        }

        entryWithoutIntervention.add(data);
        if (entryWithoutIntervention.size() > habitWindowSize) {
            entryWithoutIntervention.poll();
        }
    }

    private void updateCalorieIntakeEntryQueue() {
        calorieIntakeEntryQueue.add(calorieIntakeEntry);
        if (calorieIntakeEntryQueue.size() > habitWindowSize) {
            calorieIntakeEntryQueue.poll();
        }
    }

    public void updateEpisodeAnalysis() {
        if (p2dmea instanceof HabitYearEpisodeAnalysis) {
            ((HabitYearEpisodeAnalysis) p2dmea).addHabitGainEntry(HabitGainRatio.get(difficulty, habitGainOffset));
            ((HabitYearEpisodeAnalysis) p2dmea).addCalorieIntakeEntry(calorieIntakeEntry);
        }
    }

    public int getDayOfExperiment() {
        return dayOfExperiment;
    }

    public EpisodeAnalysis getEpisodeAnalysis() {
        return p2dmea;
    }

    public boolean getCalorieIntakeEntry() {
        return calorieIntakeEntry;
    }

    public boolean getDailyInterventionDelivery() {
        return dailyInterventionDelivery;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isHabitActive() {
        return isHabitActive;
    }

    public int getHabitGainOffset() {
        return habitGainOffset;
    }

    public void setHabitGainOffset(int habitGainOffset) {
        int offsetUpperLimit = 0;
        if (difficulty.equals(TaskDifficulty.EASY)) {
            offsetUpperLimit = 22;
        } else if (difficulty.equals(TaskDifficulty.MEDIUM)) {
            offsetUpperLimit = 65;
        } else if (difficulty.equals(TaskDifficulty.HARD)) {
            offsetUpperLimit = 88;
        }

        this.habitGainOffset = Math.min(habitGainOffset, offsetUpperLimit);
        this.habitGainOffset = Math.max(this.habitGainOffset, 0);
    }
}
