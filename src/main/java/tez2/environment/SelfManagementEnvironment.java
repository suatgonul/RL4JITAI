package tez2.environment;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import tez2.domain.SelfManagementDomain;
import tez2.domain.SelfManagementRewardFunction;
import tez2.domain.SelfManagementStateGenerator;
import tez2.environment.context.DayPart;
import tez2.environment.context.DayType;

/**
 * Created by suat on 26-May-17.
 */
public abstract class SelfManagementEnvironment extends SimulatedEnvironment {
    protected int stateChangeFrequency;
    public DateTime currentTime;

    public SelfManagementEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency) {
        super(domain, rf, tf);
        ((SelfManagementRewardFunction) rf).setEnvironment(this);
        this.stateChangeFrequency = stateChangeFrequency;
        stateGenerator = new SelfManagementStateGenerator(this);
    }

    public SelfManagementDomain getDomain() {
        return (SelfManagementDomain) domain;
    }

    public abstract State getNextState(GroundedAction action);

    public abstract State getStateFromCurrentContext();

    protected int getDayType(int dayOffset) {
        if (dayOffset - 1 % 7 < 5) {
            return DayType.WEEKDAY.ordinal();
        } else {
            return DayType.WEEKEND.ordinal();
        }
    }

    protected int getDayPart() {
        return getDayPart(currentTime);
    }

    protected int getDayPart(DateTime time) {
        LocalTime localTime = time.toLocalTime();

        // 6:00
        LocalTime morningStart = new LocalTime().withHourOfDay(6).withMinuteOfHour(0).withSecondOfMinute(0);
        LocalTime morningEnd = morningStart.plusHours(5).plusMinutes(30).minusSeconds(1);
        // 11:30
        LocalTime noonStart = morningEnd.plusSeconds(1);
        LocalTime noonEnd = noonStart.plusHours(3).plusMinutes(30).minusSeconds(1);
        // 14:30
        LocalTime afternoonStart = noonEnd.plusSeconds(1);
        LocalTime afternoonEnd =afternoonStart.plusHours(3).minusSeconds(1);
        // 19:00
        LocalTime eveningStart = afternoonEnd.plusSeconds(1);
        LocalTime eveningEnd = eveningStart.plusHours(4).plusMinutes(30).minusSeconds(1);
        // 23:30
        LocalTime nightStart = eveningEnd.plusSeconds(1);
        LocalTime nightEnd = morningStart.minusSeconds(1);

        if(localTime.isAfter(nightEnd) && localTime.isBefore(noonStart)) {
            return DayPart.MORNING.ordinal();
        } else if(localTime.isAfter(morningEnd) && localTime.isBefore(afternoonStart)) {
            return DayPart.NOON.ordinal();
        } else if(localTime.isAfter(noonEnd) && localTime.isBefore(eveningStart)) {
            return DayPart.AFTERNOON.ordinal();
        } else if(localTime.isAfter(afternoonEnd) && localTime.isBefore(nightStart)) {
            return DayPart.EVENING.ordinal();
        } else {
            return DayPart.NIGHT.ordinal();
        }
    }

    protected int getQuarterStateRepresentation() {
        int minute = currentTime.getHourOfDay() * 60 + currentTime.getMinuteOfHour();
        int quarterIndex = minute / 15;
        return quarterIndex;
    }
}
