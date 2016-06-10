package power2dm.model.habit.hour;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * Created by suat on 08-Apr-16.
 */
public class HabitDailyRewardFunction implements RewardFunction {

    private HabitP2DMEnvironmentSimulator environmentSimulator;

    public void setEnvironmentSimulator(HabitP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }

    public double reward(State s, GroundedAction a, State sPrime) {
        boolean sCalorieIntakeEntry = s.getObject(HabitP2DMDomain.CLASS_STATE).getBooleanValForAttribute(HabitP2DMDomain.ATT_CAL_INTAKE_ENTRY);
        boolean sPrimeCalorieIntakeEntry = sPrime.getObject(HabitP2DMDomain.CLASS_STATE).getBooleanValForAttribute(HabitP2DMDomain.ATT_CAL_INTAKE_ENTRY);

        // no calorie intake entry in the current state
        if (sCalorieIntakeEntry == false) {
            // calorie intake entry in place in the next state
            if (sPrimeCalorieIntakeEntry == true) {
                // calorie intake has been entered thanks to the gained habit
                if (environmentSimulator.isHabitActive()) {
                    // punish if an intervention is delivered although the habit's been gained
                    if (a.actionName().equals(HabitP2DMDomain.ACTION_INT_DELIVERY)) {
//                        if(environmentSimulator.getCalorieIntakeProbability() > 0.5) {
//                            return -1 * environmentSimulator.getCalorieIntakeProbability();
//                        } else {
//                            return environmentSimulator.getCalorieIntakeProbability();
//                        }
                        return -1;
                        // reward when no intervention is delivered whtn the habit's been gained
                    } else {
                        return 1;
                    }
                } else {
                    // calorie intake has been entered thanks to the delivered intervention
                    if (a.actionName().equals(HabitP2DMDomain.ACTION_INT_DELIVERY)) {
                        return 1;
                    } else {
                        // the execution is not supposed arrive here
                        return 0;
                    }
                }
                // both first and the next states have no calorie intake
            } else {
                if (environmentSimulator.hourOfDay < 19) {
                    return 0;
                } else {
//                    if (environmentSimulator.isHabitActive()) {
//                        if (a.actionName().equals(ACTION_INT_DELIVERY)) {
//                            return -1 * environmentSimulator.getCalorieIntakeProbability();
//                        } else {
//                            if(environmentSimulator.hourOfDay == 24) {
//                                return -1;
//                            } else {
//                                return environmentSimulator.getCalorieIntakeProbability();
//                            }
//                        }
//                    } else {
//                        if (a.actionName().equals(ACTION_INT_DELIVERY)) {
//                            return 1;
//                        } else {
//                            return -1;
//                        }
//                    }
                    if(a.actionName().equals(HabitP2DMDomain.ACTION_INT_DELIVERY)) {
                        return -1;
                    } else {
                        if(environmentSimulator.hourOfDay == 24) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }
            }
            // calorie intake has already been entered
        } else {
            //punish if an intervention is delivered although the calorie intake has been entered
            if (a.actionName().equals(HabitP2DMDomain.ACTION_INT_DELIVERY)) {
                return -1;
                //reward if no intervention is delivered considering the calorie intake
            } else {
                return 1;
            }
        }
    }
}
