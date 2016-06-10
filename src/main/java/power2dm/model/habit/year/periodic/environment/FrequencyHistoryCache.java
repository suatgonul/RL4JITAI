package power2dm.model.habit.year.periodic.environment;

/**
 * Created by suat on 10-Jun-16.
 */
public class FrequencyHistoryCache extends HistoryCache {

    public FrequencyHistoryCache(Integer... periodSizeArray) {
        super(periodSizeArray);
    }

    protected void calculateNewPeriodValue(int periodNo, int newValue, int lastValue) {
        int newPeriodValue = periodValues.get(periodNo);
        if(newPeriodValue == -1) {
            newPeriodValue = 0;
        }
        newPeriodValue = newPeriodValue + newValue - lastValue;
        periodValues.set(periodNo, newPeriodValue);
    }
}
