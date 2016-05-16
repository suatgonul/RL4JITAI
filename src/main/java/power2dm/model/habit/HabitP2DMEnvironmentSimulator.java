//package power2dm.model.habit;
//
//import burlap.oomdp.core.states.State;
//import burlap.oomdp.singleagent.GroundedAction;
//import power2dm.algorithm.EnvironmentSimulator;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * Created by suat on 09-May-16.
// */
//public class HabitP2DMEnvironmentSimulator extends EnvironmentSimulator {
//    private boolean calorieIntakeEntry = true;
//    private static Map<Integer, Integer> habitGainPatterns = new LinkedHashMap<Integer, Integer>();
//
//    public HabitP2DMEnvironmentSimulator() {
//        setUserPreferences();
//        populateHabitGainPatterns();
//        resetEnvironmentForEpisode();
//    }
//
//    @Override
//    public boolean simulateUserReactionToIntervention(State s, GroundedAction groundedAction) {
//        if(hourOfDay < 19 || calorieIntakeEntry == true) {
//            return false;
//        }
//        return true;
//    }
//
//    public boolean getCalorieIntakeEntry() {
//        return calorieIntakeEntry;
//    }
//
//    public void updateEnvironment(State s, GroundedAction groundedAction) {
//        super.updateEnvironment(s, groundedAction);
//        simulateCalorieIntakeEntry(groundedAction);
//    }
//
//    private void populateHabitGainPatterns() {
//        habitGainPatterns.put(100, 1);
//        habitGainPatterns.put(5, 2);
//        habitGainPatterns.put(3, 3);
//        habitGainPatterns.put(1, 5);
//    }
//
//    private void simulateCalorieIntakeEntry(GroundedAction action) {
//        if(isHabitActive(dayOfExperiment)) {
//            if(hourOfDay == 20) {
//                calorieIntakeEntry = true;
//            }
//        } else {
//            if(hourOfDay >= 20) {
//                if(action.actionName().equals(HabitP2DMDomain.ACTION_INT_DELIVERY)) {
//                    calorieIntakeEntry = true;
//                }
//            }
//        }
//    }
//
//    public boolean isHabitActive() {
//        return isHabitActive(dayOfExperiment);
//    }
//
//    public boolean isHabitActive(int currentDayOfExperiment) {
//        int gainOffset = 0;
//        int remindingRange = 0;
//        int habitRange = 0;
//
//        for(Map.Entry<Integer, Integer> e : habitGainPatterns.entrySet()) {
//            remindingRange = e.getKey();
//            gainOffset += remindingRange;
//            if(currentDayOfExperiment <= gainOffset) {
//                return false;
//            }
//
//            habitRange = e.getValue();
//            gainOffset += habitRange;
//            if(currentDayOfExperiment <= gainOffset) {
//                return true;
//            }
//        }
//
//        while(true) {
//            gainOffset += remindingRange;
//            if(currentDayOfExperiment <= gainOffset) {
//                return false;
//            }
//
//            gainOffset += habitRange;
//            if(currentDayOfExperiment <= gainOffset) {
//                return true;
//            }
//        }
//    }
//
//    public void resetEnvironmentForEpisode() {
//        super.resetEnvironmentForEpisode();
//        calorieIntakeEntry = false;
//        dayOfExperiment++;
//    }
//}
