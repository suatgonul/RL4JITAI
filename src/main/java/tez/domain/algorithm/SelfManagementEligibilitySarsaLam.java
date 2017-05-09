package tez.domain.algorithm;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.EnvironmentOptionOutcome;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleGroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import tez.domain.ExtendedEnvironmentOutcome;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.SelfManagementRewardFunction;
import tez.experiment.performance.SelfManagementEligibilityEpisodeAnalysis;
import tez.model.Constants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by suatgonul on 5/1/2017.
 */
public class SelfManagementEligibilitySarsaLam extends SarsaLam {
    private List<State> deliveredInterventions;

    public SelfManagementEligibilitySarsaLam(Domain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize, double lambda) {
        super(domain, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize, lambda);
        if (learningPolicy instanceof SolverDerivedPolicy) {
            ((SolverDerivedPolicy) learningPolicy).setSolver(this);
        }
     }

    @Override
    public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {
        deliveredInterventions = new ArrayList<>();

        State initialState = env.getCurrentObservation();

        SelfManagementEligibilityEpisodeAnalysis ea = new SelfManagementEligibilityEpisodeAnalysis(initialState);
        maxQChangeInLastEpisode = 0.;

        HashableState curState = this.stateHash(initialState);
        eStepCounter = 0;
        LinkedList<SelfManagementEligibilityTrace> traces = new LinkedList<>();

        GroundedAction action = (GroundedAction) learningPolicy.getAction(curState.s);
        QValue curQ = this.getQ(curState, action);


        while (!env.isInTerminalState() && (eStepCounter < maxSteps || maxSteps == -1)) {

            if (action.actionName().equals(Constants.ACTION_INT_DELIVERY)) {
                deliveredInterventions.add(curState);
            }

            EnvironmentOutcome eo = action.executeIn(env);
            ExtendedEnvironmentOutcome eeo = (ExtendedEnvironmentOutcome) eo;

            HashableState nextState = this.stateHash(eo.op);
            GroundedAction nextAction = (GroundedAction) learningPolicy.getAction(nextState.s);
            QValue nextQ = this.getQ(nextState, nextAction);
            double nextQV = nextQ.q;

            if (env.isInTerminalState()) {
                nextQV = 0.;
            }


            //manage option specifics
            double r = eo.r;
            double discount = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).discount : this.gamma;
            int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).numSteps : 1;
            eStepCounter += stepInc;

            //delta
            double delta = r + (discount * nextQV) - curQ.q;

            //update all states visited in this episode so far
            boolean foundCurrentQTrace = false;
            for (SelfManagementEligibilityTrace et : traces) {

                if (et.sh.equals(curState)) {
                    if (et.q.a.equals(action)) {
                        foundCurrentQTrace = true;
                        //et.eligibility = 1.; //replacing traces
                        et.eligibility = et.eligibility + 1;
                        System.out.println("Encountered previous state: " + curState);
                    } else {
                        et.eligibility = 0.; //replacing traces
                    }
                }

                double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, et.sh.s, et.q.a);

                // if the user reaction is positive at the current state and if the current trace includes an
                // intervention delivery action do not positively reward a previous trace if it did not provide a
                // positive result.
                if (eeo.getUserReaction() && et.q.a.actionName().equals(Constants.ACTION_INT_DELIVERY)) {
                    if(!et.isUseful()) {
                        double tempDelta = SelfManagementRewardFunction.getRewardNonReactionToIntervention() + (discount * nextQV) - curQ.q;
                        et.q.q = et.q.q + (learningRate * et.eligibility * tempDelta);
                        et.eligibility = et.eligibility * lambda * discount;

                    } else {
                        et.q.q = et.q.q + (learningRate * et.eligibility * delta);
                        et.eligibility = et.eligibility * lambda * discount;
                    }

                    //continue;
                } else {
                    // if the user reaction is negative then apply regular eligibility traces
                    et.q.q = et.q.q + (learningRate * et.eligibility * delta);
                    et.eligibility = et.eligibility * lambda * discount;
                }

                double deltaQ = Math.abs(et.initialQ - et.q.q);
                if (deltaQ > maxQChangeInLastEpisode) {
                    maxQChangeInLastEpisode = deltaQ;
                }

            }

            String interference = "N";
            if (!foundCurrentQTrace) {
                //then update and add it
                double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, curQ.s, curQ.a);

                SelfManagementEligibilityTrace et;

                if(eeo.getUserReaction()) {
                    if(action.actionName().equals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
                        curQ.q = curQ.q + (learningRate * delta);
                        et = new SelfManagementEligibilityTrace(curState, curQ, lambda * discount, true);
                        interference = "NoN"; //no need

                    } else if(deliveredInterventions.size() > 0) {
                        // override delta for the simulated action
                        Action intDeliveryAction = null;
                        for (Action a : actions) {
                            if (a.getName().equals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
                                intDeliveryAction = a;
                            }
                        }

                        action = new SimpleGroundedAction(intDeliveryAction);
                        QValue simCurQ = this.getQ(curState, action);
                        r = SelfManagementRewardFunction.getRewardReactionToIntervention();
                        delta = r + (discount * nextQV) - simCurQ.q;
                        simCurQ.q = simCurQ.q + (learningRate * delta);

                        et = new SelfManagementEligibilityTrace(curState, simCurQ, lambda * discount, true);
                        interference = "Y";
                    } else {
                        curQ.q = curQ.q + (learningRate * delta);
                        et = new SelfManagementEligibilityTrace(curState, curQ, lambda * discount, false);
                    }

                } else {
                    curQ.q = curQ.q + (learningRate * delta);
                    et = new SelfManagementEligibilityTrace(curState, curQ, lambda * discount, false);
                }

                traces.add(et);

                if (action.action.isPrimitive() || !this.shouldAnnotateOptions) {
                    ea.recordTransitionTo(action, nextState.s, r, qIndex.get(curState).qEntry, eeo.getUserContext(), eeo.getUserReaction(), interference);
                } else {
                    ea.appendAndMergeEpisodeAnalysis(((Option) action.action).getLastExecutionResults());
                }

                double deltaQ = Math.abs(et.initialQ - et.q.q);
                if (deltaQ > maxQChangeInLastEpisode) {
                    maxQChangeInLastEpisode = deltaQ;
                }

            }


            //move on
            curState = nextState;
            action = nextAction;
            curQ = nextQ;

            if(eeo.getUserReaction()) {
                deliveredInterventions = new ArrayList<>();
            }

            this.totalNumberOfSteps++;

        }


        if (episodeHistory.size() >= numEpisodesToStore) {
            episodeHistory.poll();
        }
        episodeHistory.offer(ea);

        return ea;
    }
}
