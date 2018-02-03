package tez2.algorithm;

import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.statehashing.HashableState;

/**
 * Created by suat on 06-May-17.
 */
public class SelfManagementEligibilityTrace extends SarsaLam.EligibilityTrace{
    private boolean isUseful;

    /**
     * Creates a new eligibility trace to track for an episode.
     *
     * @param sh          the state of the trace
     * @param q           the q-value (containing the action reference) of the trace
     * @param elgigbility the eligibility value
     */
    public SelfManagementEligibilityTrace(HashableState sh, QValue q, double elgigbility, boolean isUseful) {
        super(sh, q, elgigbility);
        this.isUseful = isUseful;
    }

    public boolean isUseful() {
        return isUseful;
    }
}
