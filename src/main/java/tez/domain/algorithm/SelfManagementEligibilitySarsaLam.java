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
import tez.experiment.performance.SelfManagementEpisodeAnalysis;
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

        SelfManagementEpisodeAnalysis ea = new SelfManagementEpisodeAnalysis(initialState);
        maxQChangeInLastEpisode = 0.;

        HashableState curState = this.stateHash(initialState);
        eStepCounter = 0;
        LinkedList<EligibilityTrace> traces = new LinkedList<SarsaLam.EligibilityTrace>();

        GroundedAction action = (GroundedAction) learningPolicy.getAction(curState.s);
        QValue curQ = this.getQ(curState, action);


        while (!env.isInTerminalState() && (eStepCounter < maxSteps || maxSteps == -1)) {

            if (action.actionName().equals(Constants.ACTION_INT_DELIVERY)) {
                deliveredInterventions.add(curState);
            }

            EnvironmentOutcome eo = action.executeIn(env);

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

            if (action.action.isPrimitive() || !this.shouldAnnotateOptions) {
                ea.recordTransitionTo(action, nextState.s, r);
            } else {
                ea.appendAndMergeEpisodeAnalysis(((Option) action.action).getLastExecutionResults());
            }

            ExtendedEnvironmentOutcome eeo = (ExtendedEnvironmentOutcome) eo;

            //delta
            double delta = r + (discount * nextQV) - curQ.q;

            //update all states visited in this episode so far
            boolean foundCurrentQTrace = false;
            for (EligibilityTrace et : traces) {

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

                // if the user reaction is positive
                if (eeo.getUserReaction() && et.q.a.actionName().equals(Constants.ACTION_INT_DELIVERY)) {
                    continue;
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

            boolean interventionDelivered = false;
            if (deliveredInterventions.size() > 0) {
                interventionDelivered = true;
            }
            if (!foundCurrentQTrace) {
                //then update and add it
                double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, curQ.s, curQ.a);

                EligibilityTrace et;

                // if there is an intervention delivered already and user's context is suitable for reacting to the
                // delivered intervention
                if (interventionDelivered && eeo.getUserReaction()) {
                    // override delta for the simulated action
                    Action intDeliveryAction = null;
                    for (Action a : actions) {
                        if (a.getName().equals(SelfManagementDomainGenerator.ACTION_INT_DELIVERY)) {
                            intDeliveryAction = a;
                        }
                    }

                    QValue simCurQ = this.getQ(curState, new SimpleGroundedAction(intDeliveryAction));
                    delta = SelfManagementRewardFunction.getRewardReactionToIntervention() + (discount * nextQV) - simCurQ.q;
                    simCurQ.q = simCurQ.q + (learningRate * delta);

                    et = new EligibilityTrace(curState, simCurQ, lambda * discount);

                } else {
                    curQ.q = curQ.q + (learningRate * delta);
                    et = new EligibilityTrace(curState, curQ, lambda * discount);
                }

                traces.add(et);

                double deltaQ = Math.abs(et.initialQ - et.q.q);
                if (deltaQ > maxQChangeInLastEpisode) {
                    maxQChangeInLastEpisode = deltaQ;
                }

            }


            //move on
            curState = nextState;
            action = nextAction;
            curQ = nextQ;

            this.totalNumberOfSteps++;

        }


        if (episodeHistory.size() >= numEpisodesToStore) {
            episodeHistory.poll();
        }
        episodeHistory.offer(ea);

        return ea;
    }
}
