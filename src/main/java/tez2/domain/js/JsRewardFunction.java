package tez2.domain.js;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import tez2.domain.SelfManagementRewardFunction;
import tez2.environment.simulator.JsEnvironment;

import static tez2.domain.DomainConfig.*;

public class JsRewardFunction extends SelfManagementRewardFunction {

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
        ObjectInstance stateInstance = s.getObject(CLASS_STATE);
        int rememberedBehavior = stateInstance.getIntValForAttribute(ATT_REMEMBER_BEHAVIOR);
        boolean performedBehavior = ((JsEnvironment) environment).isBehaviorPerformed();

        if (!a.action.getName().equals(ACTION_NO_ACTION)) {
            //if(rememberedBehavior == 0) {
            if(performedBehavior == false) {
                boolean reactedToJitai = ((JsEnvironment) environment).isReactedToJitai();
                if(reactedToJitai) {
                    return 10;
                } else {
                    return 5;
                }

            } else {
                return -1;
            }

        } else {
            //if(rememberedBehavior == 0) {
            if(performedBehavior == false) {
                return -10;
            } else {
                return 0;
            }
        }
    }
}
