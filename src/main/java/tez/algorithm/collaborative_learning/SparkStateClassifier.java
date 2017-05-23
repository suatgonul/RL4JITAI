package tez.algorithm.collaborative_learning;

import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.spark.SparkConf;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.DataFrameNaFunctions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import scala.Array;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;

import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.col;


/**
 * Created by suat on 22-May-17.
 */
public class SparkStateClassifier extends StateClassifier {

    private static SparkStateClassifier instance = null;

    public static SparkStateClassifier getInstance() {
        if (instance == null) {
            instance = new SparkStateClassifier();
        }
        return instance;
    }

    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "D:\\tools\\spark-2.1.1-bin-hadoop2.7\\hadoop");

        SparkConf conf = new SparkConf().setMaster("local[2]").set("spark.executor.memory", "1g");
        SparkSession spark = SparkSession
                .builder()
                .config(conf)
                .appName("JavaPipelineExample")
                .getOrCreate();

        StructType structType = new StructType()
                .add("Time", DataTypes.IntegerType, true)
                .add("DayType", DataTypes.StringType, true)
                .add("Location", DataTypes.StringType, true)
                .add("Activity", DataTypes.StringType, true)
                .add("PhoneUsage", DataTypes.StringType, true)
                .add("StateOfMind", DataTypes.StringType, true)
                .add("EmotionalStatus", DataTypes.StringType, true)
                .add("Action", DataTypes.StringType, true);

        StructType testStructType = new StructType()
                .add("Time", DataTypes.IntegerType, true)
                .add("DayType", DataTypes.StringType, true)
                .add("Location", DataTypes.StringType, true)
                .add("Activity", DataTypes.StringType, true)
                .add("PhoneUsage", DataTypes.StringType, true)
                .add("StateOfMind", DataTypes.StringType, true)
                .add("EmotionalStatus", DataTypes.StringType, true);

        Dataset<Row> trainingData = spark.read()
                .schema(structType)
                .option("header", true)
                .csv("D:\\mine\\odtu\\6\\tez\\codes\\dummy_data\\trainingData.csv");
        trainingData.printSchema();
        trainingData.show();

        Dataset<Row> testData = spark.read()
                .schema(testStructType)
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

    @Override
    public void updateLearningModel(List<SelfManagementEpisodeAnalysis> ea) {
    }
}