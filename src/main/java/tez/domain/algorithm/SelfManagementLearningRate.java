package tez.domain.algorithm;

import burlap.behavior.learningrate.ConstantLR;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import tez.domain.SelfManagementDomainGenerator;

/**
 * Created by suatgonul on 5/1/2017.
 */
public class SelfManagementLearningRate extends ConstantLR{
    @Override
    public double pollLearningRate(int agentTime, State s, AbstractGroundedAction ga) {
        if(ga.actionName().equals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
            return 1;
        } else {
            return super.pollLearningRate(agentTime, s, ga);
        }
    }
}
