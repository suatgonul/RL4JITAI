package power2dm.model.habit.year.periodic.environment;

/**
 * Created by suat on 10-Jun-16.
 */
public class AutomationHistoryCache extends HistoryCache {
    public AutomationHistoryCache(Integer... periodSizeArray) {
        super(periodSizeArray);
    }

    protected void calculateNewPeriodValue(int periodNo, int newValue, int lastValue) {
        Integer newPeriodValue = periodValues.get(periodNo);
        if(newPeriodValue == -1) {
            newPeriodValue = 0;
        }
        newPeriodValue = newPeriodValue + (newValue == 1 ? 1 : 0) - (lastValue == 1 ? 1 : 0);
        periodValues.set(periodNo, newPeriodValue);
    }
}
