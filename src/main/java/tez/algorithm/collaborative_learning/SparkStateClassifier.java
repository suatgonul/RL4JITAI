package tez.algorithm.collaborative_learning;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
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
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tez.domain.SelfManagementDomainGenerator.*;


/**
 * Created by suat on 22-May-17.
 */
public class SparkStateClassifier extends StateClassifier {

    private static SparkStateClassifier instance = null;
    private Domain domain;
    private SparkSession spark;
    private Dataset<Row> stateActionData;
    private PipelineModel rdfClassifier;

    private SparkStateClassifier() {
        System.setProperty("hadoop.home.dir", "D:\\tools\\spark-2.1.1-bin-hadoop2.7\\hadoop");
        //System.setProperty("SPARK_JAVA_OPTS", "-Xmx14g");
        SparkConf conf = new SparkConf()
                .setMaster("local")
                .set("spark.executor.memory", "4g")
                .set("spark.driver.memory", "4g")
                .setAppName("StateClassifier");
        SparkContext context = new SparkContext(conf);
        //context.setCheckpointDir("D:\\mine\\odtu\\6\\tez\\codes\\spark_checkpoint");
        spark = SparkSession
                .builder()
                .config(conf)
                .sparkContext(context)
                .getOrCreate();
    }

    public static SparkStateClassifier getInstance() {
        if (instance == null) {
            instance = new SparkStateClassifier();
        }
        return instance;
    }

    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "D:\\tools\\spark-2.1.1-bin-hadoop2.7\\hadoop");

        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        ObjectClass oc = new ObjectClass(domain, CLASS_STATE);

        State s = new MutableState();
        s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));
        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_DAY_TYPE, 0);
        o.setValue(ATT_LOCATION, 0);
        o.setValue(ATT_ACTIVITY_TIME, "10:40");
        o.setValue(ATT_ACTIVITY, 0);
        o.setValue(ATT_PHONE_USAGE, 0);
        o.setValue(ATT_EMOTIONAL_STATUS, 0);
        o.setValue(ATT_STATE_OF_MIND, 0);

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
                .add("DayType", DataTypes.StringType, true)
                .add("Location", DataTypes.StringType, true)
                .add("Activity", DataTypes.StringType, true)
                .add("PhoneUsage", DataTypes.StringType, true)
                .add("StateOfMind", DataTypes.StringType, true)
                .add("EmotionalStatus", DataTypes.StringType, true);

        StructType trainingDataStruct = new StructType().copy(testDataStruct.fields());
        trainingDataStruct.add("Action", DataTypes.StringType, false);

        Dataset<Row> trainingData = spark.read()
                .schema(trainingDataStruct)
                .option("header", true)
                .csv("D:\\mine\\odtu\\6\\tez\\codes\\dummy_data\\trainingData.csv");
        trainingData.printSchema();
        trainingData.show();

        List<StateBean> sbs = new ArrayList<>();
        sbs.add(new StateBean(s));
        sbs.add(new StateBean(s2));
        Dataset<Row> sbsDf = spark.createDataFrame(sbs, StateBean.class);
        sbsDf.printSchema();
        sbsDf.show();

        DataItem di1 = new DataItem(s, "INT");
        DataItem di2 = new DataItem(s2, "NO_INT");
        List<StateActionBean> sabList = new ArrayList<>();
        sabList.add(new StateActionBean(di1));
        sabList.add(new StateActionBean(di2));
        Dataset<Row> sabsDf = spark.createDataFrame(sabList, StateActionBean.class);
        sabsDf.printSchema();
        sabsDf.show();

        System.exit(1);

        Dataset<Row> testData = spark.read()
                .schema(testDataStruct)
                .option("header", true)
                .csv("D:\\mine\\odtu\\6\\tez\\codes\\dummy_data\\testData.csv");
        //testData = new DataFrameNaFunctions(testData).fill("", new String[]{"Action"});
        testData.printSchema();
        testData.show();

        StringIndexerModel dayTypeIndexer = new StringIndexer()
                .setInputCol("DayType")
                .setOutputCol("DayTypeIndex")
                .fit(trainingData);
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
        StringIndexerModel stateOfMindIndexer = new StringIndexer()
                .setInputCol("StateOfMind")
                .setOutputCol("StateOfMindIndex")
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
                .setInputCols(new String[]{"Time", "DayTypeIndex", "LocationIndex", "ActivityIndex", "PhoneUsageIndex",
                        "StateOfMindIndex", "EmotionalStatusIndex"})
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
                .setStages(new PipelineStage[]{actionIndexer, dayTypeIndexer, locationIndexer, activityIndexer, phoneUsageIndexer, stateOfMindIndexer, emotionalStatusIndexer, featureVectorAssembler, rf, labelConverter});

        PipelineModel model = pipeline.fit(trainingData);

        Dataset<Row> predictions = model.transform(testData);
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
        List<StateActionBean> stateActionBeans = new ArrayList<>();
        for (DataItem dataItem : dataItems) {
            stateActionBeans.add(new StateActionBean(dataItem));
        }
        stateActionData = spark.createDataFrame(stateActionBeans, StateActionBean.class);
        stateActionData.cache();

        StringIndexerModel dayTypeIndexer = new StringIndexer()
                .setInputCol("dayType")
                .setOutputCol("DayTypeIndex")
                .fit(stateActionData);

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

        StringIndexerModel stateOfMindIndexer = new StringIndexer()
                .setInputCol("stateOfMind")
                .setOutputCol("StateOfMindIndex")
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
                .setInputCols(new String[]{"time", "DayTypeIndex", "LocationIndex", "ActivityIndex", "PhoneUsageIndex",
                        "StateOfMindIndex", "EmotionalStatusIndex"})
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
                .setStages(new PipelineStage[]{actionIndexer, dayTypeIndexer, locationIndexer, activityIndexer, phoneUsageIndexer, stateOfMindIndexer, emotionalStatusIndexer, featureVectorAssembler, rf, labelConverter});

        rdfClassifier = pipeline.fit(stateActionData);
    }

    @Override
    public Action guessAction(State state) {
        List<StateBean> testData = Arrays.asList(new StateBean[]{new StateBean(state)});
        Dataset<Row> testDataSet = spark.createDataFrame(testData, StateBean.class);
        Dataset<Row> predictions = rdfClassifier.transform(testDataSet);
        predictions.col("predictedLabel");
        Row predictionResult = predictions.collectAsList().get(0);
        String predictedAction = predictionResult.getString(predictionResult.fieldIndex("predictedLabel"));
        return domain.getAction(predictedAction);
    }
}