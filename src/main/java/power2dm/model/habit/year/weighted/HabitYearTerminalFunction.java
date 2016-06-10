package power2dm.model.habit.year.weighted;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

/**
 * Created by suat on 02-Jun-16.
 */
public class HabitYearTerminalFunction implements TerminalFunction {
    private HabitYearP2DMEnvironmentSimulator environmentSimulator;

    public boolean isTerminal(State s) {
        if(environmentSimulator.getDayOfExperiment() == 365) {
            return true;
        }
        return false;
    }

    public void setEnvironmentSimulator(HabitYearP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }
}
