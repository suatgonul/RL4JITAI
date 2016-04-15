package power2dm;

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

    public static void main(String[] args) {
        P2DMRecommender example = new P2DMRecommender();
        String outputPath = "output/"; //directory to record results

        //run example
        example.QLearningExample(outputPath);
    }

    public P2DMRecommender() {
        domain = new P2DMDomain();
        rf = new DailyRewardFunction();
        tf = new DailyTerminalFunction();
        goalCondition = new TFGoalCondition(tf);

        initialState = domain.getInitialState();
        hashingFactory = new SimpleHashableStateFactory();

        env = new SimulatedEnvironment(domain, rf, tf, initialState);
    }

    public void QLearningExample(String outputPath) {

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
            int totalDaysOfinterventionDeliveredInPreferredRange = 0;
            //run learning for 50 episodes
            LearningAgent agent = new P2DMQLearning(domain, 0.99, hashingFactory, 0., 1.);
            for (int i = 0; i < 200; i++) {
                System.out.println("Episode : " + i);
                EpisodeAnalysis ea = agent.runLearningEpisode(env, 100);

                ea.writeToFile(outputPath + "ql_" + i);
                if (printPreferredTimeSteps(i, ea, agent)) {
                    totalDaysOfinterventionDeliveredInPreferredRange++;
                }

                //reset environment for next learning episode
                env.resetEnvironment();
            }
            System.out.println("Total days of intervention delivered in prefferred range: " + totalDaysOfinterventionDeliveredInPreferredRange);
//            totalInterventionDelieveyInPreferredRange += totalDaysOfinterventionDeliveredInPreferredRange;
//        }
//        System.out.println("Total days of intervention delivery in preferred range: " + totalInterventionDelieveyInPreferredRange);
//        System.out.println("Average days of intervention delivery in " + runNum + " run = " + (totalInterventionDelieveyInPreferredRange / runNum));
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
}
