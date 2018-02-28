package tez2.algorithm.collaborative_learning;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import tez2.algorithm.collaborative_learning.DataItem;
import tez2.domain.DomainConfig;
import tez2.domain.SelfManagementRewardFunction;
import tez2.experiment.Experiment;
import tez2.experiment.performance.OmiEpisodeAnalysis;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.*;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suat on 22-May-17.
 */
public abstract class StateClassifier {
    public Map<HashableState, Map<String, Integer>> stateActionCounts = new HashMap<>();

    protected SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();

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

    public static boolean classifierModeIncludes(String requiredMode) {
        for(String configuredMode : Experiment.CLASSIFIER_MODE) {
            if(configuredMode.contentEquals(requiredMode)) {
                return true;
            }
        }
        return false;
    }

    public static String getOmiUsageMode() {
        for(String configuredMode : Experiment.CLASSIFIER_MODE) {
            if(configuredMode.contentEquals("use") || configuredMode.contentEquals("use-omi")) {
                return configuredMode;
            }
        }
        return "none";
    }

    protected abstract void updateStateActionCounts(List<SelfManagementEpisodeAnalysis> eaList);

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
