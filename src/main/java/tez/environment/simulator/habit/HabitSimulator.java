package tez.environment.simulator.habit;

import tez.environment.simulator.habit.visualization.AccessibilityThresholdChart;
import tez.environment.simulator.habit.visualization.BehaviourChart;
import tez.environment.simulator.habit.visualization.ThresholdChart;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HabitSimulator {
    // accessability gain constant for reminders
    private double AGC_REM;
    // accessability gain constant for events
    private double AGC_EVENT;
    // accessability gain constant for behaviours
    private double AGC_BEH;
    // behaviour frequency weight for accessability gain by behaviour
    private double WBF_AGBEH;
    // behaviour frequency weight for accessability threshold
    private double WBF_AT;
    // commitment intensity weight for reminders
    private double WCI_REM;
    // commitment intensity weight for events
    private double WCI_EVENT;
    // weight for habit on accessability threshold
    private double WH_AT;
    // salience decay parameter
    private double SDP_REM;
    // habit decay parameter
    private double HDP;
    // accessability decay parameter
    private double ADP;
    // accessability threshold constant
    private double CAT;

    // similarity function related constants
    private double BF_TS;
    private double SS;
    private double DP;

    // instant values of habit parameters
    private double behaviourFrequency;
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
        HabitSimulator hs = new HabitSimulator();
        hs.setAGC_REM(0.037);
        hs.setAGC_EVENT(0.615);
        hs.setAGC_BEH(0.21);
        hs.setWBF_AGBEH(0.851);
        hs.setWBF_AT(0.446);
        hs.setWCI_REM(0.578);
        hs.setWCI_EVENT(0.757);
        hs.setWH_AT(0.515);
        hs.setSDP_REM(0.028);
        hs.setADP(0.760);
        hs.setHDP(0.082);
        hs.setCAT(0.504);
        hs.setBF_TS(0.523);
        hs.setSS(19.522);
        hs.setDP(0.790);

        hs.setBehaviourFrequency(0.5);
        hs.setCommitmentIntensity(0.0);
        hs.setHabitStrength(0.5);
        hs.setSalienceReminder(0.0);
        hs.setAccessability(0.0);
        hs.initialBehaviourList();

        hs.simulateScenario(hs);

    }

    private void simulateScenario(HabitSimulator hs) {
        List<Boolean> simulatedBehaviours = new ArrayList<>();

        // simulate salience decay
        for (day = 1; day <= 150; day++) {
            if (day == 1) {
                hs.setSalienceReminder(1.0);
                hs.setCommitmentIntensity(1.0);
            }
            System.out.println("Day: " + day + "=================");
            boolean behaviour = hs.simulateBehaviour();
            simulatedBehaviours.add(behaviour);
            System.out.println("Updated values: ====");
            System.out.println("Behaviour: " + behaviour);
            System.out.println("Accessibility: " + hs.getAccessability());
            System.out.println("Habit strength: " + hs.getHabitStrength());
            System.out.println("Frequency: " + hs.getBehaviourFrequency());
            System.out.println("Salience: " + hs.getSalienceReminder());
            System.out.println();
        }

        hs.drawCharts();
    }

    private int weeklyPerforming = 0;
    private int weeklyTotal = 0;
    public boolean simulateBehaviour() {
        // calculate behaviour frequency threshold
        // 1) ThresholdBF = CAT + WBFAT * BF - WHAT * HSBF
        double behaviourFrequencyThreshold = CAT + WBF_AT * behaviourFrequency - WH_AT * habitStrength;

        behaviour = behaviourFrequencyThreshold < accessability;
        System.out.println("Behaviour frequency threshold: " + behaviourFrequencyThreshold);
        System.out.println("Accessibility: " + accessability);

       // behaviour = (!behaviour && new Random().nextInt(5) < 4) ? true : false;
//        if(day % 20 == 0) behaviour = true;
            //behaviour = false;
        //if(day == 19 || day == 35) behaviour = true;
        //behaviour = new Random().nextInt(5) < 3 ? false : true;
        if( day % 7 == 1) {
            weeklyTotal = new Random().nextInt(7) + 1;
            weeklyPerforming = 0;
        }
        if(weeklyPerforming < weeklyTotal) {
            int remainingDays = 8 - (day % 8);
            if(weeklyTotal - weeklyPerforming == remainingDays) {
                behaviour = true;
            } else {
                behaviour = new Random().nextDouble() <= 0.5 ? true : false;
            }
        } else {
            behaviour = false;
        }
        if(behaviour) {
            weeklyPerforming++;
        }

        behaviours.add(behaviour);
        accessibilities.add(accessability);
        thresholds.add(behaviourFrequencyThreshold);
        behaviourFrequencies.add(behaviourFrequency);
        habitStrengths.add(habitStrength);

        positives.add(WBF_AT * behaviourFrequency);
        negatives.add(WH_AT * habitStrength);

        // update behaviour frequency
        updateBehaviourFrequency(behaviour);

        // update accessability
        updateAccessability();

        // update habit
        updateHabit();

        // update reminder //TODO this will be integrated with the RL part
        if(behaviour) {
            reminder = false;
        } else {
            reminder = true;
        }
        reminders.add(reminder);

        // update salience
        updateSalience();
        return behaviour;
    }

    private void updateBehaviourFrequency(boolean behaviour) {
        behaviourWindow.remove(0);
        behaviourWindow.add(behaviour);

        int performedBehaviour = 0;
        for (int i = 0; i < windowSize; i++) {
            performedBehaviour += behaviourWindow.get(i) ? 1 : 0;
        }

        behaviourFrequency = Math.max(0, (double) performedBehaviour / (double) windowSize);
    }

    public void updateAccessability() {
        // 2) AccDecay = Accessibility * ADP
        double accessAbilityDecay = accessability * ADP;

        double accessabilityGainEvent;
        if (day != 1) {
            accessabilityGainEvent = 0;
        } else {
            // 3) AccGainEvent = AGCEvent + WCIEvent * CI
            accessabilityGainEvent = AGC_EVENT + WCI_EVENT * commitmentIntensity;
        }

        double accessAbilityGainBeh;
        if(behaviour) {
            // 4) AccGainBeh = AGCBeh + WBFAGBeh * WBFAT * BFExe.
            accessAbilityGainBeh = AGC_BEH + WBF_AGBEH * WBF_AT * behaviourFrequency;
        } else {
            accessAbilityGainBeh = 0;
        }

        double accessabilityGainRem;
        if(reminder) {
            // 6) AccGainRem = (AGCRem + WCIRem * CI) *Sf(BF)* SalienceRem.
            accessabilityGainRem = (AGC_REM + WCI_REM * commitmentIntensity) * calculateSimilarity() * salienceReminder;
        } else {
            accessabilityGainRem = 0;
        }


        System.out.println("**** ACCESSIBILITY ******");
        System.out.println("Access decay: " + accessAbilityDecay);
        System.out.println("Access gain event: " + accessabilityGainEvent);
        System.out.println("Access gain beh: " + accessAbilityGainBeh);
        System.out.println("Access gain rem: " + accessabilityGainRem);
        System.out.println();

        // 11) Acct1 = Acct - AccDecay + AccGainEvent+ AccGainBeh + AccGainRem
        accessability = accessability - accessAbilityDecay + accessabilityGainEvent + accessAbilityGainBeh + accessabilityGainRem;
        //accessability = Math.max(0.0, Math.min(1.0, accessability - accessAbilityDecay + accessabilityGainEvent + accessAbilityGainBeh + accessabilityGainRem));
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
            // 9) HabitGainExe =(HS * (1-BFExe) + BFExe) * HDP.
            double habitGainExe = (habitStrength * (1 - behaviourFrequency) + behaviourFrequency) * HDP;

            // 10) HabitGainBF = min(1, max(0, Sf(BF)+(1 - Sf(BfExe)))) * HabitGainExe
            habitGainBF = Math.min(1, Math.max(0, calculateSimilarity() + (1 - calculateSimilarity()))) * habitGainExe;
        } else {
            habitGainBF = 0;
        }

        System.out.println("**** HABITS *****");
        System.out.println("Habit decay: " + habitDecay);
        System.out.println("Habit gain bf: " + habitGainBF);
        System.out.println();

        // 12) HSt1 = HSt - HabitDecay + HabitGain
        //habitStrength = habitStrength - habitDecay + habitGainBF;
        habitStrength = Math.max(0.0, Math.min(1.0, habitStrength - habitDecay + habitGainBF));
    }

    private void updateSalience() {
        if(reminder) {
            // 7) SalienceDecay = Salience * SDPRem
            double salienceDecay = salienceReminder * SDP_REM;
            salienceReminder -= salienceDecay;
        }
    }

    public double calculateSimilarity() {
        double similarity = 1 -
                ((1.0 / (1 + Math.exp((0.5 - Math.pow(behaviourFrequency, BF_TS)) * SS)) -
                        (1.0 / (1 + Math.exp(0.5 * SS)))) /
                        ((1.0 / (1 + Math.exp(-0.5 * SS))) -
                                (1.0 / (1 + Math.exp(0.5 * SS))))) *
                        DP;
        return similarity;
    }

    public void setAGC_REM(double AGC_REM) {
        this.AGC_REM = AGC_REM;
    }

    public void setAGC_EVENT(double AGC_EVENT) {
        this.AGC_EVENT = AGC_EVENT;
    }

    public void setAGC_BEH(double AGC_BEH) {
        this.AGC_BEH = AGC_BEH;
    }

    public void setWBF_AGBEH(double WBF_AGBEH) {
        this.WBF_AGBEH = WBF_AGBEH;
    }

    public void setWBF_AT(double WBF_AT) {
        this.WBF_AT = WBF_AT;
    }

    public void setWCI_REM(double WCI_REM) {
        this.WCI_REM = WCI_REM;
    }

    public void setWCI_EVENT(double WCI_EVENT) {
        this.WCI_EVENT = WCI_EVENT;
    }

    public void setWH_AT(double WH_AT) {
        this.WH_AT = WH_AT;
    }

    public void setSDP_REM(double SDP_REM) {
        this.SDP_REM = SDP_REM;
    }

    public void setHDP(double HDP) {
        this.HDP = HDP;
    }

    public void setADP(double ADP) {
        this.ADP = ADP;
    }

    public void setCAT(double CAT) {
        this.CAT = CAT;
    }

    public void setBF_TS(double BF_TS) {
        this.BF_TS = BF_TS;
    }

    public void setSS(double SS) {
        this.SS = SS;
    }

    public void setDP(double DP) {
        this.DP = DP;
    }

    public double getBehaviourFrequency() {
        return behaviourFrequency;
    }

    public void setBehaviourFrequency(double behaviourFrequency) {
        this.behaviourFrequency = behaviourFrequency;
    }

    public double getSalienceReminder() {
        return salienceReminder;
    }

    public void setSalienceReminder(double salienceReminder) {
        this.salienceReminder = salienceReminder;
    }

    public double getAccessability() {
        return accessability;
    }

    public void setAccessability(double accessability) {
        this.accessability = accessability;
    }

    public double getCommitmentIntensity() {
        return commitmentIntensity;
    }

    public void setCommitmentIntensity(double commitmentIntensity) {
        this.commitmentIntensity = commitmentIntensity;
    }

    public double getHabitStrength() {
        return habitStrength;
    }

    public void setHabitStrength(double habitStrength) {
        this.habitStrength = habitStrength;
    }

    private void initialBehaviourList() {
        behaviourWindow = new ArrayList<>();
        double currentFrequency = 0;

        int trues = 0;
        for (int i = 0; i < windowSize; i++) {
            if (currentFrequency < getBehaviourFrequency()) {
                behaviourWindow.add(true);
                trues++;
            } else {
                behaviourWindow.add(false);
            }
            currentFrequency = (double) trues / (double) (i + 1);
        }
    }

    private void drawCharts() {
        // show behaviour chart
        SwingUtilities.invokeLater(() -> {
            BehaviourChart example = new BehaviourChart("Behaviour");
            example.showChart(behaviours, reminders);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            AccessibilityThresholdChart example = new AccessibilityThresholdChart("Accessibility / Threshold");
            example.showChart(accessibilities, thresholds, behaviourFrequencies, habitStrengths);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            ThresholdChart example = new ThresholdChart("Threshold");
            example.showChart(thresholds, positives, negatives);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
