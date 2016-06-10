package power2dm.reporting;

import burlap.behavior.singleagent.EpisodeAnalysis;

/**
 * Created by suat on 16-May-16.
 */
public class P2DMEpisodeAnalysis extends EpisodeAnalysis {

    protected int episodeNo;
    protected double totalReward;

    public P2DMEpisodeAnalysis(int episodeNo) {
        this.episodeNo = episodeNo;
    }

    public P2DMEpisodeAnalysis(EpisodeAnalysis ea) {
        appendAndMergeEpisodeAnalysis(ea);
    }

    public int getEpisodeNo() {
        return episodeNo;
    }

    public void setEpisodeNo(int episodeNo) {
        this.episodeNo = episodeNo;
    }

    public double getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(double totalReward) {
        this.totalReward = totalReward;
    }
}
