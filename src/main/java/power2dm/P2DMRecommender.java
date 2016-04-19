package power2dm;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import power2dm.algorithm.P2DMQLearning;
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
        example.QLearningExample(new EpsilonGreedy(0.1), outputPath);
//        example.sarsaExample(outputPath);
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
        P2DMQLearning agent = new P2DMQLearning(domain, 0.99, hashingFactory, 0., 1.);
        agent.setLearningPolicy(policy);
        ((SolverDerivedPolicy) policy).setSolver(agent);

        for (int i = 0; i < 50000; i++) {
            if(i%1000 == 0)System.out.println("Episode : " + i);
            EpisodeAnalysis ea = agent.runLearningEpisode(env, 100, i);

//            ea.writeToFile(outputPath + "ql_" + i);
            if (printPreferredTimeSteps(i, ea, agent)) {
                totalDaysOfinterventionDeliveredInPreferredRange++;
            }

            // record total reward
            totalRewards.add(calculateTotalReward(ea));

            //reset environment for next learning episode
            env.resetEnvironment();
        }
//        System.out.println("Total days of intervention delivered in prefferred range: " + totalDaysOfinterventionDeliveredInPreferredRange);
        totalRewardsPerPolicy.put(policy.getClass().getName(), totalRewards);
//            totalInterventionDelieveyInPreferredRange += totalDaysOfinterventionDeliveredInPreferredRange;
//        }
//        System.out.println("Total days of intervention delivery in preferred range: " + totalInterventionDelieveyInPreferredRange);
//        System.out.println("Average days of intervention delivery in " + runNum + " run = " + (totalInterventionDelieveyInPreferredRange / runNum));
    }

    private void sarsaExample(String outputPath) {
        LearningAgent agent = new SarsaLam(domain, 0.99, hashingFactory, 0., 0.5, 0.3);

        List<Double> totalRewards = new ArrayList<Double>();

        //run learning for 50 episodes
        for(int i = 0; i < 100000; i++){
            EpisodeAnalysis ea = agent.runLearningEpisode(env);

//            ea.writeToFile(outputPath + "sarsa_" + i);
//            System.out.println(i + ": " + ea.maxTimeStep());

            // record total reward
            totalRewards.add(calculateTotalReward(ea));

            //reset environment for next learning episode
            env.resetEnvironment();
        }

        totalRewardsPerPolicy.put("Epsilon Greedy", totalRewards);
    }

    private void drawRewardChards() {
        RewardVisualizer.createRewardGraph("Total rewards per episode", "Greedy rewards", totalRewardsPerPolicy);
    }

    private boolean printPreferredTimeSteps(int episode, EpisodeAnalysis ea, LearningAgent agent) {
        boolean interventionDeliveredInPreferredRange = false;

        for (int a = 0; a < ea.actionSequence.size(); a++) {
            if (a >= 19 && a <= 21) {
                if (ea.getAction(a).actionName().equals(P2DMDomain.ACTION_INT_DELIVERY)) {
                    interventionDeliveredInPreferredRange = true;
                }
            }
        }
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
