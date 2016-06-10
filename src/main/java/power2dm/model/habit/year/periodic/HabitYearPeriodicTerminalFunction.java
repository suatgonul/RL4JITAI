package power2dm.model.habit.year.periodic;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import power2dm.model.habit.year.periodic.environment.HabitYearPeriodicP2DMEnvironmentSimulator;

/**
 * Created by suat on 02-Jun-16.
 */
public class HabitYearPeriodicTerminalFunction implements TerminalFunction {
    private HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator;

    public boolean isTerminal(State s) {
        if(environmentSimulator.getDayOfExperiment() == 365) {
            return true;
        }
        return false;
    }

    public void setEnvironmentSimulator(HabitYearPeriodicP2DMEnvironmentSimulator environmentSimulator) {
        this.environmentSimulator = environmentSimulator;
    }
}
