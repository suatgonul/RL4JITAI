package power2dm.model.habit.reporting;

import power2dm.reporting.P2DMEpisodeAnalysis;

/**
 * Created by suat on 16-May-16.
 */
public class HabitEpisodeAnalysis extends P2DMEpisodeAnalysis {
    private int episodeNo;
    private boolean isHabitActive;
    private boolean isInterventionDelivered;

    public HabitEpisodeAnalysis(P2DMEpisodeAnalysis ea, boolean isHabitActive, boolean isInterventionDelivered) {
        super(ea);
        this.isHabitActive = isHabitActive;
        this.isInterventionDelivered = isInterventionDelivered;
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
}
