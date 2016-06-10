package power2dm.model.habit.hour;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import power2dm.model.Location;
import power2dm.model.UserPreference;
import power2dm.model.reacted_non_reacted_numbers.ReactNonReactP2DMDomain;

import java.util.LinkedList;
import java.util.Random;

import static power2dm.model.habit.hour.HabitP2DMDomain.*;

/**
 * Created by suat on 09-May-16.
 */
public class HabitP2DMEnvironmentSimulator extends SimulatedEnvironment {
    protected UserPreference preferences = new UserPreference();
    protected boolean fixedReaction = true;

    protected int dayOfExperiment = 1;
    protected int hourOfDay = 0;
    protected Location location = Location.HOME;
    private boolean calorieIntakeEntry = true;
    private double entryProbabilityWhenInterventionDelivery = 0;

    private int habitGainOffset = 0;
    private int sequentialEntryDays = 0;
    private int habitWindowSize = 20;
    private int habitLossWindowSize = 7;
    private boolean dailyInterventionDelivery = false;
    private LinkedList<Boolean> interventionDeliveryQueue = new LinkedList<Boolean>();
    private double habitLossRatio = 1.2;
    private boolean isHabitActive = false;
    private double nonHabitRandomization = 0.3;

//    private static List<HabitModel> initialHabitGainPatterns = new ArrayList<HabitModel>();
//    private static List<HabitModel> learnedHabitGainPatterns = new ArrayList<HabitModel>();

    private boolean learnedPatternSet = false;
    private int currentPatternIndex = 0;
    private int currentPatternOffset = 0;


    public HabitP2DMEnvironmentSimulator(Domain domain, RewardFunction rf, TerminalFunction tf) {
        super(domain, rf, tf);
    }

    public HabitP2DMEnvironmentSimulator(Domain domain, RewardFunction rf, TerminalFunction tf, State initialState) {
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

//        if(groundedAction.actionName().equals(ACTION_INT_DELIVERY)) {
//            int habitCoeff = habitCoeffMap.get(hourOfDay-1);
//            habitCoeffMap.put(hourOfDay-1, ++habitCoeff);
//        }

        if (groundedAction.actionName().equals(ACTION_INT_DELIVERY) && hourOfDay > 18) {
            dailyInterventionDelivery = true;
        }
        simulateCalorieIntakeEntry(groundedAction);
    }

    private void populateHabitGainPatterns() {

//        initialHabitGainPatterns.add(new HabitModel(10, 1));
//        initialHabitGainPatterns.add(new HabitModel(5, 2));
//        initialHabitGainPatterns.add(new HabitModel(3, 3));
//        learnedHabitGainPatterns.add(new HabitModel(1, 3));
//        learnedHabitGainPatterns.add(new HabitModel(1, 5));
//        learnedHabitGainPatterns.add(new HabitModel(2, 3));
//        learnedHabitGainPatterns.add(new HabitModel(2, 5));
    }

//    private void simulateCalorieIntakeEntry(GroundedAction action) {
//        if (calorieIntakeEntry == true) {
//            return;
//        }
//
//        if (isHabitActive(dayOfExperiment)) {
//            if (hourOfDay >= 20) {
//                Random r = new Random();
//                double rDouble = r.nextDouble();
//                double calorieIntakeProbability = (hourOfDay - 19) * 0.20;
//                if (rDouble <= calorieIntakeProbability) {
//                    calorieIntakeEntry = true;
//                }
//            }
//        } else {
//            if (hourOfDay >= 20) {
//                if (action.actionName().equals(HabitYearPeriodicP2DMDomain.ACTION_INT_DELIVERY)) {
//                    calorieIntakeEntry = true;
//                }
//            }
//        }
//    }

    private void simulateCalorieIntakeEntry(GroundedAction action) {
        if (calorieIntakeEntry == true) {
            return;
        }

        if (isHabitActive) {
            if (hourOfDay >= 20) {
                double calorieIntakeProbability = getCalorieIntakeProbability();
                if (action.actionName().equals(ACTION_INT_DELIVERY)) {
                    entryProbabilityWhenInterventionDelivery = calorieIntakeProbability;
                    calorieIntakeEntry = true;
                } else {
                    Random r = new Random();
                    double rDouble = r.nextDouble();
                    if (rDouble <= calorieIntakeProbability) {
                        calorieIntakeEntry = true;
                    }
                }
            }
        } else {
            if (hourOfDay >= 20) {
                if (action.actionName().equals(HabitP2DMDomain.ACTION_INT_DELIVERY)) {
                    entryProbabilityWhenInterventionDelivery = 0;
                    calorieIntakeEntry = true;
                }
            }
        }


    }

    public boolean isHabitActive() {
        return isHabitActive;
    }

    public double getCalorieIntakeProbability() {
        return Math.max(1 - ((hourOfDay - 18) * 0.20), 0);
    }

//    public boolean isHabitActive(int currentDayOfExperiment) {
//        int gainOffset = 0;
//        int remindingRange;
//        int habitRange;
//
//        for (HabitModel hm : initialHabitGainPatterns) {
//            remindingRange = hm.getNonHabitRange();
//            gainOffset += remindingRange;
//            if (currentDayOfExperiment <= gainOffset) {
//                return false;
//            }
//
//            habitRange = hm.getHabitRange();
//            gainOffset += habitRange;
//            if (currentDayOfExperiment <= gainOffset) {
//                return true;
//            }
//        }
//
//        HabitModel hm = learnedHabitGainPatterns.get(currentPatternIndex);
//        if (currentPatternOffset >= hm.getNonHabitRange()) {
//            return true;
//        } else {
//            return false;
//        }
//    }

//    private int updateHabitLossRatio() {
//
//    }

    public void resetEnvironment() {
        super.resetEnvironment();
        setHabitForNextEpisode();

        ObjectInstance stateInstance = curState.getFirstObjectOfClass(CLASS_STATE);
        curState.setObjectsValue(stateInstance.getName(), ATT_CAL_INTAKE_ENTRY, false);
        dailyInterventionDelivery = false;
        calorieIntakeEntry = false;
        entryProbabilityWhenInterventionDelivery = 0;
        dayOfExperiment++;

//        if (learnedPatternSet == false) {
//            Random r = new Random();
//            currentPatternIndex = r.nextInt(learnedHabitGainPatterns.size());
//            currentPatternOffset = 0;
//            learnedPatternSet = true;
//        } else {
//            currentPatternOffset++;
//        }
//
//        HabitModel hm = learnedHabitGainPatterns.get(currentPatternIndex);
//        if (currentPatternOffset == (hm.getHabitRange() + hm.getNonHabitRange())) {
//            learnedPatternSet = false;
//        }

        location = Location.HOME;
        hourOfDay = this.curState.getObject(ReactNonReactP2DMDomain.CLASS_STATE).getIntValForAttribute(ReactNonReactP2DMDomain.ATT_TIME);
    }

    private void setHabitForNextEpisode() {
        if (hourOfDay == 24) {
            if (calorieIntakeEntry == true) {
                setHabitGainOffset(habitGainOffset + 1);
                sequentialEntryDays++;
            } else {
                setHabitGainOffset((int) (habitGainOffset / habitLossRatio));
                sequentialEntryDays = 0;
            }

            updateInterventionDeliveryQueue(dailyInterventionDelivery);

            if (sequentialEntryDays >= habitWindowSize) {
                decreaseHabitGainOffset();
            }

            Random r = new Random();
            int rInt = r.nextInt((100 - 0) + 1) + 0;
            if (rInt < calculateHabitGainRatio(habitGainOffset)) {
                isHabitActive = true;
            } else {
                isHabitActive = false;
            }
        }
    }

    public int calculateHabitGainRatio(int habitGainOffset) {
        return (int) Math.min(Math.pow(Math.log10(Math.pow(habitGainOffset + 1, 6)), 0.9) * 6.4 / 42 * 100, 100.0);
    }

    private void updateInterventionDeliveryQueue(boolean interventionDelivery) {
        interventionDeliveryQueue.add(interventionDelivery);
        if (interventionDeliveryQueue.size() > habitLossWindowSize) {
            interventionDeliveryQueue.poll();
        }
    }

    private void decreaseHabitGainOffset() {
        if (interventionDeliveryQueue.size() == habitLossWindowSize) {
            double decreaseAmount = 0;
            for (int i = 0; i < habitLossWindowSize; i++) {
                switch (i) {
//                    case 0: decreaseAmount += (interventionDeliveryQueue.get(i) ? 0: 1) * 0.2; break;
//                    case 1: decreaseAmount += (interventionDeliveryQueue.get(i) ? 0: 1) * 0.2; break;
//                    case 2: decreaseAmount += (interventionDeliveryQueue.get(i) ? 0: 1) * 0.3; break;
//                    case 3: decreaseAmount += (interventionDeliveryQueue.get(i) ? 0: 1) * 0.3; break;
//                    case 4: decreaseAmount += (interventionDeliveryQueue.get(i) ? 0: 1) * 0.4; break;
//                    case 5: decreaseAmount += (interventionDeliveryQueue.get(i) ? 0: 1) * 0.8; break;
//                    case 6: decreaseAmount += (interventionDeliveryQueue.get(i) ? 0: 1) * 0.8; break;
                    case 0:
                        decreaseAmount += (interventionDeliveryQueue.get(i) ? 0 : 1) * 0.1;
                        break;
                    case 1:
                        decreaseAmount += (interventionDeliveryQueue.get(i) ? 0 : 1) * 0.1;
                        break;
                    case 2:
                        decreaseAmount += (interventionDeliveryQueue.get(i) ? 0 : 1) * 0.2;
                        break;
                    case 3:
                        decreaseAmount += (interventionDeliveryQueue.get(i) ? 0 : 1) * 0.2;
                        break;
                    case 4:
                        decreaseAmount += (interventionDeliveryQueue.get(i) ? 0 : 1) * 0.3;
                        break;
                    case 5:
                        decreaseAmount += (interventionDeliveryQueue.get(i) ? 0 : 1) * 0.5;
                        break;
                    case 6:
                        decreaseAmount += (interventionDeliveryQueue.get(i) ? 0 : 1) * 0.6;
                        break;
                }
//                if (interventionDeliveryQueue.get(i) == false) {
//                    decreaseAmount++;
//                }
            }

//            if(habitGainOffset > 4) {
//                setHabitGainOffset(habitGainOffset - decreaseAmount);
//            }
            setHabitGainOffset(habitGainOffset - (int) decreaseAmount);
        }
    }

    private boolean simulateRandomNonHabit() {
        Random r = new Random();
        double rDouble = r.nextDouble();

        if (rDouble < nonHabitRandomization) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getCalorieIntakeEntry() {
        return calorieIntakeEntry;
    }

    public double getEntryProbabilityWhenInterventionDelivery() {
        return entryProbabilityWhenInterventionDelivery;
    }

    public Location getLocation() {
        return location;
    }


    public double getHabitGainRatio() {
        return calculateHabitGainRatio(habitGainOffset);
    }

    public int getHabitGainOffset() {
        return habitGainOffset;
    }

    public void setHabitGainOffset(int habitGainOffset) {
        this.habitGainOffset = Math.min(habitGainOffset, 20);
        this.habitGainOffset = Math.max(this.habitGainOffset, 0);
    }

    protected void setUserPreferences() {
        preferences.createPreference(19, 21, Location.HOME);
    }

    private class HabitModel {
        private int nonHabitRange;
        private int habitRange;

        public HabitModel(int nonHabitRange, int habitRange) {
            this.nonHabitRange = nonHabitRange;
            this.habitRange = habitRange;
        }

        public int getNonHabitRange() {
            return nonHabitRange;
        }

        public int getHabitRange() {
            return habitRange;
        }
    }
}
