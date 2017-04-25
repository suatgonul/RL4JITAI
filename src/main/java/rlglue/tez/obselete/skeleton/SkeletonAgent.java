package rlglue.tez.obselete.skeleton;/*
 * Copyright 2008 Brian Tanner
 * http://rl-glue-ext.googlecode.com/
 * brian@tannerpages.com
 * http://brian.tannerpages.com
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
* 
*  $Revision: 676 $
*  $Date: 2009-02-08 18:15:04 -0700 (Sun, 08 Feb 2009) $
*  $Author: brian@tannerpages.com $
*  $HeadURL: http://rl-glue-ext.googlecode.com/svn/trunk/projects/codecs/Java/examples/skeleton-sample/SkeletonAgent.java $
* 
*/

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import java.util.Random;

/**
 *
 * @author Brian Tanner
 */
public class SkeletonAgent implements AgentInterface {

    Random randGenerator = new Random();
    Action lastAction;
    Observation lastObservation;

    public void agent_init(String taskSpecification) {
        System.out.println("Agent init");
    }

    public Action agent_start(Observation observation) {
        /**
         * Choose a random action (0 or 1)
         */
        int theIntAction = randGenerator.nextInt(2);
        /**
         * Create a structure to hold 1 integer action
         * and set the value
         */
        Action returnAction = new Action(1, 0, 0);
        returnAction.intArray[0] = theIntAction;

        lastAction = returnAction.duplicate();
        lastObservation = observation.duplicate();

        return returnAction;
    }

    public Action agent_step(double reward, Observation observation) {
        /**
         * Choose a random action (0 or 1)
         */
        int theIntAction = randGenerator.nextInt(2);
        /**
         * Create a structure to hold 1 integer action
         * and set the value (alternate method)
         */
        Action returnAction = new Action();
        returnAction.intArray = new int[]{theIntAction};

        lastAction = returnAction.duplicate();
        lastObservation = observation.duplicate();

        return returnAction;
    }

    public void agent_end(double reward) {
    }

    public void agent_cleanup() {
        lastAction=null;
        lastObservation=null;
    }

    public String agent_message(String message) {
        if(message.equals("what is your name?"))
            return "my name is skeleton_agent, Java edition!";

	return "I don't know how to respond to your message";
    }
    
    /**
     * This is a trick we can use to make the agent easily loadable.
     * @param args
     */
    
    public static void main(String[] args){
     	AgentLoader theLoader=new AgentLoader(new SkeletonAgent());
        theLoader.run();
	}

}
