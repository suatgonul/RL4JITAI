package tez2.environment.context;

import tez2.environment.simulator.WorldSimulationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/25/2016.
 */
public class EmotionalModel {
    public List<EmotionalStatus> getEmotionalStatusForStateOfMind(StateOfMind stateOfMind) throws WorldSimulationException {
        List<EmotionalStatus> emotionalStatusList = new ArrayList<>();
        if (stateOfMind.equals(StateOfMind.CALM)) {
            emotionalStatusList.add(EmotionalStatus.NEUTRAL);
            emotionalStatusList.add(EmotionalStatus.HAPPY);
            emotionalStatusList.add(EmotionalStatus.RELAXED);

        } else if (stateOfMind.equals(StateOfMind.FOCUS)) {
            emotionalStatusList.add(EmotionalStatus.NEUTRAL);
            emotionalStatusList.add(EmotionalStatus.ANGRY);
            emotionalStatusList.add(EmotionalStatus.EXCITED);
            emotionalStatusList.add(EmotionalStatus.HAPPY);

        } else if (stateOfMind.equals(StateOfMind.TENSE)) {
            emotionalStatusList.add(EmotionalStatus.STRESSED);
            emotionalStatusList.add(EmotionalStatus.ANGRY);
        }
        throw new WorldSimulationException("Unidentified state of mind: " + stateOfMind);
    }
}
