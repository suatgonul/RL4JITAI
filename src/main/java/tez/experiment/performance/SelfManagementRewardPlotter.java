package tez.experiment.performance;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.PerformancePlotter;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by suatgonul on 4/27/2017.
 */
public class SelfManagementRewardPlotter extends PerformancePlotter {
    public SelfManagementRewardPlotter(String firstAgentName, int chartWidth, int chartHeight, int columns, int maxWindowHeight, TrialMode trialMode, PerformanceMetric... metrics) {
        super(firstAgentName, chartWidth, chartHeight, columns, maxWindowHeight, trialMode, metrics);
    }

    public class SelfManagementTrial extends Trial {
        public List<Integer> cumulativeEpisodeUserReaction = new ArrayList<>();
        public List<Double> cumulativeRewardInEpisode = new ArrayList<>();
        public int currentEpisodeUserReaction;

        @Override
        public void stepIncrement(double r){
            super.stepIncrement(r);
            this.currentEpisodeUserReaction++;
        }

        @Override
        public void setupForNewEpisode(){
            accumulate(this.cumulativeEpisodeReward, this.curEpisodeReward);
            accumulate(this.cumulativeStepEpisode, this.curEpisodeSteps);

            double avgER = this.curEpisodeReward / (double)this.curEpisodeSteps;
            this.averageEpisodeReward.add(avgER);
            this.stepEpisode.add((double)this.curEpisodeSteps);
            this.cumulativeEpisodeUserReaction.add(this.currentEpisodeUserReaction);
            this.cumulativeRewardInEpisode.add(this.curEpisodeReward);

            Collections.sort(this.curEpisodeRewards);
            double med = 0.;
            if(this.curEpisodeSteps > 0){
                int n2 = this.curEpisodeSteps / 2;
                if(this.curEpisodeSteps % 2 == 0){
                    double m = this.curEpisodeRewards.get(n2);
                    double m2 = this.curEpisodeRewards.get(n2-1);
                    med = (m + m2) / 2.;
                }
                else{
                    med = this.curEpisodeRewards.get(n2);
                }
            }

            this.medianEpisodeReward.add(med);


            this.totalSteps += this.curEpisodeSteps;
            this.totalEpisodes++;

            this.curEpisodeReward = 0.;
            this.curEpisodeSteps = 0;
            this.currentEpisodeUserReaction = 0;
            this.curEpisodeRewards.clear();
        }
    }
}
