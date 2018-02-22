package tez2.algorithm.collaborative_learning;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import tez2.algorithm.collaborative_learning.DataItem;
import tez2.domain.SelfManagementRewardFunction;
import tez2.experiment.performance.OmiEpisodeAnalysis;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.*;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suat on 22-May-17.
 */
public abstract class StateClassifier {
    protected Map<HashableState, Map<String, Integer>> stateActionCounts = new HashMap<>();

    private SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

    private static SparkSession spark;

    public abstract void updateLearningModel(List<SelfManagementEpisodeAnalysis> ea);

    public abstract Action guessAction(State state);

    public static SparkSession getSparkSession() {
        if(spark == null) {
            System.setProperty("hadoop.home.dir", "D:\\tools\\spark-2.1.1-bin-hadoop2.7\\hadoop");
            //System.setProperty("SPARK_JAVA_OPTS", "-Xmx14g");
            SparkConf conf = new SparkConf()
                    .setMaster("local")
                    .set("spark.executor.memory", "4g")
                    .set("spark.driver.memory", "4g")
                    .set("spark.driver.allowMultipleContexts", "true")
                    .setAppName("SMSpark");
            SparkContext context = new SparkContext(conf);
            //context.setCheckpointDir("D:\\mine\\odtu\\6\\tez\\codes\\spark_checkpoint");
            spark = SparkSession
                    .builder()
                    .config(conf)
                    .sparkContext(context)
                    .getOrCreate();
        }
        return spark;
    }

    protected void updateStateActionCounts(List<SelfManagementEpisodeAnalysis> eaList) {
        for (int t = 0; t < eaList.size(); t++) {
            SelfManagementEpisodeAnalysis ea = eaList.get(t);
            for (int i = 0; i < ea.actionSequence.size(); i++) {
                // do not keep the state as data item if no intervention is delivered
                // i.e. keep only the states and actions where an intervention is delivered (as an indicator of preference)
                double r = ea.rewardSequence.get(i);
                String actionName = ea.actionSequence.get(i).actionName();
                HashableState s = hashingFactory.hashState(ea.stateSequence.get(i));
                Map<String, Integer> actionCounts = stateActionCounts.get(s);
                if (actionCounts == null) {
                    actionCounts = new HashMap<>();
                    stateActionCounts.put(s, actionCounts);
                }

                Integer count = actionCounts.get(actionName);
                if (count == null) {
                    count = 0;
                }
                count++;
                actionCounts.put(actionName, count);
            }
        }
        System.out.println("Number of distinct states: " + stateActionCounts.keySet().size());
    }

    protected List<DataItem> generateDataSetFromDataItems() {
        List<DataItem> dataItems = new ArrayList<>();
        for (Map.Entry<HashableState, Map<String, Integer>> e : stateActionCounts.entrySet()) {
            State s = e.getKey();
            Map<String, Integer> actionCounts = e.getValue();

            List<String> mostPreferredActions = new ArrayList<>();
            int max = Integer.MIN_VALUE;
            for (Map.Entry<String, Integer> e2 : actionCounts.entrySet()) {
                if (e2.getValue() > max) {
                    mostPreferredActions.clear();
                    mostPreferredActions.add(e2.getKey());
                    max = e2.getValue();
                } else if (e2.getValue() == max) {
                    mostPreferredActions.add(e2.getKey());
                }
            }

            Random r = new Random();
            String selectedAction = mostPreferredActions.get(r.nextInt(mostPreferredActions.size()));
            DataItem dataItem = new DataItem(s, selectedAction);
            dataItems.add(dataItem);
        }
        return dataItems;
    }
}
