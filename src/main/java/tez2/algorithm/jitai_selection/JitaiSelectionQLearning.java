package tez2.algorithm.jitai_selection;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.EnvironmentOptionOutcome;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import tez2.domain.ExtendedEnvironmentOutcome;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 07-Feb-18.
 */
public class JitaiSelectionQLearning extends QLearning {
    public JitaiSelectionQLearning(Domain domain, double gamma, HashableStateFactory hashingFactory, ValueFunctionInitialization qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize) {
        super(domain, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize);
    }

    public void executeLearningStep(Environment env, HashableState curState, SelfManagementEpisodeAnalysis ea) {
        GroundedAction action = (GroundedAction) learningPolicy.getAction(curState.s);
        QValue curQ = this.getQ(curState, action);
        List<QValue> currentQVals = copyCurrentQVals(this.qIndex.get(curState).qEntry);

        EnvironmentOutcome eo = action.executeIn(env);


        HashableState nextState = this.stateHash(eo.op);
        double maxQ = 0.;

        if (!eo.terminated) {
            maxQ = this.getMaxQ(nextState);
        }

        //manage option specifics
        double r = eo.r;
        double discount = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).discount : this.gamma;
        int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).numSteps : 1;
        eStepCounter += stepInc;

        if (action.action.isPrimitive() || !this.shouldAnnotateOptions) {
            ExtendedEnvironmentOutcome eeo = (ExtendedEnvironmentOutcome) eo;
            ea.recordTransitionTo(action, nextState.s, r, currentQVals, eeo.getUserContext(), eeo.getUserReaction());
        } else {
            ea.appendAndMergeEpisodeAnalysis(((Option) action.action).getLastExecutionResults());
        }

        double oldQ = curQ.q;

        //update Q-value
        curQ.q = curQ.q + this.learningRate.pollLearningRate(this.totalNumberOfSteps, curState.s, action) * (r + (discount * maxQ) - curQ.q);

        double deltaQ = Math.abs(oldQ - curQ.q);
        if (deltaQ > maxQChangeInLastEpisode) {
            maxQChangeInLastEpisode = deltaQ;
        }

        //move on polling environment for its current state in case it changed during processing
        curState = this.stateHash(env.getCurrentObservation());
        this.totalNumberOfSteps++;
    }

    protected List<QValue> copyCurrentQVals(List<QValue> qValues) {
        List<QValue> copyList = new ArrayList<>();
        for (QValue qv : qValues) {
            copyList.add(new QValue(qv));
        }
        return copyList;
    }
}
