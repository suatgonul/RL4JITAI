package tez2.algorithm.collaborative_learning.omi;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
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
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import tez2.algorithm.collaborative_learning.DataItem;
import tez2.algorithm.collaborative_learning.StateClassifier;
import tez2.domain.omi.OmiDomainGenerator;
import tez2.experiment.Experiment;
import tez2.experiment.performance.OmiEpisodeAnalysis;
import tez2.experiment.performance.SelfManagementEpisodeAnalysis;
import tez2.util.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tez2.domain.DomainConfig.*;


/**
 * Created by suat on 22-May-17.
 */
public class SparkOmiStateClassifier extends StateClassifier {

    private static SparkOmiStateClassifier instance = null;
    private static final Logger log = Logger.getLogger(SparkOmiStateClassifier.class);
    private static final String classifierPath = "rdfClassifier/omi";

    private Domain domain;
    private Dataset<Row> stateActionData;
    private PipelineModel rdfClassifier;

    private SparkOmiStateClassifier() {
        if(Experiment.CLASSIFIER_MODE.equals("use")) {
            StateClassifier.getSparkSession();
            rdfClassifier = PipelineModel.load(classifierPath);
        }
    }

    public static SparkOmiStateClassifier getInstance() {
        if (instance == null) {
            instance = new SparkOmiStateClassifier();
        }
        return instance;
    }

    public void loadRandomForestClassifier(String path) {
        rdfClassifier = PipelineModel.load(path);
        LogUtil.log_generic(log, "Pipeline loaded");
    }

    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "D:\\tools\\spark-2.1.1-bin-hadoop2.7\\hadoop");

        OmiDomainGenerator smdg = new OmiDomainGenerator();
        Domain domain = smdg.generateDomain();
        ObjectClass oc = new ObjectClass(domain, CLASS_STATE);

        State s = new MutableState();
        s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));
        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);

        o.setValue(ATT_QUARTER_HOUR_OF_DAY, 0);
        o.setValue(ATT_LOCATION, 0);
        o.setValue(ATT_ACTIVITY, 0);
        o.setValue(ATT_PHONE_USAGE, 0);
        o.setValue(ATT_EMOTIONAL_STATUS, 0);



        State s2 = s.copy();
        s2.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s2.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_LOCATION, 1);

        SparkConf conf = new SparkConf().setMaster("local[4]").set("spark.executor.memory", "4g");
        SparkSession spark = SparkSession
                .builder()
                .config(conf)
                .appName("StateClassifier")
                .getOrCreate();

        StructType testDataStruct = new StructType()
                .add("Time", DataTypes.IntegerType, true)
                .add("Location", DataTypes.StringType, true)
                .add("Activity", DataTypes.StringType, true)
                .add("PhoneUsage", DataTypes.StringType, true)
                .add("EmotionalStatus", DataTypes.StringType, true);

        StructType trainingDataStruct = new StructType().copy(testDataStruct.fields());
        trainingDataStruct.add("Action", DataTypes.StringType, false);

        Dataset<Row> trainingData = spark.read()
                .schema(trainingDataStruct)
                .option("header", true)
                .csv("D:\\mine\\odtu\\6\\tez\\codes\\dummy_data\\trainingDatav2.csv");
        System.out.println("Training");
        trainingData.printSchema();
        trainingData.show();

        List<OmiStateBean> sbs = new ArrayList<>();
        sbs.add(new OmiStateBean(s));
        sbs.add(new OmiStateBean(s2));
        Dataset<Row> sbsDf = spark.createDataFrame(sbs, OmiStateBean.class);
        System.out.println("States");
        sbsDf.printSchema();
        sbsDf.show();

        DataItem di1 = new DataItem(s, "INT");
        DataItem di2 = new DataItem(s2, "NO_INT");
        List<OmiStateActionBean> sabList = new ArrayList<>();
        sabList.add(new OmiStateActionBean(di1));
        sabList.add(new OmiStateActionBean(di2));
        Dataset<Row> sabsDf = spark.createDataFrame(sabList, OmiStateActionBean.class);
        System.out.println("States with predictions");
        sabsDf.printSchema();
        sabsDf.show();

        Dataset<Row> testData = spark.read()
                .schema(testDataStruct)
                .option("header", true)
                .csv("D:\\mine\\odtu\\6\\tez\\codes\\dummy_data\\testDatav2.csv");
        //testData = new DataFrameNaFunctions(testData).fill("", new String[]{"Action"});
        System.out.println("Test");
        testData.printSchema();
        testData.show();

        StringIndexerModel locationIndexer = new StringIndexer()
                .setInputCol("Location")
                .setOutputCol("LocationIndex")
                .fit(trainingData);
        StringIndexerModel activityIndexer = new StringIndexer()
                .setInputCol("Activity")
                .setOutputCol("ActivityIndex")
                .fit(trainingData);
        StringIndexerModel phoneUsageIndexer = new StringIndexer()
                .setInputCol("PhoneUsage")
                .setOutputCol("PhoneUsageIndex")
                .fit(trainingData);
        StringIndexerModel emotionalStatusIndexer = new StringIndexer()
                .setInputCol("EmotionalStatus")
                .setOutputCol("EmotionalStatusIndex")
                .fit(trainingData);
        StringIndexerModel actionIndexer = new StringIndexer()
                .setInputCol("Action")
                .setOutputCol("ActionIndex")
                .fit(trainingData);

        VectorAssembler featureVectorAssembler = new VectorAssembler()
                .setInputCols(new String[]{"Time", "DayTypeIndex", "LocationIndex", "ActivityIndex", "PhoneUsageIndex", "EmotionalStatusIndex"})
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
                .setStages(new PipelineStage[]{actionIndexer, locationIndexer, activityIndexer, phoneUsageIndexer, emotionalStatusIndexer, featureVectorAssembler, rf, labelConverter});

        PipelineModel model = pipeline.fit(trainingData);

        Dataset<Row> predictions = model.transform(testData);
        System.out.println("Predictions");
        predictions.printSchema();
        predictions.show();

    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Override
    public void updateLearningModel(List<SelfManagementEpisodeAnalysis> ea) {
        updateStateActionCounts(ea);
        List<DataItem> dataItems = generateDataSetFromDataItems();
        List<OmiStateActionBean> stateActionBeans = new ArrayList<>();
        for (DataItem dataItem : dataItems) {
            stateActionBeans.add(new OmiStateActionBean(dataItem));
        }
        stateActionData = getSparkSession().createDataFrame(stateActionBeans, OmiStateActionBean.class);
        stateActionData.cache();

        StringIndexerModel locationIndexer = new StringIndexer()
                .setInputCol("location")
                .setOutputCol("LocationIndex")
                .fit(stateActionData);

        StringIndexerModel activityIndexer = new StringIndexer()
                .setInputCol("activity")
                .setOutputCol("ActivityIndex")
                .fit(stateActionData);

        StringIndexerModel phoneUsageIndexer = new StringIndexer()
                .setInputCol("phoneUsage")
                .setOutputCol("PhoneUsageIndex")
                .fit(stateActionData);

        StringIndexerModel emotionalStatusIndexer = new StringIndexer()
                .setInputCol("emotionalStatus")
                .setOutputCol("EmotionalStatusIndex")
                .fit(stateActionData);

        StringIndexerModel actionIndexer = new StringIndexer()
                .setInputCol("action")
                .setOutputCol("ActionIndex")
                .fit(stateActionData);

        VectorAssembler featureVectorAssembler = new VectorAssembler()
                .setInputCols(new String[]{"time", "numberOfJitaisSent", "LocationIndex", "ActivityIndex", "PhoneUsageIndex", "EmotionalStatusIndex"})
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
                .setStages(new PipelineStage[]{actionIndexer, locationIndexer, activityIndexer, phoneUsageIndexer, emotionalStatusIndexer, featureVectorAssembler, rf, labelConverter});

        rdfClassifier = pipeline.fit(stateActionData);
        try {
            rdfClassifier.write().overwrite().save(classifierPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Action guessAction(State state) {
        List<OmiStateBean> testData = Arrays.asList(new OmiStateBean[]{new OmiStateBean(state)});
        Dataset<Row> testDataSet = getSparkSession().createDataFrame(testData, OmiStateBean.class);
        Dataset<Row> predictions = rdfClassifier.transform(testDataSet);
        predictions.col("predictedLabel");
        Row predictionResult = predictions.collectAsList().get(0);
        String predictedAction = predictionResult.getString(predictionResult.fieldIndex("predictedLabel"));
        return domain.getAction(predictedAction);
    }
}