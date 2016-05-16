package power2dm.model.habit;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

import static power2dm.model.habit.HabitP2DMDomain.*;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitDailyRewardFunction implements RewardFunction {

    private NewHabitP2DMEnvironmentSimulator environmentSimulator;

    public void setEnvironmentSimulator(NewHabitP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }

    public double reward(State s, GroundedAction a, State sPrime) {
        boolean sCalorieIntakeEntry = s.getObject(CLASS_STATE).getBooleanValForAttribute(ATT_CAL_INTAKE_ENTRY);
        boolean sPrimeCalorieIntakeEntry = sPrime.getObject(CLASS_STATE).getBooleanValForAttribute(ATT_CAL_INTAKE_ENTRY);

        if (sCalorieIntakeEntry == false) {
            if (sPrimeCalorieIntakeEntry == true) {
                if (environmentSimulator.isHabitActive()) {
                    if (a.actionName().equals(ACTION_INT_DELIVERY)) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    if(a.actionName().equals(ACTION_INT_DELIVERY)) {
                        return 1;
                    } else {
                        // the execution is not supposed arrive here
                        return 0;
                    }
                }
            } else {
                if(a.actionName().equals(ACTION_INT_DELIVERY)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        } else {
            if (a.actionName().equals(ACTION_INT_DELIVERY)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
