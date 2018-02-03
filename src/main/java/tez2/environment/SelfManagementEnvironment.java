package tez2.environment;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import org.joda.time.DateTime;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementRewardFunction;
import tez.domain.SelfManagementStateGenerator;
import tez.environment.context.DayType;
import tez.persona.Activity;
import tez.persona.TimePlan;

/**
 * Created by suat on 26-May-17.
 */
public abstract class SelfManagementEnvironment extends SimulatedEnvironment {
    protected int stateChangeFrequency;
    protected DateTime currentTime;

    public SelfManagementEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, Object... subClassParameters) {
        super(domain, rf, tf);
        initParameters(subClassParameters);
        ((SelfManagementRewardFunction) rf).setEnvironment(this);
        this.stateChangeFrequency = stateChangeFrequency;
        stateGenerator = new SelfManagementStateGenerator(this);
    }

    public SelfManagementDomain getDomain() {
        return (SelfManagementDomain) domain;
    }

    protected abstract void initParameters(Object... params);

    public abstract State getNextState();

    public abstract State getStateFromCurrentContext();

    protected DayType getDayType(int dayOffset) {
        if (dayOffset - 1 % 7 < 5) {
            return DayType.WEEKDAY;
        } else {
            return DayType.WEEKEND;
        }
    }

    protected String getQuarterStateRepresentation() {
        int minute = currentTime.getMinuteOfHour();
        int quarterIndex = minute / 15;
        int quarterOffset = minute % 15;
        if (quarterOffset > 7) {
            quarterIndex++;
        }
        return currentTime.getHourOfDay() + "" + quarterIndex;
    }
}
