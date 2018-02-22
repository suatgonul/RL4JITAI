
package tez2.algorithm.collaborative_learning.js;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
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
import tez2.experiment.Experiment;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;
import tez2.util.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by suat on 22-May-17.
 */
public class SparkJsStateClassifier extends StateClassifier {

    private static SparkJsStateClassifier instance = null;
    private static final Logger log = Logger.getLogger(SparkJsStateClassifier.class);
    private static final String classifierPath = "rdfClassifier/js";

    private Domain domain;
    private Dataset<Row> stateActionData;
    private PipelineModel rdfClassifier;

    private SparkJsStateClassifier() {
        if(Experiment.CLASSIFIER_MODE.equals("use")) {
            StateClassifier.getSparkSession();
            rdfClassifier = PipelineModel.load(classifierPath);
        }
    }

    public static SparkJsStateClassifier getInstance() {
        if (instance == null) {
            instance = new SparkJsStateClassifier();
        }
        return instance;
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
                .setInputCols(new String[]{"rememberBehavior", "behaviorFrequency", "habitStrength", "partOfDay", "dayType"})
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

    @Override
    public Action guessAction(State state) {
        List<JsStateBean> testData = Arrays.asList(new JsStateBean[]{new JsStateBean(state)});
        Dataset<Row> testDataSet = getSparkSession().createDataFrame(testData, JsStateBean.class);
        Dataset<Row> predictions = rdfClassifier.transform(testDataSet);
        predictions.col("predictedLabel");
        Row predictionResult = predictions.collectAsList().get(0);
        String predictedAction = predictionResult.getString(predictionResult.fieldIndex("predictedLabel"));
        return domain.getAction(predictedAction);
    }
}