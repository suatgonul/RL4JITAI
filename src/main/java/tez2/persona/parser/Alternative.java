package tez2.persona.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suatgonul on 12/10/2016.
 * Identifies a set of activities that could potentially be performed by a person during a particular time. For example,
 * the set of activities until getting to the office might differentiate for person considering that he/she takes the
 * bus or drives himself/herself. An {@link tez2.persona.parser.Alternative} instance allows identification of these two different sets of
 * activities.
 */
public class Alternative {
    private int groupId;
    private List<Integer> order = new ArrayList<Integer>();
    private int probability;
    private boolean last = false;
    private boolean continuation = false;

    public int getGroupId() {
        return groupId;
    }

    public List<Integer> getOrder() {
        return order;
    }

    public int getProbability() {
        return probability;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isContinuation() {
        return continuation;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void addOrder(int order) {
        this.order.add(order);
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setContinuation(boolean continuation) {
        this.continuation = continuation;
    }
}
