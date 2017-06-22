package tez.experiment.debug;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import org.apache.log4j.Logger;
import tez.domain.action.SelfManagementAction;
import tez.environment.context.*;
import tez.util.LogUtil;

import java.text.DecimalFormat;
import java.util.List;

import static tez.domain.SelfManagementDomainGenerator.*;
import static tez.domain.SelfManagementDomainGenerator.ACTION_INT_DELIVERY;
import static tez.domain.SelfManagementDomainGenerator.ATT_EMOTIONAL_STATUS;

/**
 * Created by suat on 21-Jun-17.
 */
public class StepPrinter {
    private static final Logger log = Logger.getLogger(StepPrinter.class);
    private static final DecimalFormat qValPrecision = new DecimalFormat("#0.0000");

    public static void printStep(GroundedAction usingAction, State state, double r, List<QValue> qValues, Context context, boolean userReaction, String interference, SelfManagementAction.SelectedBy selectedBy) {
        // Context details from the state object
        ObjectInstance o = state.getObjectsOfClass(CLASS_STATE).get(0);
        Location location = Location.values()[o.getIntValForAttribute(ATT_LOCATION)];
        DayType dayType = DayType.values()[o.getIntValForAttribute(ATT_DAY_TYPE)];

        // Context details from the context object
        Location location_c = context.getLocation();
        PhoneUsage phoneUsage_c = context.getPhoneUsage();
        EmotionalStatus emotionalStatus_c = context.getEmotionalStatus();
        PhysicalActivity physicalActivity_c = context.getPhysicalActivity();
        StateOfMind stateOfMind_c = context.getStateOfMind();

        StringBuilder sb = new StringBuilder();
        String activityTime = o.getStringValForAttribute(ATT_ACTIVITY_TIME);
        PhysicalActivity activity = PhysicalActivity.values()[o.getIntValForAttribute(ATT_ACTIVITY)];
        PhoneUsage phoneUsage = PhoneUsage.values()[o.getIntValForAttribute(ATT_PHONE_USAGE)];
        StateOfMind stateOfMind = StateOfMind.values()[o.getIntValForAttribute(ATT_STATE_OF_MIND)];
        EmotionalStatus emotionalStatus = EmotionalStatus.values()[o.getIntValForAttribute(ATT_EMOTIONAL_STATUS)];
        sb.append("(" + activityTime + ", " + dayType + ", " + location + ", " + activity + ", " + phoneUsage + ", " + stateOfMind + ", " + emotionalStatus + ") ");
        sb.append("(" + location_c + ", " + physicalActivity_c + ", " + phoneUsage_c + ", " + stateOfMind_c + ", " + emotionalStatus_c + ") ");
        int actionNo;
        for (QValue qv : qValues) {
            actionNo = qv.a.actionName().equals(ACTION_INT_DELIVERY) ? 1 : 0;

            sb.append(actionNo + ": " + qValPrecision.format(qv.q) + ", ");
        }
        actionNo = usingAction.actionName().equals(ACTION_INT_DELIVERY) ? 1 : 0;
        //System.out.print(") A:" + actionNo + ", R:" + ea.rewardSequence.get(j));
        sb.append(") A:" + actionNo + ", R:" + r);
        sb.append(userReaction == true ? " (X) Inter: " + interference + " Selected by:" + selectedBy : "" + " Inter: " + interference + " Selected by: " + selectedBy);

        log.info(context.getDeviceIdentifier() + sb.toString());
    }
}
