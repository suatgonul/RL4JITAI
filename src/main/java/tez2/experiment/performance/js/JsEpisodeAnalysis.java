package tez2.experiment.performance.js;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez2.domain.TerminalState;
import tez2.domain.js.JsEnvironmentOutcome;
import tez2.environment.context.DayPart;
import tez2.environment.context.DayType;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static tez2.domain.DomainConfig.*;

public class JsEpisodeAnalysis extends SelfManagementEpisodeAnalysis {

    private static DecimalFormat qValPrecision = new DecimalFormat("#0.0000");
    public List<List<QValue>> qValuesForStates;
    private JsEnvironmentOutcome eo;

    public JsEpisodeAnalysis(State initialState) {
        super(initialState);
        qValuesForStates = new ArrayList<>();
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, JsEnvironmentOutcome eo) {
        this.eo = eo;
        this.qValuesForStates.add(qValues);
        super.recordTransitionTo(usingAction, nextState, r);
    }

    public void printEpisodeAnalysis() {
        printState(stateSequence.get(0), 0);
        for (int i = 0; i < rewardSequence.size(); i++) {
            printQValues(qValuesForStates.get(i));
            printActionAndReward(rewardSequence.get(i), actionSequence.get(i));
            printState(stateSequence.get(i + 1), i + 1);
        }
    }

    private void printState(State state, int index) {
        StringBuilder sb = new StringBuilder("STATE-").append(index).append("\n");
        if (state instanceof TerminalState) {
            sb.append("Terminal\n");
            System.out.println(sb.toString());
            return;
        }

        ObjectInstance o = state.getObjectsOfClass(CLASS_STATE).get(0);
        int habitStrength = o.getIntValForAttribute(ATT_HABIT_STRENGTH);
        sb.append("Habit strength: ").append(habitStrength).append(", ");
        int behaviorFrequency = o.getIntValForAttribute(ATT_BEHAVIOR_FREQUENCY);
        sb.append("Behavior frequency: ").append(behaviorFrequency).append(", ");
        int behaviorRemembered = o.getIntValForAttribute(ATT_REMEMBER_BEHAVIOR);
        sb.append("Behavior Rem: ").append(behaviorRemembered).append(", ");
        DayType dayType = DayType.values()[o.getIntValForAttribute(ATT_DAY_TYPE)];
        sb.append("Day type: ").append(dayType).append(", ");
        DayPart dayPart = DayPart.values()[o.getIntValForAttribute(ATT_PART_OF_DAY)];
        sb.append("Day part: ").append(dayPart).append("\n");

        System.out.println(sb.toString());
    }

    private void printQValues(List<QValue> qValues) {
        StringBuilder sb = new StringBuilder("QVALUES\n");

        for (QValue qv : qValues) {
            sb.append(qv.a.actionName()).append(": ").append(qValPrecision.format(qv.q)).append(", ");
        }
        sb.append("\n");
        System.out.println(sb.toString());
    }

    private void printActionAndReward(double reward, GroundedAction action) {
        StringBuilder sb = new StringBuilder("ACTION, REWARD\n");
        sb.append("Action: ").append(action.actionName()).append(", Reward: ").append(reward);
        sb.append("\n");
        System.out.println(sb.toString());
    }
}
