package tez2.environment;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
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
    protected DateTime currentTime;

    public SelfManagementEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency) {
        super(domain, rf, tf);
        ((SelfManagementRewardFunction) rf).setEnvironment(this);
        this.stateChangeFrequency = stateChangeFrequency;
        stateGenerator = new SelfManagementStateGenerator(this);
    }

    public SelfManagementDomain getDomain() {
        return (SelfManagementDomain) domain;
    }

    public abstract State getNextState();

    public abstract State getStateFromCurrentContext();

    public abstract boolean simulateBehavior();

    protected DayType getDayType(int dayOffset) {
        if (dayOffset - 1 % 7 < 5) {
            return DayType.WEEKDAY;
        } else {
            return DayType.WEEKEND;
        }
    }

    protected DayPart getDayPart() {
        LocalTime time = currentTime.toLocalTime();
        LocalTime morningStart = new LocalTime().withHourOfDay(6).withMinuteOfHour(0).withSecondOfMinute(0);
        LocalTime morningEnd = morningStart.plusHours(5).plusMinutes(30).minusSeconds(1);
        LocalTime noonStart = morningEnd.plusSeconds(1);
        LocalTime noonEnd = noonStart.plusHours(3).plusMinutes(30).minusSeconds(1);
        LocalTime afternoonStart = noonEnd.plusSeconds(1);
        LocalTime afternoonEnd =afternoonStart.plusHours(3).minusSeconds(1);
        LocalTime eveningStart = afternoonEnd.plusSeconds(1);
        LocalTime eveningEnd = eveningStart.plusHours(4).plusMinutes(30).minusSeconds(1);
        LocalTime nightStart = eveningEnd.plusSeconds(1);
        LocalTime nightEnd = morningStart.minusSeconds(1);
        System.out.println(morningStart);
        System.out.println(morningEnd);
        System.out.println(noonStart);
        System.out.println(noonEnd);
        System.out.println(afternoonStart);
        System.out.println(afternoonEnd);
        System.out.println(eveningStart);
        System.out.println(eveningEnd);
        System.out.println(nightStart);
        System.out.println(nightEnd);

        if(time.isAfter(nightEnd) && time.isBefore(noonStart)) {
            return DayPart.MORNING;
        } else if(time.isAfter(morningEnd) && time.isBefore(afternoonStart)) {
            return DayPart.NOON;
        } else if(time.isAfter(noonEnd) && time.isBefore(eveningStart)) {
            return DayPart.AFTERNOON;
        } else if(time.isAfter(afternoonEnd) && time.isBefore(nightStart)) {
            return DayPart.EVENING;
        } else {
            return DayPart.NIGHT;
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
