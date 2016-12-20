package tez.obselete.rlglue.skeleton;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

/**
 *  This is a very simple environment with discrete observations corresponding to states labeled {0,1,...,19,20}
    The starting state is 10.

    There are 2 actions = {0,1}.  0 decrements the state, 1 increments the state.

    The problem is episodic, ending when state 0 or 20 is reached, giving reward -1 or +1, respectively.  The reward is 0 on 
    all other steps.
 * @author Brian Tanner
 */
public class SkeletonEnvironment implements EnvironmentInterface {
    private int currentState=10;
    
    public String env_init() {
        System.out.println("Environment init");
        return "";
    }

    public Observation env_start() {
        currentState=10;
        
        Observation returnObservation=new Observation(1,0,0);
        returnObservation.intArray[0]=currentState;
        return returnObservation;
    }

    public Reward_observation_terminal env_step(Action thisAction) {
        boolean episodeOver=false;
        double theReward=0.0d;
        
        if(thisAction.intArray[0]==0)
            currentState--;
        if(thisAction.intArray[0]==1)
            currentState++;
        
        if(currentState<=0){
            currentState=0;
            theReward=-1.0d;
            episodeOver=true;
        }
        
        if(currentState>=20){
            currentState=20;
            episodeOver=true;
            theReward=1.0d;
        }
        Observation returnObservation=new Observation(1,0,0);
        returnObservation.intArray[0]=currentState;
        
        Reward_observation_terminal returnRewardObs=new Reward_observation_terminal(theReward,returnObservation,episodeOver);
        return returnRewardObs;
    }

    public void env_cleanup() {
    }

    public String env_message(String message) {
        if(message.equals("what is your name?"))
            return "my name is skeleton_environment, Java edition!";

	return "I don't know how to respond to your message";
    }
    
   /**
     * This is a trick we can use to make the agent easily loadable.
     * @param args
     */
    public static void main(String[] args){
        EnvironmentLoader theLoader=new EnvironmentLoader(new SkeletonEnvironment());
        theLoader.run();
    }


}
