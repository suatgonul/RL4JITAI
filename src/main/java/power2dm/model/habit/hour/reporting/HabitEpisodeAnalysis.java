package power2dm.model.habit.hour.reporting;

import power2dm.reporting.P2DMEpisodeAnalysis;

/**
 * Created by suat on 16-May-16.
 */
public class HabitEpisodeAnalysis extends P2DMEpisodeAnalysis {
    private int episodeNo;
    private boolean isHabitActive;
    private boolean isInterventionDelivered;
    private boolean isCalorieIntakeEntered;
    private double entryProbabilityWhenInterventionDelivery;
    private int habitGainOffset;

    public HabitEpisodeAnalysis(P2DMEpisodeAnalysis ea, boolean isHabitActive, boolean isInterventionDelivered, boolean isCalorieIntakeEntered, double entryProbabilityWhenInterventionDelivery, int habitGainOffset) {
        super(ea);
        this.isHabitActive = isHabitActive;
        this.isInterventionDelivered = isInterventionDelivered;
        this.isCalorieIntakeEntered = isCalorieIntakeEntered;
        this.entryProbabilityWhenInterventionDelivery = entryProbabilityWhenInterventionDelivery;
        this.habitGainOffset = habitGainOffset;
        this.totalReward = ea.getTotalReward();
        this.episodeNo = ea.getEpisodeNo();
    }

    public int getEpisodeNo() {
        return episodeNo;
    }

    public boolean isHabitActive() {
        return isHabitActive;
    }

    public boolean isInterventionDelivered() {
        return isInterventionDelivered;
    }

    public boolean isCalorieIntakeEntered() {
        return isCalorieIntakeEntered;
    }

    public double getEntryProbabilityWhenInterventionDelivery() {
        return entryProbabilityWhenInterventionDelivery;
    }

    public int getHabitGainOffset() {
        return habitGainOffset;
    }
}
