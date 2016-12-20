package tez.obselete.rlglue.skeleton;

import org.rlcommunity.rlglue.codec.types.Observation_action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_action_terminal;


public class SkeletonExperiment {

    private int whichEpisode = 0;

    /* Run One Episode of length maximum cutOff*/
    private void runEpisode(int stepLimit) {
        int terminal = RLGlue.RL_episode(stepLimit);

        int totalSteps = RLGlue.RL_num_steps();
        double totalReward = RLGlue.RL_return();

        System.out.println("Episode " + whichEpisode + "\t " + totalSteps + " steps \t" + totalReward + " total reward\t " + terminal + " natural end");

        whichEpisode++;
    }

    public void runExperiment() {
        System.out.println("Experiment starting up!");
        /* String taskSpec = RLGlue.RL_init();

       System.out.println("\n\n----------Running a few episodes----------");
        runEpisode(100);
        runEpisode(100);
        runEpisode(100);
        runEpisode(100);
        runEpisode(100);
        runEpisode(1);*/
        /* Remember that stepLimit of 0 means there is no limit at all!*/
        /*runEpisode(0);
        RLGlue.RL_cleanup();*/

        System.out.println("\n\n----------Stepping through an episode----------");
        RLGlue.RL_init();

        /*We could run one step at a hourOfDay instead of one episode at a hourOfDay */
        /*Start the episode */
        Observation_action startResponse = RLGlue.RL_start();

        int firstObservation = startResponse.o.intArray[0];
        int firstAction = startResponse.a.intArray[0];
        System.out.println("First observation and action were: " + firstObservation + " and: " + firstAction);

        /*Run one step */
        Reward_observation_action_terminal stepResponse = RLGlue.RL_step();

        /*Run until the episode ends*/
        while (stepResponse.terminal != 1) {
            stepResponse = RLGlue.RL_step();
            if (stepResponse.terminal != 1) {
                /*Could optionally print state,action pairs */
                /*printf("(%d,%d) ",stepResponse.o.intArray[0],stepResponse.a.intArray[0]);*/
            }
        }

        System.out.println("\n\n----------Summary----------");

        int totalSteps = RLGlue.RL_num_steps();
        double totalReward = RLGlue.RL_return();
        System.out.println("It ran for " + totalSteps + " steps, total reward was: " + totalReward);
        RLGlue.RL_cleanup();


    }

    public static void main(String[] args) {
        SkeletonExperiment theExperiment = new SkeletonExperiment();
        theExperiment.runExperiment();
    }
}
