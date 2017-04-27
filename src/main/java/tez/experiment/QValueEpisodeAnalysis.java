package tez.experiment;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 4/26/2017.
 */
public class QValueEpisodeAnalysis extends EpisodeAnalysis {
    public List<List<QValue>> qValuesForStates;

    public QValueEpisodeAnalysis(State initialState) {
        super(initialState);
        qValuesForStates = new ArrayList<>();
    }

    public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, List<QValue> qValues) {
        List<QValue> copyList = new ArrayList<>();
        for(QValue qv : qValues){
            copyList.add(new QValue(qv));
        }

        qValuesForStates.add(copyList);
        super.recordTransitionTo(usingAction, nextState, r);
    }
}
