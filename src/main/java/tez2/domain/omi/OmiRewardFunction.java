package tez2.domain.omi;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import tez2.domain.SelfManagementRewardFunction;
import tez2.environment.simulator.SimulatedWorld;

import java.util.List;

import static tez2.domain.DomainConfig.ACTION_SEND_JITAI;

public class OmiRewardFunction extends SelfManagementRewardFunction {

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
        SimulatedWorld simulatedWorld = (SimulatedWorld) environment;
        if (a.action.getName().equals(ACTION_SEND_JITAI)) {
            boolean userReacted = simulatedWorld.isReactedToJitai();
            List<Integer> behaviorPerformanceTimeMean = simulatedWorld.getBehaviourPerformanceTimeMean();
            LocalTime currentLocalTime = simulatedWorld.currentTime.toLocalTime();
            int checkedActionPlanIndex = simulatedWorld.getCheckedActionPlanIndex();

            if(userReacted) {
                return REWARD_REACTION_TO_INTERVENTION;
            } else {
                double reward = 0;
                if(simulatedWorld.getNumberOfSentJitaisForAction() > 2) {
                    reward = simulatedWorld.getNumberOfSentJitaisForAction() * -2;
                } else {
                    reward = -1;
                }

                reward += getRewardByDistanceToMean(behaviorPerformanceTimeMean, currentLocalTime, checkedActionPlanIndex);
                return reward;

            }
        } else {
            return REWARD_NO_INTERVENTION;
        }
    }

    private double getRewardByDistanceToMean(List<Integer> currentMean, LocalTime currentLocalTime, int checkedActionPlanIndex) {
        if(currentMean == null || checkedActionPlanIndex % 2 != 0) {
            return 0;
        } else {
            int currentTimeMinutes = currentLocalTime.getHourOfDay() * 60 + currentLocalTime.getMinuteOfHour();
            int mean = currentMean.get(1);
            int difference = Math.abs(currentTimeMinutes - mean);
            if(difference > 60) {
                return -2;
            } else if(difference <= 60 && difference > 30) {
                return 0;
            } else {
                return 2;
            }
        }
    }
}
