package tez.environment.simulator.habit;

import tez.environment.simulator.habit.visualization.AccessibilityThresholdChart;
import tez.environment.simulator.habit.visualization.h3.BehaviorJitaiChart;

import javax.swing.*;
import java.util.*;

public class HabitSimulator3 {
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


    private double CI;
    private int day;
    private int dailyStep;
    private LinkedHashMap<Integer, Integer> jitaiGroups;
    private int selectedJitaiGroup;
    private int selectedJitaiType;
    private double accessibility;
    private double habitStrength;
    private double behaviorFrequency;
    private boolean behaviorPerformed;
    private int windowSize;
    private List<Boolean> behaviourWindow;
    private Map<Integer, Double> salienceReminders = new HashMap<>();

    // visualization data
    private List<Integer> behaviors = new ArrayList<>();
    private List<Integer> remembers = new ArrayList<>();
    private List<Double> accessibilities = new ArrayList<>();
    private List<Double> thresholds = new ArrayList<>();
    private List<Double> habitStrengths = new ArrayList<>();
    private List<Double> behaviorFrequencies = new ArrayList<>();
    private List<Integer> jitais = new ArrayList<>();

    public HabitSimulator3(double initialBehaviorFrequency, double commitmentIntensity, LinkedHashMap<Integer, Integer> jitaiGroups) {
        ADP = 0.641;
        //ADP = 0.3;
        AGC_EVENT = 0.111;
        AGC_REM = 0.005;
        //AGC_REM = 0.037;
        //AGC_REM = 0.1;
        CAT = 0.749;
        DP = 0.886;
        DRH_AT = 0.975;
        HDP = 0.08;
        SDP_REM = 0.094;
        SS = 18.484;
        TS = 0.214;
        TP = TS;
        WBF_AGBEH = 0.221;
        //WBF_AGBEH = 0.6;
        WBF_AT = 0.355;
        WCI_EVENT = 0.997;
        WCI_REM = 0.083;
        WH_AT = 1.0;

        // initial values
        behaviorFrequency = initialBehaviorFrequency;
        CI = commitmentIntensity;
        this.jitaiGroups = jitaiGroups;
        int actionTypeNumber = 0;
        for (int jitaiNum : jitaiGroups.values()) {
            actionTypeNumber += jitaiNum;
        }
        for (int i = 1; i <= actionTypeNumber; i++) {
            salienceReminders.put(i, 1.0);
        }
        windowSize = 15;
        accessibility = commitmentIntensity;
        habitStrength = initialBehaviorFrequency;
        initiateBehaviorList();
    }

    public static void main(String[] args) {
        LinkedHashMap<Integer, Integer> jitaiGroups = new LinkedHashMap<>();
        jitaiGroups.put(1, 2);
        jitaiGroups.put(2, 1);
        HabitSimulator3 hs = new HabitSimulator3(0.7, 0.7, jitaiGroups);
        hs.simulateScenario();
        hs.drawCharts();

        hs = new HabitSimulator3(0.3, 0.3, jitaiGroups);
        hs.simulateScenario();
        hs.drawCharts();
    }

    private void simulateScenario() {
        for (day = 0; day < 50; day++) {
            System.out.println("************* DAY:" + day);
            for (dailyStep = 0; dailyStep < 3; dailyStep++) {
                System.out.println("**************** STEP: " + dailyStep);
                //if(dailyStep % 2) {
                //simulateStep((dailyStep % 2) + 1, (dailyStep % 2) == 0 ? 1 : 3);
                simulateStep(1, 1);
            }
        }
    }

    private void simulateStep(int jitaiGroup, int jitaiType) {
        selectedJitaiGroup = jitaiGroup;
        selectedJitaiType = jitaiType;

        //if(dailyStep % 2 == 0) {
            simulateBehavior();
            updateAccessibility();
            updateHabitStrength();
        //}
        updateSalience();
    }

    private void simulateBehavior() {
        System.out.println("VALUES FOR THRESHOLD");
        System.out.println("Habit strenght: " + habitStrength);
        System.out.println("Behavior frequency: " + behaviorFrequency);
        System.out.println("");
        double threshold = CAT - (CAT *  WH_AT * habitStrength) + (1.0 - CAT) * WBF_AT * behaviorFrequency * (1.0 - DRH_AT * habitStrength);

        boolean behaviorRemembered = accessibility >= threshold;
        behaviorPerformed = behaviorRemembered;
        behaviorPerformed = false;
        if (behaviorRemembered) {
            behaviorPerformed = true;
        }
//        if(behaviorRemembered) {
            if ((new Random().nextInt(100) % 100 < (CI * 100))) {
                behaviorPerformed = true;
            } else {
                behaviorPerformed = false;
            }
//        } else {
//            behaviorPerformed = false;
//        }

//        if(day == 5) behaviorPerformed = true;
//        if(day == 10) behaviorPerformed = false;
//        if(day == 15) behaviorPerformed = true;
//        if(day == 20) behaviorPerformed = false;
//        if(day >= 10 && day < 17)
//            behaviorPerformed = false;
//        if(day >= 30 && day < 35)
//            behaviorPerformed = true;

        updateBehaviourFrequency(behaviorPerformed);

        System.out.println("Behavior frequency: " + behaviorFrequency);
        System.out.println("Behavior: " + behaviorPerformed);
        System.out.println("Threshold: " + threshold);
        System.out.println("Accessibility: " + accessibility);
        //System.out.println("Salience remainder: " + salienceReminder);
        System.out.println();

        if (behaviors.size() == day) {
            behaviors.add(0);
            remembers.add(0);
        }

        remembers.set(day, remembers.get(day) + (behaviorRemembered ? 1 : 0));
        behaviors.set(day, behaviors.get(day) + (behaviorPerformed ? 1 : 0));
        accessibilities.add(accessibility);
        thresholds.add(threshold);
        habitStrengths.add(habitStrength);
        behaviorFrequencies.add(behaviorFrequency);
        jitais.add(selectedJitaiType);
    }

    private void updateAccessibility() {
        double accDecay  = accessibility * ADP;
        double accGainBeh = 0;
        double accGainEvent = 0;
        if(day == 0 && dailyStep == 0) {
            accGainEvent = AGC_EVENT * (1.0 - AGC_EVENT) * WCI_EVENT * CI;
        }
        if (behaviorPerformed) {
            accGainBeh = behaviorFrequency * WBF_AGBEH;
        }
        double accGainRem = 0;
        if(selectedJitaiType != 0) {
            accGainRem = (AGC_REM + (1.0 - AGC_REM) * WCI_REM * CI) * salienceReminders.get(selectedJitaiType) ;
        }
        accessibility = Math.max(0, Math.min(1, accessibility - accDecay + accGainEvent + accGainBeh + accGainRem));

        System.out.println("accDecay: " + accDecay);
        System.out.println("accGainRem: " + accGainRem);
        System.out.println("accGainBeh:" + accGainBeh);
        System.out.println("salience: " + salienceReminders.get(selectedJitaiType));
        System.out.println("accessibility: " + accessibility);
    }

    public void updateHabitStrength() {
        double habitDecay;
        if (!behaviorPerformed) {
            habitDecay = habitStrength * HDP;
        } else {
            habitDecay = 0;
        }

        double habitGainBF;
        if (behaviorPerformed) {
            double habitGainExe = (habitStrength * (1.0 - behaviorFrequency) + behaviorFrequency) * HDP;
            habitGainBF = habitGainExe;
        } else {
            habitGainBF = 0;
        }

        habitStrength = Math.max(0, Math.min(1, habitStrength - habitDecay + habitGainBF));
    }

    /**
     * Updates saliences of jitais that are included in the group of the selected jitai
     */
    private void updateSalience() {
        for (int gi = 1, actionOffset = 1; gi <= jitaiGroups.size(); gi++) {
            if (selectedJitaiGroup == gi) {
                for (int i = 0; i < jitaiGroups.get(gi); i++) {
                    double salience = salienceReminders.get(actionOffset + i);
                    if (selectedJitaiType == (actionOffset + i)) {
                        double salienceDecay = salience * SDP_REM;
                        salience -= salienceDecay;
                    } else {
                        double salienceIncrease = salience / SDP_REM;
                        salience += salienceIncrease;
                    }
                    salience = Math.max(0, Math.min(1, salience));
                    salienceReminders.put(actionOffset + i, salience);
                }
            }
            actionOffset += jitaiGroups.get(gi);
        }
    }

    private void updateBehaviourFrequency(boolean behaviour) {
        behaviourWindow.remove(0);
        behaviourWindow.add(behaviour);

        int performedBehaviour = 0;
        for (int i = 0; i < windowSize * 3; i++) {
            performedBehaviour += behaviourWindow.get(i) ? 1 : 0;
        }

        behaviorFrequency = Math.max(0, (double) performedBehaviour / (double) (windowSize*3));
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
//        SwingUtilities.invokeLater(() -> {
//            BehaviorJitaiChart example = new BehaviorJitaiChart("Behaviour");
//            example.showChart(behaviors, remembers);
//            example.setSize(800, 400);
//            example.setLocationRelativeTo(null);
//            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//            example.setVisible(true);
//        });
        SwingUtilities.invokeLater(() -> {
            AccessibilityThresholdChart example = new AccessibilityThresholdChart("Acc / Thresh");
            example.showChart(accessibilities, thresholds, behaviorFrequencies, habitStrengths);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }

    private void initiateBehaviorList() {
        behaviourWindow = new ArrayList<>();
        double currentFrequency = 0;

        int trues = 0;
        for (int i = 0; i < windowSize * 3 /* because there are 3 actions in the action plan in a day*/; i++) {
            if (currentFrequency < behaviorFrequency) {
                behaviourWindow.add(true);
                trues++;
            } else {
                behaviourWindow.add(false);
            }
            currentFrequency = (double) trues / (double) (i + 1);
        }
    }
}
