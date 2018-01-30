package tez.environment.simulator;

import org.junit.Test;
import tez.environment.simulator.habit.HabitSimulator;

public class HabitSimulatorTest {
    @Test
    public void testSimilarityFunction() {
        HabitSimulator hs = new HabitSimulator();
        hs.setBF_TS(1.0);
        hs.setDP(0.79);
        hs.setSS(19.52);

        for (double i = 0.0; i < 1.0; i += 0.1) {
            hs.setBehaviourFrequency(i);
            System.out.println("bf: " + i + "sim: " + hs.calculateSimilarity());
        }
    }
}
