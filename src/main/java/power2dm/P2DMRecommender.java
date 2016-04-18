package power2dm;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import power2dm.visualization.RewardVisualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 08-Apr-16.
 */
public class P2DMRecommender {

    public static final String ACTION_DELIVER = "deliver";

    private P2DMDomain domain;
    private RewardFunction rf;
    private TerminalFunction tf;
    private StateConditionTest goalCondition;
    private State initialState;
    private HashableStateFactory hashingFactory;
    private Environment env;

    private Map<String, List<Double>> totalRewardsPerPolicy;

    public static void main(String[] args) {
        P2DMRecommender example = new P2DMRecommender();
        String outputPath = "output/"; //directory to record results

        //run example
//        example.QLearningExample(new GreedyQPolicy(), outputPath);
        example.QLearningExample(new EpsilonGreedy(0.05), outputPath);

        //visualize total rewards
        example.drawRewardChards();
    }

    public P2DMRecommender() {
        domain = new P2DMDomain();
        rf = new DailyRewardFunction();
        tf = new DailyTerminalFunction();
        goalCondition = new TFGoalCondition(tf);

        initialState = domain.getInitialState();
        hashingFactory = new SimpleHashableStateFactory();

        env = new SimulatedEnvironment(domain, rf, tf, initialState);

        totalRewardsPerPolicy = new HashMap<String, List<Double>>();
    }

    public void QLearningExample(Policy policy, String outputPath) {
        int totalInterventionDelieveyInPreferredRange = 0;

        // execute 50 runs of learning
//        int runNum = 50;
//        for (int r = 0; r < runNum; r++) {
//            if (r % 5 == 0) {
//                int willingness = (r / 5) % 2 == 0 ? 1 : 0;
//                domain.getSimulator().setWillingnessToReact(willingness);
//                System.out.println("Willingness set to: " + willingness);
//            }
//
        List<Double> totalRewards = new ArrayList<Double>();
        int totalDaysOfinterventionDeliveredInPreferredRange = 0;
        //run learning for 50 episodes
        LearningAgent agent = new P2DMQLearning(domain, 0.99, hashingFactory, 0., 1.);
        ((P2DMQLearning) agent).setLearningPolicy(policy);
        ((SolverDerivedPolicy) policy).setSolver((P2DMQLearning) agent);

        for (int i = 0; i < 1000; i++) {
            System.out.println("Episode : " + i);
            EpisodeAnalysis ea = agent.runLearningEpisode(env, 100);

            ea.writeToFile(outputPath + "ql_" + i);
            if (printPreferredTimeSteps(i, ea, agent)) {
                totalDaysOfinterventionDeliveredInPreferredRange++;
            }

            // record total reward
            totalRewards.add(calculateTotalReward(ea));

            //reset environment for next learning episode
            env.resetEnvironment();
        }
        System.out.println("Total days of intervention delivered in prefferred range: " + totalDaysOfinterventionDeliveredInPreferredRange);
        totalRewardsPerPolicy.put(policy.getClass().getName(), totalRewards);
//            totalInterventionDelieveyInPreferredRange += totalDaysOfinterventionDeliveredInPreferredRange;
//        }
//        System.out.println("Total days of intervention delivery in preferred range: " + totalInterventionDelieveyInPreferredRange);
//        System.out.println("Average days of intervention delivery in " + runNum + " run = " + (totalInterventionDelieveyInPreferredRange / runNum));
    }

    private void drawRewardChards() {
        RewardVisualizer.createRewardGraph("Total rewards per episode", "Greedy rewards", totalRewardsPerPolicy);
    }

    private boolean printPreferredTimeSteps(int episode, EpisodeAnalysis ea, LearningAgent agent) {
        boolean interventionDeliveredInPreferredRange = false;

        for (int a = 0; a < ea.actionSequence.size(); a++) {
            GroundedAction ga = ea.actionSequence.get(a);
//                if (a >= 17 && a <= 21) {
//            if (ga.actionName().equals("intervention_delivery") && (a >= 19 && a <= 21)) {
            if (a >= 19 && a <= 21) {
//                    System.out.print(" Hour " + a + ": " + ga.actionName());
                State state = ea.stateSequence.get(a);
//                System.out.print("State " + a + " " + state.getFirstObjectOfClass(P2DMDomain.CLASS_STATE).getIntValForAttribute(P2DMDomain.ATT_REACTED_INT) + ", ");
//                List<QValue> qValues = ((QLearning) agent).getQs(state);
//                System.out.print("Act " + ea.getAction(a).actionName() + ", ");
//                System.out.print("Rew " + ea.rewardSequence.get(a) + ", ");
//                System.out.print("Q-Val " + qValues.get(0).a.actionName() + ": " + qValues.get(0).q + ", ");
//                System.out.print(qValues.get(1).a.actionName() + ": " + qValues.get(1).q);
//                System.out.println();

                if (ea.getAction(a).actionName().equals(P2DMDomain.ACTION_INT_DELIVERY)) {
                    interventionDeliveredInPreferredRange = true;
                }
            }
        }
        System.out.println();
        return interventionDeliveredInPreferredRange;
    }

    private double calculateTotalReward(EpisodeAnalysis ea) {
        double totalReward = 0;
        for (double reward : ea.rewardSequence) {
            totalReward += reward;
        }
        return totalReward;
    }
}
