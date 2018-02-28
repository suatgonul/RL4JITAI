package tez2.algorithm.collaborative_learning.js;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.statehashing.HashableState;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import tez2.algorithm.collaborative_learning.DataItem;
import tez2.algorithm.collaborative_learning.StateClassifier;
import tez2.domain.DomainConfig;
import tez2.experiment.Experiment;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;
import tez2.experiment.performance.js.JsEpisodeAnalysis;
import tez2.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Created by suat on 22-May-17.
 */
public class SparkJsStateClassifier extends StateClassifier {

    private static final Logger log = Logger.getLogger(SparkJsStateClassifier.class);
    private static final String classifierPath = Experiment.runId + "rdfClassifier/js";
    private static SparkJsStateClassifier instance = null;
    private Domain domain;
    private Dataset<Row> stateActionData;
    private PipelineModel rdfClassifier;
    private Map<String, Integer> totalReactionCounts = new HashMap<>();

    private SparkJsStateClassifier() {
        StateClassifier.getSparkSession();
        totalReactionCounts.put(DomainConfig.ACTION_JITAI_1, 0);
        totalReactionCounts.put(DomainConfig.ACTION_JITAI_2, 0);
        totalReactionCounts.put(DomainConfig.ACTION_JITAI_3, 0);
    }

    public static SparkJsStateClassifier getInstance() {
        if (instance == null) {
            instance = new SparkJsStateClassifier();
        }
        return instance;
    }

    public static void endTrialForClassifier() {
        try {
            FileUtils.cleanDirectory(new File(classifierPath));
            instance.totalReactionCounts.put(DomainConfig.ACTION_JITAI_1, 0);
            instance.totalReactionCounts.put(DomainConfig.ACTION_JITAI_2, 0);
            instance.totalReactionCounts.put(DomainConfig.ACTION_JITAI_3, 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to clean the js classifier directory", e);
        }

        instance.stateActionCounts = new HashMap<>();
    }

    public void loadRandomForestClassifier(String path) {
        rdfClassifier = PipelineModel.load(path);
        LogUtil.log_generic(log, "Pipeline loaded");
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Override
    public void updateLearningModel(List<SelfManagementEpisodeAnalysis> ea) {
        updateStateActionCounts(ea);
        List<DataItem> dataItems = generateDataSetFromDataItems();
        List<JsStateActionBean> stateActionBeans = new ArrayList<>();
        for (DataItem dataItem : dataItems) {
            stateActionBeans.add(new JsStateActionBean(dataItem));
        }
        stateActionData = getSparkSession().createDataFrame(stateActionBeans, JsStateActionBean.class);
        stateActionData.cache();

        StringIndexerModel actionIndexer = new StringIndexer()
                .setInputCol("action")
                .setOutputCol("ActionIndex")
                .fit(stateActionData);

        VectorAssembler featureVectorAssembler = new VectorAssembler()
                .setInputCols(new String[]{"rememberBehavior", "behaviorFrequency", "habitStrength" /*"partOfDay",*/, "hourOfDay", "dayType"})
                .setOutputCol("features");

        RandomForestClassifier rf = new RandomForestClassifier()
                .setPredictionCol("prediction")
                .setLabelCol("ActionIndex")
                .setFeaturesCol("features");

        IndexToString labelConverter = new IndexToString()
                .setInputCol("prediction")
                .setOutputCol("predictedLabel")
                .setLabels(actionIndexer.labels());

        Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[]{actionIndexer, featureVectorAssembler, rf, labelConverter});

        rdfClassifier = pipeline.fit(stateActionData);
        try {
            rdfClassifier.write().overwrite().save(classifierPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

                // action
                if (actionName.contentEquals(DomainConfig.ACTION_NO_ACTION)) {
                    if(r == -50) {
                        if(i % 2 == 0) {
                            Integer jitai1Count = totalReactionCounts.get(DomainConfig.ACTION_JITAI_1);
                            Integer jitai2Count = totalReactionCounts.get(DomainConfig.ACTION_JITAI_2);
                            int total = jitai1Count + jitai2Count;

                            double ratio;
                            if(total == 0) {
                                ratio = 0.5;
                            } else {
                                ratio = (double) jitai1Count / (double) total;
                            }

                            if (new Random().nextDouble() < ratio) {
                                actionName = DomainConfig.ACTION_JITAI_1;
                            } else {
                                actionName = DomainConfig.ACTION_JITAI_2;
                            }
                            //System.out.println("trained action: " + actionName);
                        } else {
                            actionName = DomainConfig.ACTION_JITAI_3;
                        }
                    }

                } else {
                    if (!(r == -3 || r == 10)) {
                        if (i % 2 == 0) {
                            if (actionName.contentEquals(DomainConfig.ACTION_JITAI_1)) {
                                actionName = DomainConfig.ACTION_JITAI_2;
                            } else {
                                actionName = DomainConfig.ACTION_JITAI_1;
                            }
                        } else {
                            actionName = DomainConfig.ACTION_NO_ACTION;
                        }
                    } else {
                        Integer currentCount = totalReactionCounts.get(actionName);
                        totalReactionCounts.put(actionName, currentCount + 1);
                    }
                }

                //JsEpisodeAnalysis.printState(s.s);
                //System.out.println("reward: " + r + ", action: " + actionName);

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

    @Override
    public Action guessAction(State state) {
        if (rdfClassifier == null) {
            return null;
        }

        List<JsStateBean> testData = Arrays.asList(new JsStateBean[]{new JsStateBean(state)});
        Dataset<Row> testDataSet = getSparkSession().createDataFrame(testData, JsStateBean.class);
        Dataset<Row> predictions = rdfClassifier.transform(testDataSet);
        predictions.col("predictedLabel");
        Row predictionResult = predictions.collectAsList().get(0);
        String predictedAction = predictionResult.getString(predictionResult.fieldIndex("predictedLabel"));

//        JsEpisodeAnalysis.printState(state);
        //System.out.println("guessed action: " + predictedAction);
        return domain.getAction(predictedAction);
    }
}