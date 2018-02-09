package tez2.environment.simulator;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import power2dm.model.TaskDifficulty;
import tez2.algorithm.ActionRestrictingState;
import tez2.domain.TerminalState;
import tez2.domain.action.Jitai1Action;
import tez2.domain.action.Jitai2Action;
import tez2.domain.action.Jitai3Action;
import tez2.environment.SelfManagementEnvironment;
import tez2.environment.simulator.habit.HabitGainRatio;
import tez2.environment.simulator.habit.visualization.AccessibilityThresholdChart;
import tez2.persona.ActionPlan;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static tez2.domain.DomainConfig.*;
import static tez2.domain.DomainConfig.ATT_DAY_TYPE;
import static tez2.domain.DomainConfig.ATT_PART_OF_DAY;

public class JitaiSelectionEnvironment extends SelfManagementEnvironment {
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
    private int habitGainOffset;
    private TaskDifficulty taskDifficulty;

    // environment related parameters
    private int stepCount = 0;
    private int reminderCount = 0;
    private GroundedAction lastAction;
    private SimulatedWorld simulatedWorld;

    // visualization data
    private List<Integer> behaviors = new ArrayList<>();
    private List<Integer> remembers = new ArrayList<>();
    private List<Double> accessibilities = new ArrayList<>();
    private List<Double> thresholds = new ArrayList<>();
    private List<Double> habitStrengths = new ArrayList<>();
    private List<Double> behaviorFrequencies = new ArrayList<>();
    private List<Integer> jitais = new ArrayList<>();

    public JitaiSelectionEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, String configFilePath) {
        super(domain, rf, tf, stateChangeFrequency);

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(configFilePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file at: " + configFilePath);
        }

        String[] jitaiTypes = prop.getProperty("jitai_types").split(",");
        LinkedHashMap jitaiTypeMap = new LinkedHashMap();
        for (int i = 0; i < jitaiTypes.length; i++) {
            jitaiTypeMap.put(i + 1, Integer.parseInt(jitaiTypes[i]));
        }

        double behaviorFrequency = Double.parseDouble(prop.getProperty("behavior_frequency"));
        double commitmentIntensity = Double.parseDouble(prop.getProperty("commitment_intensity"));

        setInitialValues(behaviorFrequency, commitmentIntensity, jitaiTypeMap);
    }

    private void setInitialValues(double initialBehaviorFrequency, double commitmentIntensity, LinkedHashMap<Integer, Integer> jitaiGroups) {
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
        accessibility = habitStrength = commitmentIntensity;
        initiateBehaviorList();
    }

    public void setSimulatedWorld(SimulatedWorld simulatedWorld) {
        this.simulatedWorld = simulatedWorld;
    }

    private void simulateStep(GroundedAction action) {
        ActionRestrictingState currentState = (ActionRestrictingState) getCurrentObservation();
        int jitaiGroup = currentState.getExpectedJitaiType() == ActionPlan.JitaiNature.REMINDER ? 1 : 2;
        int jitaiType = 0;
        if(action.action instanceof Jitai1Action) {
            jitaiType = 1;
        } else if(action.action instanceof Jitai2Action) {
            jitaiType = 2;
        } else if(action.action instanceof Jitai3Action) {
            jitaiType = 3;
        }

        simulateStep(jitaiGroup, jitaiType);
    }

    private void simulateStep(int jitaiGroup, int jitaiType) {
        selectedJitaiGroup = jitaiGroup;
        selectedJitaiType = jitaiType;

        simulateBehavior();
        updateAccessibility();
        updateHabitStrength();
        updateSalience();
    }

    public void simulateBehavior() {
        System.out.println("VALUES FOR THRESHOLD");
        System.out.println("Habit strenght: " + habitStrength);
        System.out.println("Behavior frequency: " + behaviorFrequency);
        System.out.println("");
        //double threshold = CAT - (CAT *  WH_AT * habitStrength) + (1.0 - CAT) * WBF_AT * behaviorFrequency * (1.0 - DRH_AT * habitStrength);
        double threshold = Math.abs(CAT - (CAT *  WH_AT * habitStrength) + (1.0 - CAT) * WBF_AT * behaviorFrequency * (1.0 - DRH_AT * habitStrength) - (1-behaviorFrequency)*normalizeCI());

        boolean behaviorRemembered = accessibility >= threshold;
        behaviorPerformed = behaviorRemembered;
        behaviorPerformed = false;
        if (behaviorRemembered) {
            behaviorPerformed = true;
        }

        if(behaviorRemembered) {
            double habitGain = HabitGainRatio.get(taskDifficulty, habitGainOffset++);
            System.out.println("Habit gain ratio:" + habitGain);
            //if ((new Random().nextInt(100) % 100 < (CI * 100))) {
            if ((new Random().nextInt(100) % 100 < habitGain)) {
                behaviorPerformed = true;
            } else {
                behaviorPerformed = false;
            }
        } else {
            behaviorPerformed = false;
        }

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

    public boolean willRemember() {
        //double threshold = CAT - (CAT * WH_AT * habitStrength) + (1.0 - CAT) * WBF_AT * behaviorFrequency * (1.0 - DRH_AT * habitStrength);
        double threshold = Math.abs(CAT - (CAT *  WH_AT * habitStrength) + (1.0 - CAT) * WBF_AT * behaviorFrequency * (1.0 - DRH_AT * habitStrength) - (1-behaviorFrequency)*normalizeCI());
        boolean behaviorRemembered = accessibility >= threshold;
        return behaviorRemembered;
    }

    private void updateAccessibility() {
        double accDecay = accessibility * ADP;
        double accGainBeh = 0;
        double accGainEvent = 0;
        if (day == 0 && dailyStep == 0) {
            accGainEvent = AGC_EVENT * (1.0 - AGC_EVENT) * WCI_EVENT * CI;
        }
        if (behaviorPerformed) {
            //accGainBeh = behaviorFrequency * WBF_AGBEH
            accGainBeh = behaviorFrequency * WBF_AGBEH + (1 - behaviorFrequency) * WBF_AGBEH;
        }
        double accGainRem = 0;
        if (selectedJitaiType != 0) {
            accGainRem = (AGC_REM + (1.0 - AGC_REM) * WCI_REM * CI) * salienceReminders.get(selectedJitaiType) + (1-behaviorFrequency) * CI;
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

        behaviorFrequency = Math.max(0, (double) performedBehaviour / (double) (windowSize * 3));
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

    private double normalizeCI() {
        return CI * 0.4 + 0.3;
    }

    /***************************************
     ***** Environment-related methods *****
     ***************************************/

    @Override
    public State getNextState(GroundedAction action) {
        lastAction = action;
        State nextState;

        if (stepCount < 6) {
            if(action.action instanceof Jitai1Action || action.action instanceof  Jitai3Action || action.action instanceof Jitai3Action) {
                reminderCount++;
            }

            // advance the environment
            // only simulate the habit model when the environment is in a reminder state
            ActionRestrictingState currentState = (ActionRestrictingState) getCurrentObservation();
            if(currentState.getExpectedJitaiType() == ActionPlan.JitaiNature.REMINDER) {
                simulateStep(action);
                reminderCount = 0; //TODO update the effect of reminders on accessibility based on reminder count
            }

            SimulatedWorld.DynamicSimulatedWorldContext simulatedWorldContext = this.simulatedWorld.getLastContextForJitai(stepCount);
            ActionPlan.JitaiNature expectedJitai = simulatedWorldContext.getExpectedJitaiNature();

            nextState = new ActionRestrictingState(expectedJitai);
            nextState.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));
            ObjectInstance o = nextState.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_HABIT_STRENGTH, habitStrength);
            o.setValue(ATT_BEHAVIOR_FREQUENCY, behaviorFrequency);
            o.setValue(ATT_REMEMBER_BEHAVIOR, willRemember());
            o.setValue(ATT_DAY_TYPE, simulatedWorldContext.getCurrentDayType());
            o.setValue(ATT_PART_OF_DAY, simulatedWorldContext.getCurrentDayPart());

        } else {
            nextState = new TerminalState();
        }
        stepCount++;
        return nextState;
    }

    @Override
    public State getStateFromCurrentContext() {
        SimulatedWorld.DynamicSimulatedWorldContext simulatedWorldContext = this.simulatedWorld.getContext();
        ActionPlan.JitaiNature expectedJitai = simulatedWorldContext.getExpectedJitaiNature();

        ActionRestrictingState s = new ActionRestrictingState(expectedJitai);

        s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_HABIT_STRENGTH, habitStrength);
        o.setValue(ATT_BEHAVIOR_FREQUENCY, behaviorFrequency);
        o.setValue(ATT_REMEMBER_BEHAVIOR, willRemember());
        o.setValue(ATT_DAY_TYPE, simulatedWorldContext.getCurrentDayType());
        o.setValue(ATT_PART_OF_DAY, simulatedWorldContext.getCurrentDayPart());

        return s;
    }

    @Override
    public void resetEnvironment() {
        stepCount = 0;
        super.resetEnvironment();
    }

    public void initEpisode() {
        State s = getStateFromCurrentContext();
        setCurStateTo(s);
    }

    public GroundedAction getLastAction() {
        return lastAction;
    }
}
