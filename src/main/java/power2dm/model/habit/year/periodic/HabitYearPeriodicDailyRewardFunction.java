package power2dm.model.habit.year.periodic;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import power2dm.model.habit.year.periodic.environment.HabitYearPeriodicP2DMEnvironmentSimulator;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitYearPeriodicDailyRewardFunction implements RewardFunction {

    private HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator;

    public void setEnvironmentSimulator(HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }

    public double reward(State s, GroundedAction a, State sPrime) {
        double reward;
        if(a.actionName().equals(HabitYearPeriodicP2DMDomain.ACTION_NO_ACTION)){
            if(environmentSimulator.getCalorieIntakeEntry() == false) {
                reward = -1;
            } else {
                reward = 1;
            }
        } else {
            if(environmentSimulator.isHabitActive()) {
                reward = -1;
            } else {
                reward = 1;
            }
        }
        return reward;
    }
}
