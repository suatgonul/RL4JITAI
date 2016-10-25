package power2dm.model.habit.year.periodic.environment;

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
import power2dm.model.habit.year.periodic.reporting.HabitYearPeriodicEpisodeAnalysis;
import power2dm.reporting.P2DMEpisodeAnalysis;

import java.util.Random;

import static power2dm.model.habit.year.periodic.HabitYearPeriodicP2DMDomain.ACTION_INT_DELIVERY;

/**
 * Created by suat on 09-May-16.
 */
public class HabitYearPeriodicP2DMEnvironmentSimulator extends SimulatedEnvironment {
    protected UserPreference preferences = new UserPreference();
    private TaskDifficulty difficulty = TaskDifficulty.EASY;

    private int dayOfExperiment = 1;
    private int episodeOfExperiment = 0;
    private P2DMEpisodeAnalysis p2dmea = new HabitYearPeriodicEpisodeAnalysis(0, difficulty);

    protected Location location = Location.HOME;
    private boolean calorieIntakeEntry = true;
    private boolean dailyInterventionDelivery = false;

    //duration related
    private int totalCalorieIntakeAmount = 0;
    private int periodSize = 7;

    //automation related
    // 0: no entry without intervention
    // 1: entry without intervention
    // -1: intervention
    private Integer[] automationHistoryPeriodSizes = {1,3,5,20};
    private HistoryCache automationHistoryCache = new AutomationHistoryCache(automationHistoryPeriodSizes);

    // frequency related
    private Integer[] frequencyHistoryPeriodSizes = {1,3,5,20};
    private HistoryCache frequencyHistoryCache = new FrequencyHistoryCache(frequencyHistoryPeriodSizes);

    private boolean isHabitActive = false;
    private int habitGainOffset = 0;
    private int habitLossAmount = 2;


    public HabitYearPeriodicP2DMEnvironmentSimulator(Domain domain, RewardFunction rf, TerminalFunction tf, State initialState) {
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
        ((HabitYearPeriodicEpisodeAnalysis) p2dmea).addGeneratedRandom(rInt);

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

        updateEntryWithoutIntervention(action);
        updateCalorieIntakeEntryQueue();
        updateEpisodeAnalysis();
    }

    public void resetEnvironment() {
        super.resetEnvironment();

        int difficultyIndex = episodeOfExperiment % 3;
        if(difficultyIndex == 1) {
            difficulty = TaskDifficulty.EASY;
        } else if(difficultyIndex == 2) {
            difficulty = TaskDifficulty.MEDIUM;
        } else {
            difficulty = TaskDifficulty.HARD;
        }

        p2dmea = new HabitYearPeriodicEpisodeAnalysis(0, difficulty);
        dayOfExperiment = 0;
        episodeOfExperiment++;
        totalCalorieIntakeAmount = 0;
        habitGainOffset = 0;
        automationHistoryCache.destroy();
        automationHistoryCache = new AutomationHistoryCache(automationHistoryPeriodSizes);
        frequencyHistoryCache.destroy();
        frequencyHistoryCache = new FrequencyHistoryCache(frequencyHistoryPeriodSizes);
    }

    public int getAutomationRatioForPeriod(int periodNo) {
        return automationHistoryCache.getValueForPeriod(periodNo);
    }

    public int getPeriod() {
        return totalCalorieIntakeAmount / periodSize;
    }

    public int getFrequencyForPeriod(int periodNo) {
        return frequencyHistoryCache.getValueForPeriod(periodNo);
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

    private void updateEntryWithoutIntervention(GroundedAction action) {
        int newValue;
        if (action.actionName().equals(ACTION_INT_DELIVERY)) {
            newValue = -1;
        } else {
            if (calorieIntakeEntry == true) {
                newValue = 1;
            } else {
                newValue = 0;
            }
        }

        automationHistoryCache.addValue(newValue);
    }

    private void updateCalorieIntakeEntryQueue() {
        frequencyHistoryCache.addValue(calorieIntakeEntry == true ? 1 : 0);
    }

    public void updateEpisodeAnalysis() {
        if (p2dmea instanceof HabitYearPeriodicEpisodeAnalysis) {
            ((HabitYearPeriodicEpisodeAnalysis) p2dmea).addHabitGainEntry(HabitGainRatio.get(difficulty, habitGainOffset));
            ((HabitYearPeriodicEpisodeAnalysis) p2dmea).addCalorieIntakeEntry(calorieIntakeEntry);
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
            offsetUpperLimit = 21;
        } else if (difficulty.equals(TaskDifficulty.MEDIUM)) {
            offsetUpperLimit = 65;
        } else if (difficulty.equals(TaskDifficulty.HARD)) {
            offsetUpperLimit = 88;
        }

        this.habitGainOffset = Math.min(habitGainOffset, offsetUpperLimit);
        this.habitGainOffset = Math.max(this.habitGainOffset, 0);
    }
}
