package tez2.environment.simulator.habit;

import tez.environment.simulator.habit.visualization.h2.BfAccessibilityThreshold;

import javax.swing.*;
import java.util.*;

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
    // salience decay parameter
    private double SDP_REM;
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
    private double CI;
    private double ACC_FIRST;

    private Map<Double, BehaviorFrequency> behaviorFrequencies = new HashMap<>();
    private double salienceReminder;
    private double BF_EXE;
    private int step = 2;
    private boolean eventActive = false;
    private boolean behavior;

    // visualization data
    private List<BehaviorFrequency> selectedFrequencies = new ArrayList<>();

    public static void main(String[] args) {
        tez.environment.simulator.habit.HabitSimulator2 hs = new tez.environment.simulator.habit.HabitSimulator2();
        hs.simulateScenario();
        hs.drawCharts();
    }

    public HabitSimulator2() {
        ADP = 0.641;
        AGC_EVENT = 0.111;
        AGC_REM = 0.005;
        CAT = 0.749;
        DP = 0.886;
        DRH_AT = 0.975;
        HDP = 0.08;
        SDP_REM = 0.094;
        SS = 18.484;
        TS = 0.214;
        TP = TS;
        WBF_AGBEH = 0.221;
        WBF_AT = 0.355;
        WCI_EVENT = 0.997;
        WCI_REM = 0.083;
        WH_AT = 1.0;

        // initial values
        salienceReminder = 1.0;
        BF_EXE = 0.2;
        CI = 0.3;
        ACC_FIRST = BF_EXE;
    }

    private void simulateScenario() {
        for(double bf = 0.0; bf < 1.05; bf += 0.05) {
            behaviorFrequencies.put(bf, new BehaviorFrequency(bf,1.0, bf));
        }

        BehaviorFrequency firstBf = new BehaviorFrequency(BF_EXE,1.0, ACC_FIRST);
        selectedFrequencies.add(firstBf);

        for(; step < 150; step++) {
            System.out.println("**************** STEP " + step);
            if(step == 1) {
                eventActive = true;
            } else {
                eventActive = false;
            }
            simulateStep();
        }
    }

    private void simulateStep() {
        double maxBehaviorFrequency = 0.0;

        updateSalience();
        double bf;
        for(bf = 0.0; bf < 1.05; bf += 0.05) {
            updateBfAccessibility(bf);
            updateHabitStrength(bf);
            checkAccessibility(bf);
            if(behavior) {
                maxBehaviorFrequency = bf;
            }
        }
        BF_EXE = maxBehaviorFrequency;
        if(new Random().nextInt(10) % 10 < 3) BF_EXE = 0.0; else BF_EXE = bf-0.05;

        selectedFrequencies.add(behaviorFrequencies.get(BF_EXE).copy());
        System.out.println("Selected bf: " + BF_EXE);
    }

    private void checkAccessibility(double behaviorFrequency) {
        BehaviorFrequency bf = behaviorFrequencies.get(behaviorFrequency);
        double threshold = CAT - (CAT * WH_AT * bf.getHabitStrength()) + (1.0 - CAT) * WBF_AT * behaviorFrequency * (1.0 - DRH_AT * bf.getHabitStrength());
        bf.setThreshold(threshold);

        behavior = bf.getAccessibility() >= threshold;

        //if(behaviorFrequency == 0) {
            System.out.println("Behavior frequency: " + behaviorFrequency);
            System.out.println("Behavior: " + behavior);
            System.out.println("Threshold: " + threshold);
            System.out.println("Accessibility: " + bf.getAccessibility());
            //System.out.println("Salience remainder: " + salienceReminder);
            System.out.println();
        //}
    }

    private void updateBfAccessibility(double behaviorFrequency) {
        BehaviorFrequency bf = behaviorFrequencies.get(behaviorFrequency);
        double accDecay = bf.getAccessibility() * ADP;
        double accGainEvent = 0;
        if(eventActive) {
            accGainEvent = AGC_EVENT * (1.0 - AGC_EVENT) * WCI_EVENT * CI;
        }
        double accGainBeh = 0;
        if(BF_EXE != 0) {
            accGainBeh = BF_EXE * WBF_AGBEH;
        }
        double accGainRem = (AGC_REM + (1.0-AGC_REM) * WCI_REM * CI) * calculateSimilarity(behaviorFrequency) * salienceReminder;
        bf.setAccessibility(Math.max(0, Math.min(1, bf.getAccessibility() - accDecay + accGainEvent + accGainBeh + accGainRem)));
    }

    public void updateHabitStrength(double behaviorFrequency) {
        BehaviorFrequency bf = behaviorFrequencies.get(behaviorFrequency);
        double habitDecay;
//        if(behavior == false) {
            habitDecay = bf.getHabitStrength() * HDP;
//        } else {
//            habitDecay = 0;
//        }

        double habitGainBF;
        if(BF_EXE != 0) {
            double habitGainExe = (bf.getHabitStrength() * (1.0 - BF_EXE) + BF_EXE) * HDP;
            habitGainBF = (1.0 - calculateSimilarity(BF_EXE) + calculateSimilarity(behaviorFrequency) * habitGainExe);
        } else {
            habitGainBF = 0;
        }

        bf.setHabitStrength(Math.max(0, Math.min(1, bf.getHabitStrength() - habitDecay + habitGainBF)));
    }

    private void updateSalience() {
        double salienceDecay = salienceReminder * SDP_REM;
        salienceReminder -= salienceDecay;
    }


    private double calculateSimilarity(double behaviorFrequency) {
        double similarity = 1.0 -
                ((1.0 / (1 + Math.exp((0.5 - Math.pow(behaviorFrequency, TS)) * SS)) -
                        (1.0 / (1 + Math.exp(0.5 * SS)))) /
                        ((1.0 / (1 + Math.exp(-0.5 * SS))) -
                                (1.0 / (1 + Math.exp(0.5 * SS))))) *
                        DP;
        return similarity;
    }

    private void drawCharts() {
        // show behaviour chart
        SwingUtilities.invokeLater(() -> {
            BfAccessibilityThreshold example = new BfAccessibilityThreshold("BFs");
            example.showChart(selectedFrequencies);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }

    public static class BehaviorFrequency {
        private double behaviourFrequency;
        private double habitStrength;
        private double accessibility;
        private double threshold;

        public BehaviorFrequency(double behaviourFrequency, double habitStrength, double accessibility) {
            this.behaviourFrequency = behaviourFrequency;
            this.habitStrength = habitStrength;
            this.accessibility = accessibility;
        }

        public double getHabitStrength() {
            return habitStrength;
        }

        public void setHabitStrength(double habitStrength) {
            this.habitStrength = habitStrength;
        }

        public double getAccessibility() {
            return accessibility;
        }

        public void setAccessibility(double accessibility) {
            this.accessibility = accessibility;
        }

        public double getThreshold() {
            return threshold;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public double getBehaviourFrequency() {
            return behaviourFrequency;
        }

        public void setBehaviourFrequency(double behaviourFrequency) {
            this.behaviourFrequency = behaviourFrequency;
        }

        public BehaviorFrequency copy() {
            BehaviorFrequency copy = new BehaviorFrequency(this.behaviourFrequency, this.habitStrength, this.accessibility);
            copy.setThreshold(this.threshold);
            return copy;
        }
    }
}
