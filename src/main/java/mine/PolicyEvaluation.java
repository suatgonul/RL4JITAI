package mine;

import java.util.*;

/**
 * Created by suat on 24-Mar-16.
 */
public class PolicyEvaluation {
    private int maxInterventionPerPeriod = 1;
    private int numberOfTestDays = 2;
    private Map<String, State> policy;

    private List<List<State>> userPreferences;

    public static void main(String[] args) {
        new PolicyEvaluation().run();
    }

    private void run() {
        initiateUserPreferences();
        initiatePolicy();
        runSystem();
    }

    private void initiateUserPreferences() {
        this.userPreferences = new ArrayList<List<State>>();
        List<State> userPreferences = new ArrayList<State>();
        // #1 loves morning interventions
        State preference = new State(Timing.MORNING, 1, 0);
        userPreferences.add(preference);
        this.userPreferences.add(userPreferences);

        // #2 loves afternoon and evening interventions
        userPreferences = new ArrayList<State>();
        preference = new State(Timing.AFTERNOON, 1, 0);
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
        State currentState;
        for (int d = 0; d < numberOfTestDays; d++) {
            evaluatePolicy(d);
        }
    }

    private void evaluatePolicy(int round) {
        //for each state calculate the value function
        for (State currentState : policy.values()) {
            double nextValue = 0;
            // v_k+1 (s) = Sum for all actions [ P(a|s) (R(a,s) + G( Sum for all target states (Trans. P(s, a, s') . v_k(s') ]
            for (Action a : Action.values()) {
                State nextState = getNextState(currentState, a);
                //R(a,s) is calculated based on the user preferences on reacting to intervention in a particular timing
                //Discounting factor: 1
                //Transition probability to a particular state on a particular action is 1
                if (nextState != null) {
                    nextValue += currentState.getActionProbability() / 100 * (simulateReward(currentState, 0) * 1 + 1 * nextState.getCurrentValue());
                    System.out.println("Temp next value: " + nextValue);
                }
            }
            currentState.setCurrentValue(nextValue);
            System.out.println("Round: " + round + " State " + currentState.getKey() + " Value: " + currentState.getCurrentValue());
        }

//        for (Timing t : Timing.values()) {
//            for (int i = 0; i <= maxInterventionPerPeriod; i++) {
//                for (int j = i; j <= i + t.ordinal() * maxInterventionPerPeriod; j++) {
//                    currentState = policy.get(new State())
//                            nextValue = 0;
//                    // v_k+1 (s) = Sum for all actions [ P(a|s) (R(a,s) + G( Sum for all target states (Trans. P(s, a, s') . v_k(s') ]
//                    for (Action a : Action.values()) {
//                        nextState = getNextState(currentState, a);
//                        //R(a,s) is calculated based on the user preferences on reacting to intervention in a particular timing
//                        //Discounting factor: 1
//                        //Transition probability to a particular state on a particular action is 1
//                        nextValue += nextValue + currentState.getActionProbability() / 100 * (simulateReward(currentState, 0) * 1 + 1 * nextState.getCurrentValue());
//                    }
//                    currentState.setCurrentValue(nextValue);
//                    System.out.println("Round: " + round + " Timing: " + currentState.timing.name() + " 1st int: " + currentState.getNumberOfInverventionsInTiming() + " Value: " + currentState.getCurrentValue());
//                }
//            }
//        }
    }

    private State getNextState(State currentState, Action action) {
        if (currentState.getTiming().equals(Timing.EVENING)) {
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
                    result = getRandom() >= 25 ? (interventionProbability ? 1 : -1) : 0;
                } else {
                    result = getRandom() >= 25 ? (interventionProbability ? -1 : 1) : 0;
                }
                hasPreference = true;
            }
        }

        // user does not have a preference for this period
        if(hasPreference == false) {
            result = interventionProbability ? (getRandom() >= 50 ? 1 : -1) : 0;
        }

        System.out.println("Reward: " + result);
        return result;
    }

    private int getRandom() {
        Random r = new Random();
        int low = 0;
        int high = 100;
        return r.nextInt(high - low) + low;
    }

    private enum Timing {
        START, MORNING, AFTERNOON, EVENING;
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
            this.actionProbability = 100;
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
