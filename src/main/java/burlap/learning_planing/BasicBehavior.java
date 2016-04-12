package burlap.learning_planing;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.auxiliary.common.SinglePFTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

/**
 * Created by suat on 01-Apr-16.
 */
public class BasicBehavior {

    GridWorldDomain gwdg;
    Domain domain;
    RewardFunction rf;
    TerminalFunction tf;
    StateConditionTest goalCondition;
    State initialState;
    HashableStateFactory hashingFactory;
    Environment env;


    public static void main(String[] args) {

        BasicBehavior example = new BasicBehavior();
        String outputPath = "output/"; //directory to record results

        //run example
//        example.BFSExample(outputPath);
//        example.DFSExample(outputPath);
//        example.AStarExample(outputPath);
//        example.valueIterationExample(outputPath);
        example.QLearningExample(outputPath);

        //run the visualizer
        example.visualize(outputPath);
    }

    public BasicBehavior() {
        gwdg = new GridWorldDomain(11, 11);
        gwdg.setMapToFourRooms();
        domain = gwdg.generateDomain();

        rf = new UniformCostRF();
        tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION));
        goalCondition = new TFGoalCondition(tf);

        initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
        GridWorldDomain.setAgent(initialState, 0, 0);
        GridWorldDomain.setLocation(initialState, 0, 10, 10);

        hashingFactory = new SimpleHashableStateFactory();

        env = new SimulatedEnvironment(domain, rf, tf, initialState);
    }

    public void BFSExample(String outputPath){
        DeterministicPlanner planner = new BFS(domain, goalCondition, hashingFactory);
        Policy p = planner.planFromState(initialState);
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "bfs");
    }

    public void DFSExample(String outputPath){
        DeterministicPlanner planner = new DFS(domain, goalCondition, hashingFactory);
        Policy p = planner.planFromState(initialState);
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "dfs");
    }

    public void AStarExample(String outputPath){
        Heuristic mdistHeuristic = new Heuristic() {
            public double h(State s) {

                ObjectInstance agent = s.getFirstObjectOfClass(GridWorldDomain.CLASSAGENT);
                ObjectInstance location = s.getFirstObjectOfClass(GridWorldDomain.CLASSLOCATION);

                int ax = agent.getIntValForAttribute(GridWorldDomain.ATTX);
                int ay = agent.getIntValForAttribute(GridWorldDomain.ATTY);

                int lx = location.getIntValForAttribute(GridWorldDomain.ATTX);
                int ly = location.getIntValForAttribute(GridWorldDomain.ATTY);

                double mdist = Math.abs(ax-lx) + Math.abs(ay-ly);
                return -mdist;
            }
        };

        DeterministicPlanner planner = new AStar(domain, rf, goalCondition,
                hashingFactory, mdistHeuristic);
        Policy p = planner.planFromState(initialState);
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "astar");

    }

    public void valueIterationExample(String outputPath){
        Planner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.001, 100);
        Policy p = planner.planFromState(initialState);
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "vi");
    }

    public void QLearningExample(String outputPath){

        LearningAgent agent = new QLearning(domain, 0.99, hashingFactory, 0., 1.);

        //run learning for 50 episodes
        for(int i = 0; i < 50; i++){
            EpisodeAnalysis ea = agent.runLearningEpisode(env);

            ea.writeToFile(outputPath + "ql_" + i);
            System.out.println(i + ": " + ea.maxTimeStep());

            //reset environment for next learning episode
            env.resetEnvironment();
        }

    }

    public void visualize(String outputPath){
        Visualizer v = GridWorldVisualizer.getVisualizer(gwdg.getMap());
        new EpisodeSequenceVisualizer(v, domain, outputPath);
    }

}