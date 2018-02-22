package tez2.experiment.performance;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import org.joda.time.LocalTime;
import tez2.environment.context.Context;
import tez2.experiment.performance.js.JsEpisodeAnalysis;

import java.util.ArrayList;
import java.util.List;

import static tez2.domain.DomainConfig.ACTION_SEND_JITAI;

public class OmiEpisodeAnalysis extends SelfManagementEpisodeAnalysis {
    public List<List<QValue>> qValuesForStates;
    public List<Context> userContexts;
    public List<LocalTime> stateTimes;
    public List<Boolean> userReactions;
    public int phoneCheckNumber;
    public int actionDeliveredDuringPhoneCheck;
    private JsEpisodeAnalysis jsEpisodeAnalysis;

    public OmiEpisodeAnalysis(State initialState) {
        super(initialState);
        qValuesForStates = new ArrayList<>();
        userContexts = new ArrayList<>();
        stateTimes = new ArrayList<>();
        userReactions = new ArrayList<>();
    }

    public void setTrialNo(int trialNo) {
        this.trialNo = trialNo;
        jsEpisodeAnalysis.trialNo = trialNo;
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues, LocalTime stateTime, Context userContext, boolean userReaction) {
        qValuesForStates.add(qValues);
        userContexts.add(userContext);
        stateTimes.add(stateTime);
        userReactions.add(userReaction);
        if (userReaction == true) {
            phoneCheckNumber++;
            if (usingAction.actionName().equals(ACTION_SEND_JITAI)) {
                actionDeliveredDuringPhoneCheck++;
            }
        }

        super.recordTransitionTo(usingAction, nextState, r);
    }

    public void setJsEpisodeAnalysis(JsEpisodeAnalysis jsEpisodeAnalysis) {
        this.jsEpisodeAnalysis = jsEpisodeAnalysis;
    }

    public JsEpisodeAnalysis getJsEpisodeAnalysis() {
        return this.jsEpisodeAnalysis;
    }
}
