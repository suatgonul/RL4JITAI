package power2dm.model.habit;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import power2dm.model.Location;
import power2dm.model.UserPreference;
import power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by suat on 09-May-16.
 */
public class NewHabitP2DMEnvironmentSimulator extends SimulatedEnvironment {
    protected UserPreference preferences = new UserPreference();
    protected boolean fixedReaction = true;

    protected int dayOfExperiment = 1;
    protected int hourOfDay = 0;
    protected Location location = Location.HOME;

    private boolean calorieIntakeEntry = true;
    private static Map<Integer, Integer> habitGainPatterns = new LinkedHashMap<Integer, Integer>();

    public NewHabitP2DMEnvironmentSimulator(Domain domain, RewardFunction rf, TerminalFunction tf) {
        super(domain, rf, tf);
    }

    public NewHabitP2DMEnvironmentSimulator(Domain domain, RewardFunction rf, TerminalFunction tf, State initialState) {
        super(domain, rf, tf, initialState);
    }

    public void initialize() {
        setUserPreferences();
        populateHabitGainPatterns();
        hourOfDay = this.curState.getObject(ReactNonReactP2DMDomain.CLASS_STATE).getIntValForAttribute(ReactNonReactP2DMDomain.ATT_TIME);
    }

    public void updateEnvironment(State s, GroundedAction groundedAction) {
        hourOfDay++;
        if (hourOfDay < 8 || hourOfDay > 18) {
            location = Location.HOME;
        } else if (hourOfDay == 8 || hourOfDay == 18) {
            location = Location.ON_THE_WAY;
        } else {
            location = Location.WORK;
        }

        simulateCalorieIntakeEntry(groundedAction);
    }

    private void populateHabitGainPatterns() {
        habitGainPatterns.put(100, 1);
        habitGainPatterns.put(5, 2);
        habitGainPatterns.put(3, 3);
        habitGainPatterns.put(1, 5);
    }

    private void simulateCalorieIntakeEntry(GroundedAction action) {
        if(isHabitActive(dayOfExperiment)) {
            if(hourOfDay == 20) {
                calorieIntakeEntry = true;
            }
        } else {
            if(hourOfDay >= 20) {
                if(action.actionName().equals(HabitP2DMDomain.ACTION_INT_DELIVERY)) {
                    calorieIntakeEntry = true;
                }
            }
        }
    }

    public boolean isHabitActive() {
        return isHabitActive(dayOfExperiment);
    }

    public boolean isHabitActive(int currentDayOfExperiment) {
        int gainOffset = 0;
        int remindingRange = 0;
        int habitRange = 0;

        for(Map.Entry<Integer, Integer> e : habitGainPatterns.entrySet()) {
            remindingRange = e.getKey();
            gainOffset += remindingRange;
            if(currentDayOfExperiment <= gainOffset) {
                return false;
            }

            habitRange = e.getValue();
            gainOffset += habitRange;
            if(currentDayOfExperiment <= gainOffset) {
                return true;
            }
        }

        while(true) {
            gainOffset += remindingRange;
            if(currentDayOfExperiment <= gainOffset) {
                return false;
            }

            gainOffset += habitRange;
            if(currentDayOfExperiment <= gainOffset) {
                return true;
            }
        }
    }

    public void resetEnvironment() {
        super.resetEnvironment();
        calorieIntakeEntry = false;
        dayOfExperiment++;
        location = Location.HOME;
        hourOfDay = this.curState.getObject(ReactNonReactP2DMDomain.CLASS_STATE).getIntValForAttribute(ReactNonReactP2DMDomain.ATT_TIME);
    }

    public boolean getCalorieIntakeEntry() {
        return calorieIntakeEntry;
    }

    public Location getLocation() {
        return location;
    }

    protected void setUserPreferences() {
        preferences.createPreference(19, 21, Location.HOME);
    }
}
