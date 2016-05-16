package power2dm.algorithm;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.oomdp.statehashing.HashableState;

import java.util.Map;

/**
 * Created by suat on 02-May-16.
 */
public interface LearningProvider {
    Map<HashableState, QLearningStateNode> getAllQValues();

    Policy getPolicy();

    void setPolicySolver();
}
