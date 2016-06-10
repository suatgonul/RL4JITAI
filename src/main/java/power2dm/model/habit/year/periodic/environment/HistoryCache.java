package power2dm.model.habit.year.periodic.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by suat on 10-Jun-16.
 */
public abstract class HistoryCache {
    protected List<LinkedList<Integer>> periodQueues = new ArrayList<LinkedList<Integer>>();
    protected List<Integer> periodValues = new ArrayList<Integer>();
    protected List<Integer> periodSizes = null;

    public HistoryCache(Integer... periodSizeArray) {
        periodSizes = Arrays.asList(periodSizeArray);

        for (int i = 0; i < periodSizes.size(); i++) {
            periodQueues.add(new LinkedList<Integer>());
            periodValues.add(-1);
        }
    }

    public void addValue(int value) {
        for (int i = periodSizes.size() - 1; i > 0; i--) {
            // check if the previous queue is full
            LinkedList<Integer> previousQueue = periodQueues.get(i - 1);
            if (previousQueue.size() == periodSizes.get(i - 1)) {
                addValueToPeriodQueue(i, previousQueue.peek());
            }
        }
        addValueToPeriodQueue(0, value);
    }

    protected void addValueToPeriodQueue(int periodNo, int newValue) {
        LinkedList<Integer> theQueue = periodQueues.get(periodNo);

        int lastValue = 0;
        if (theQueue.size() == periodSizes.get(periodNo)) {
            lastValue = theQueue.poll();
        }

        theQueue.add(newValue);
        calculateNewPeriodValue(periodNo, newValue, lastValue);
    }

    protected abstract void calculateNewPeriodValue(int periodNo, int newValue, int lastValue);

    public int getValueForPeriod(int periodNo) {
        return periodValues.get(periodNo);
    }

    public void destroy() {
        periodQueues.clear();
        periodSizes = null;
        periodValues.clear();
        periodValues = null;
        periodSizes = null;
    }
}
