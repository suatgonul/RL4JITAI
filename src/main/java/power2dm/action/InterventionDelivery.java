//package power2dm.action;
//
//import burlap.oomdp.core.TransitionProbability;
//import burlap.oomdp.core.objects.ObjectInstance;
//import burlap.oomdp.core.states.State;
//import burlap.oomdp.singleagent.FullActionModel;
//import burlap.oomdp.singleagent.GroundedAction;
//import burlap.oomdp.singleagent.common.SimpleAction;
//
//import java.util.List;
//
//import static power2dm.P2DMRecommender.ATT_TIMING;
//import static power2dm.P2DMRecommender.ATT_TIMING_INT;
//import static power2dm.P2DMRecommender.CLASS_AGENT;
///**
// * Created by suat on 08-Apr-16.
// */
//public class InterventionDelivery extends SimpleAction implements FullActionModel {
//    @Override
//    protected State performActionHelper(State s, GroundedAction groundedAction) {
//        //get agent and current position
//        ObjectInstance agent = s.getFirstObjectOfClass(CLASS_AGENT);
//        int timing = agent.getIntValForAttribute(ATT_TIMING);
//        int timingIntAmount = agent.getIntValForAttribute(ATT_TIMING_INT);
//        int totalIntAmount = agent.getIntValForAttribute(ATT_TIMING_INT);
//
//        //sample directon with random roll
//        double r = Math.random();
//        double sumProb = 0.;
//        int dir = 0;
//        for(int i = 0; i < this.directionProbs.length; i++){
//            sumProb += this.directionProbs[i];
//            if(r < sumProb){
//                dir = i;
//                break; //found direction
//            }
//        }
//
//        //get resulting position
//        int [] newPos = this.moveResult(timing, timingIntAmount, dir);
//
//        //set the new position
//        agent.setValue(ATTX, newPos[0]);
//        agent.setValue(ATTY, newPos[1]);
//
//        //return the state we just modified
//        return s;
//    }
//
//    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
//        return null;
//    }
//}
