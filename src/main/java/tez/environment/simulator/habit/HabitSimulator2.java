package tez.environment.simulator.habit;

import java.util.ArrayList;
import java.util.List;

public class HabitSimulator2 {
    // accessability decay parameter
    private double ADP;
    // accessability gain constant for events
    private double AGC_EVENT;
    // accessability gain constant for reminders
    private double AGC_REM;
    // accessability threshold constant
    private double CAT;
    // dissimilarity parameter
    private double DP;
    // distraction reduction due to habits
    private double DRH_AT;
    // habit decay parameter
    private double HDP;
    // slope of the similarity function
    private double SS;
    // turning point parameter of the similarity function
    private double TS;
    // turning point parameter of the similarity function for prompt
    private double TP;
    // behaviour frequency weight for accessability gain by behaviour
    private double WBF_AGBEH;
    // behaviour frequency weight for accessability threshold
    private double WBF_AT;
    // commitment intensity weight for events
    private double WCI_EVENT;
    // commitment intensity weight for reminders
    private double WCI_REM;
    // weight for habit on accessability threshold
    private double WH_AT;

    // agent-specific
    private double ACC_START;
    private double BF_START;
    private double CI;

    // salience decay parameter
    private double ACC_BF;
    private double BF;


    // instant values of habit parameters
    private double salienceReminder; // set 1 to at each reminder
    private double accessability;
    private double commitmentIntensity;
    private double habitStrength;
    private boolean behaviour;
    private boolean reminder = true;

    private int windowSize = 14;
    private List<Boolean> behaviourWindow;

    // visualization data
    private List<Boolean> behaviours = new ArrayList<>();
    private List<Boolean> reminders = new ArrayList<>();
    private List<Double> accessibilities = new ArrayList<>();
    private List<Double> thresholds = new ArrayList<>();
    private List<Double> behaviourFrequencies = new ArrayList<>();
    private List<Double> habitStrengths = new ArrayList<>();

    // threshold
    private List<Double> positives = new ArrayList<>();
    private List<Double> negatives = new ArrayList<>();

    private int day = 1;

    public static void main(String[] args) {
        HabitSimulator2 hs = new HabitSimulator2();
    }

    private boolean checkAccessibility() {
        double threshold = CAT - (CAT * WH_AT * habitStrength) + (1 - CAT) * WBF_AT * BF * (1 - DRH_AT * habitStrength);
        return ACC_BF >= threshold;
    }

    private void updateBfAccessibility() {
        double accDecay = ACC_BF - ADP;
        double accGainEvent = AGC_EVENT * (1-AGC_EVENT) * WCI_EVENT * CI;
        double accGainBeh = BF * WBF_AGBEH;
        double accGainRem = (AGC_REM + (1-AGC_REM) * WCI_REM * CI) * calculateSimilarity() * salienceReminder;
        ACC_BF = ACC_BF - accDecay + accGainEvent + accGainBeh + accGainRem;
    }

    public void updateHabit() {
        double habitDecay;
        if(behaviour == false) {
            // 8) HabitDecay = HS * HDP
            habitDecay = habitStrength * HDP;
        } else {
            habitDecay = 0;
        }

        double habitGainBF;
        if(behaviour == true) {
            double habitGainExe = (habitStrength * (1 - BF) + BF) * HDP;
            habitGainBF = (1 - calculateSimilarity() + calculateSimilarity() * habitGainExe);
        } else {
            habitGainBF = 0;
        }

        // 12) HSt1 = HSt - HabitDecay + HabitGain
        //habitStrength = habitStrength - habitDecay + habitGainBF;
        habitStrength = habitStrength - habitDecay + habitGainBF;
    }

    public double calculateSimilarity() {
        double similarity = 1 -
                ((1.0 / (1 + Math.exp((0.5 - Math.pow(BF    , TS)) * SS)) -
                        (1.0 / (1 + Math.exp(0.5 * SS)))) /
                        ((1.0 / (1 + Math.exp(-0.5 * SS))) -
                                (1.0 / (1 + Math.exp(0.5 * SS))))) *
                        DP;
        return similarity;
    }
}
