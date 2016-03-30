package mine;

import java.util.*;

/**
 * Created by suat on 24-Mar-16.
 */
public class PolicyImprovement {
    private int maxInterventionPerPeriod = 1;
    private int iterationAmount = 50;
    private Map<String, State> policy;

    private List<List<State>> userPreferences;

    public static void main(String[] args) {
        new PolicyImprovement().run();
    }

    private void run() {
        initiateUserPreferences();
        initiatePolicy();
        runSystem();
    }

    private void initiateUserPreferences() {
        this.userPreferences = new ArrayList<List<State>>();
        List<State> userPreferences = new ArrayList<State>();

        State preference = new State(Timing.EVENING, 1, 0);
        userPreferences.add(preference);
        this.userPreferences.add(userPreferences);

        userPreferences = new ArrayList<State>();
        preference = new State(Timing.MORNING, 1, 0);
        userPreferences.add(preference);
        preference = new State(Timing.EVENING, 1, 0);
        userPreferences.add(preference);
        this.userPreferences.add(userPreferences);
    }

    private void initiatePolicy() {
        // start state
        policy = new LinkedHashMap<String, State>();
        State startState = new State(Timing.START, 0, 0);
        policy.put(startState.getKey(), startState);

        for (int t = 1; t < Timing.values().length; t++) {
            for (int i = 0; i <= maxInterventionPerPeriod; i++) {
                for (int j = i; j <= i + (t - 1) * maxInterventionPerPeriod; j++) {
                    /**
                     * t: the period after which the intervention is applied
                     * i: the order of the intervention delivered in the period t
                     * j: total number of interventions delivered during the day
                     */
                    State newState = new State(Timing.values()[t], i, j);
                    policy.put(newState.getKey(), newState);
//                    System.out.println("Timing: " + t + " i: " + i + " j: " + j);
                }
            }
        }
    }

    private void runSystem() {
        for (int d = 0; d < iterationAmount; d++) {
            evaluatePolicy(d);
            printPolicy(d);
            improvePolicy();
            printPolicy(d);
        }
    }

    private void evaluatePolicy(int round) {
        //for each state calculate the value function
        for(int i=0; i<iterationAmount; i++) {
            for (State currentState : policy.values()) {
                double nextValue = 0;
                // v_k+1 (s) = Sum for all actions [ P(a|s) (R(a,s) + G( Sum for all target states (Trans. P(s, a, s') . v_k(s') ]
                for (Action a : Action.values()) {
                    State nextState = getNextState(currentState, a);
                    //R(a,s) is calculated based on the user preferences on reacting to intervention in a particular timing
                    //Discounting factor: 1
                    //Transition probability to a particular state on a particular action is 1
                    if (nextState != null) {
                        nextValue += currentState.getActionProbability() / 100 * (simulateReward(currentState, 1) * 1 + 1 * nextState.getCurrentValue());
//                    System.out.println("Temp next value: " + nextValue);
                    }
                }
                currentState.setCurrentValue(nextValue);
//            System.out.println("Round: " + round + " State " + currentState.getKey() + " Value: " + currentState.getCurrentValue());
            }
        }
    }

    private void improvePolicy() {
        for (State currentState : policy.values()) {
            double maxValue = Double.MIN_VALUE;
            Action maxAction = null;
            for (Action a : Action.values()) {
                State nextState = getNextState(currentState, a);
                if(nextState != null && nextState.getCurrentValue() > maxValue) {
                    maxValue = nextState.getCurrentValue();
                    maxAction = a;
                }
            }

            if(maxAction != null) {
                currentState.setActionProbability(maxAction.equals(Action.INTERVENTION) ? 100 : 0);
            }
        }
    }

    private State getNextState(State currentState, Action action) {
        if (currentState.getTiming().ordinal() == Timing.values().length-1) {
            return null;
        } else {
            int actionKey = action.equals(Action.INTERVENTION) ? 1 : 0;
            String key = "" + (currentState.getTiming().ordinal() + 1) + actionKey + (currentState.getNumberOfTotalInterventions() + actionKey);
            return policy.get(key);
        }
    }

    private double simulateReward(State currentState, int personNo) {
        List<State> userPreferences = this.userPreferences.get(personNo);
        boolean hasPreference =false;
        boolean interventionProbability = currentState.getActionProbability() >= 50 ? true : false;
        double result = 0;

        // user has a preferences for this period
        for (State preference : userPreferences) {
            if (Timing.values()[(currentState.getTiming().ordinal() + 1) % Timing.values().length].equals(preference.getTiming())) {
                if (preference.getNumberOfInverventionsInTiming() == 1) {
                    result = getRandom() >= 10 ? (interventionProbability ? 1 : -1) : 0;
                } else {
                    result = getRandom() >= 10 ? (interventionProbability ? -1 : 1) : 0;
                }
                hasPreference = true;
            }
        }

        // user does not have a preference for this period
        if(hasPreference == false) {
            result = interventionProbability ? (getRandom() >= 50 ? 1 : -1) : 0;
//            result = interventionProbability ? -1 : 0;
//            result = 0;
//            result = getRandom() >= 50 ? 1 : -1;
//            result = getRandom() >= 10 ? -1 : 0;
        }

//        System.out.println("Reward: " + result);
        return result;
    }

    private int getRandom() {
        Random r = new Random();
        int low = 0;
        int high = 100;
        return r.nextInt(high - low) + low;
    }

    private void printPolicy(int round) {
        System.out.println("Round: " + round);
        for(State s : policy.values()) {
//            System.out.println("State: " + s.getKey() + " Action prob: " + s.getActionProbability() + " Val: " + s.getCurrentValue());
            System.out.format("State: %s Action prob: %.0f Val: %.0f\n", s.getKey(), s.getActionProbability(), s.getCurrentValue());
        }
        System.out.println("========================");
    }

    private enum Timing {
        START, MORNING, AFTERNOON, EVENING, NIGHT;
    }

    private enum Action {
        NO_INTERVENTION, INTERVENTION
    }

    private class State {
        private Timing timing;
        private int numberOfInverventionsInTiming;
        private int numberOfTotalInterventions;
        private double currentValue;
        private double actionProbability;

        public State(Timing timing, int numberOfInverventionsInTiming, int numberOfTotalInterventions) {
            this.timing = timing;
            this.numberOfInverventionsInTiming = numberOfInverventionsInTiming;
            this.numberOfTotalInterventions = numberOfTotalInterventions;
            if(numberOfInverventionsInTiming == 1) {
                this.actionProbability = 100;
            } else {
                this.actionProbability = 0;
            }
            this.currentValue = 0;
        }

        public Timing getTiming() {
            return timing;
        }

        public int getNumberOfInverventionsInTiming() {
            return numberOfInverventionsInTiming;
        }

        public int getNumberOfTotalInterventions() {
            return numberOfTotalInterventions;
        }

        public double getActionProbability() {
            return actionProbability;
        }

        public void setActionProbability(double actionProbability) {
            this.actionProbability = actionProbability;
        }

        public double getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(double currentValue) {
            this.currentValue = currentValue;
        }

        private String getKey() {
            return "" + timing.ordinal() + numberOfInverventionsInTiming + numberOfTotalInterventions;
        }
    }
}
