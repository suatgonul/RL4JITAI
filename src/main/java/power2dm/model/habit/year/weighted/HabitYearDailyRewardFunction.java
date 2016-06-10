package power2dm.model.habit.year.weighted;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitYearDailyRewardFunction implements RewardFunction {

    private HabitYearP2DMEnvironmentSimulator environmentSimulator;

    public void setEnvironmentSimulator(HabitYearP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }

    public double reward(State s, GroundedAction a, State sPrime) {
        double reward;
        if(a.actionName().equals(HabitYearP2DMDomain.ACTION_NO_ACTION)){
            if(environmentSimulator.getCalorieIntakeEntry() == false) {
                reward = -2;
            } else {
                reward = 2;
            }
        } else {
            if(environmentSimulator.isHabitActive()) {
                reward = -1;
            } else {
                reward = 2;
            }
        }
        return reward;
    }
}
