package tez.algorithm.collaborative_learning;

import burlap.oomdp.core.states.State;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;
import water.bindings.pojos.*;

import javax.swing.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by suat on 13-May-17.
 */
public class StateClassifier {
    private String LEARNING_DATA_PATH = "D:\\mine\\odtu\\6\\tez\\codes\\data";
    private ModelsV3 model = null;
    private H2oApi h2o = new H2oApi();

    public static void main(String[] args) {
        /*Client client = Client.create();

        WebResource webResource = client.resource("http://localhost:54321/3/Predictions/models/DRF_model_okhttp_1494720676293_2057/frames/testData.hex");
        Form input = new Form();
        input.putSingle("predictions_frame", "prediction-f31feea9-626b-4831-99d7-95f19c2fa07f");

        // POST method
        ClientResponse response = webResource.accept("application/json")
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, input);
        String output = response.getEntity(String.class);
        System.out.println(output);

        // check response status code
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }*/
        new StateClassifier().buildModelDRF(null);
    }

    public void buildModelDRF(SelfManagementEpisodeAnalysis ea) {
        // write the content of the episode analysis to file so that H2O can read it
        /*StringBuilder filePath = new StringBuilder(LEARNING_DATA_PATH).append("\\")
                .append(ea.associatedLearningAgentFactory.getAgentName())
                .append("-").append(ea.trialNo).append("-").append(ea.episodeNo).append(".csv");
        ea.writeToFile(filePath.toString());*/

        try {
            h2o.deleteAllFrames();
            h2o.deleteAllKeys();
            h2o.deleteAllModels();

            // STEP 0: init a session
            String sessionId = h2o.newSession().sessionKey;

            // STEP 1: import raw file
            ImportFilesV3 trainingDataImport;
            //trainingDataImport = h2o.importFiles(filePath);
            trainingDataImport = h2o.importFiles("D:\\mine\\odtu\\6\\tez\\codes\\data\\trainingData.csv");
            ImportFilesV3 testDataImport;
            testDataImport = h2o.importFiles("D:\\mine\\odtu\\6\\tez\\codes\\data\\testData.csv");
            System.out.println("trainingDataImport:" + trainingDataImport);

            // STEP 2: parse setup
            ParseSetupV3 trainingDataParseSetup = h2o.guessParseSetup(H2oApi.stringArrayToKeyArray(trainingDataImport.destinationFrames, FrameKeyV3.class));
            System.out.println("trainingDataParseSetup: " + trainingDataParseSetup);
            ParseSetupV3 testDataParseSetup = h2o.guessParseSetup(H2oApi.stringArrayToKeyArray(testDataImport.destinationFrames, FrameKeyV3.class));

            // STEP 3: correct the guesses
            String[] columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            trainingDataParseSetup.columnTypes = columnTypes;
            trainingDataParseSetup = h2o.guessParseSetup(trainingDataParseSetup);
            testDataParseSetup.columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            testDataParseSetup = h2o.guessParseSetup(testDataParseSetup);
            System.out.println("trainingDataParseSetup corrected: " + trainingDataParseSetup);

            // STEP 4: parse into columnar Frame
            ParseV3 trainingDataParseParams = new ParseV3();
            H2oApi.copyFields(trainingDataParseParams, trainingDataParseSetup);
            trainingDataParseParams.destinationFrame = H2oApi.stringToFrameKey(trainingDataParseSetup.destinationFrame + "");
            trainingDataParseParams.blocking = true;  // alternately, call h2o.waitForJobCompletion(trainingDataParseSetup.job)
            ParseV3 testDataParseParams = new ParseV3();
            H2oApi.copyFields(testDataParseParams, testDataParseSetup);
            testDataParseParams.destinationFrame = H2oApi.stringToFrameKey(testDataParseSetup.destinationFrame + "");
            testDataParseParams.blocking = true;

            ParseV3 trainingParseBody = h2o.parse(trainingDataParseParams);
            ParseV3 testParseBody = h2o.parse(testDataParseParams);
            System.out.println("trainingParseBody: " + trainingParseBody);

            // STEP 6: Train the model (NOTE: step 5 is polling, which we don't require because we specified blocking for the parse above)
            DRFParametersV3 drfParams = new DRFParametersV3();
            //drfParams.trainingFrame = H2oApi.stringToFrameKey(trainingDataParseSetup.destinationFrame + "");
            //drfParams.validationFrame = H2oApi.stringToFrameKey(testDataParseSetup.destinationFrame + "");
            drfParams.trainingFrame = trainingParseBody.destinationFrame;
            drfParams.validationFrame = testParseBody.destinationFrame;

            ColSpecifierV3 responseColumn = new ColSpecifierV3();
            responseColumn.columnName = "reaction";
            drfParams.responseColumn = responseColumn;

            System.out.println("About to train DRF. . .");
            DRFV3 drfBody = h2o.train_drf(drfParams);
            System.out.println("drfBody: " + drfBody);

            // STEP 7: poll for completion
            JobV3 job = h2o.waitForJobCompletion(drfBody.job.key);
            System.out.println("DRF build done.");

            // STEP 8: fetch the model
            ModelKeyV3 model_key = (ModelKeyV3)job.dest;
            ModelsV3 models = h2o.model(model_key);
            System.out.println("models: " + models);

            DRFModelV3 model = (DRFModelV3)models.models[0];
            System.out.println("new GBM model: " + model);
            // System.out.println("new GBM model: " + models.models[0]);
            assert model.getClass() == DRFModelV3.class;
            assert model.output.getClass() == DRFModelOutputV3.class;
            assert model.parameters.getClass() == DRFParametersV3.class;

            // STEP 9 predict!
            ModelMetricsListSchemaV3 predict_params = new ModelMetricsListSchemaV3();
            predict_params.model = model_key;
            predict_params.frame = drfParams.validationFrame;
            predict_params.predictionsFrame = H2oApi.stringToFrameKey("predictions");

            
            DRFV3 drf = h2o.validate_drf(drfParams);
            ModelMetricsListSchemaV3 predictions = h2o.predict(predict_params);
            //ModelMetricsListSchemaV3 predictions = h2o.predict(model_key, drfParams.validationFrame);
            System.out.println("predictions: " + predictions);
            //h2o.validate_drf();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildModel(SelfManagementEpisodeAnalysis ea) {
        // write the content of the episode analysis to file so that H2O can read it
        /*StringBuilder filePath = new StringBuilder(LEARNING_DATA_PATH).append("\\")
                .append(ea.associatedLearningAgentFactory.getAgentName())
                .append("-").append(ea.trialNo).append("-").append(ea.episodeNo).append(".csv");
        ea.writeToFile(filePath.toString());*/

        try {

            // STEP 0: init a session
            String sessionId = h2o.newSession().sessionKey;

            // STEP 1: import raw file
            ImportFilesV3 trainingDataImport;
            //trainingDataImport = h2o.importFiles(filePath);
            trainingDataImport = h2o.importFiles("D:\\mine\\odtu\\6\\tez\\codes\\data\\trainingData.csv");
            ImportFilesV3 testDataImport;
            testDataImport = h2o.importFiles("D:\\mine\\odtu\\6\\tez\\codes\\data\\testData.csv");
            System.out.println("trainingDataImport:" + trainingDataImport);

            // STEP 2: parse setup
            ParseSetupV3 trainingDataParseSetup = h2o.guessParseSetup(H2oApi.stringArrayToKeyArray(trainingDataImport.destinationFrames, FrameKeyV3.class));
            System.out.println("trainingDataParseSetup: " + trainingDataParseSetup);
            ParseSetupV3 testDataParseSetup = h2o.guessParseSetup(H2oApi.stringArrayToKeyArray(testDataImport.destinationFrames, FrameKeyV3.class));

            // STEP 3: correct the guesses
            String[] columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            trainingDataParseSetup.columnTypes = columnTypes;
            trainingDataParseSetup = h2o.guessParseSetup(trainingDataParseSetup);
            testDataParseSetup.columnTypes = columnTypes.clone();
            System.out.println("trainingDataParseSetup corrected: " + trainingDataParseSetup);

            // STEP 4: parse into columnar Frame
            ParseV3 trainingDataParseParams = new ParseV3();
            H2oApi.copyFields(trainingDataParseParams, trainingDataParseSetup);
            trainingDataParseParams.destinationFrame = H2oApi.stringToFrameKey(trainingDataParseSetup.destinationFrame + "");
            trainingDataParseParams.blocking = true;  // alternately, call h2o.waitForJobCompletion(trainingDataParseSetup.job)
            ParseV3 testDataParseParams = new ParseV3();

            H2oApi.copyFields(testDataParseParams, testDataParseSetup);
            testDataParseParams.destinationFrame = H2oApi.stringToFrameKey(testDataParseSetup.destinationFrame);
            testDataParseParams.blocking = true;

            ParseV3 trainingParseBody = h2o.parse(trainingDataParseParams);
            ParseV3 testParseBody = h2o.parse(testDataParseParams);
            System.out.println("trainingParseBody: " + trainingParseBody);


            // STEP 5: Split into test and train datasets
            /*String tmpVec = "tmp_" + UUID.randomUUID().toString();
            String splitExpr =
                    "(, " +
                            "  (tmp= " + tmpVec + " (h2o.runif arrhythmia.hex 906317))" +
                            "  (assign train " +
                            "    (rows arrhythmia.hex (<= " + tmpVec + " 0.75)))" +
                            "  (assign test " +
                            "    (rows arrhythmia.hex (> " + tmpVec + " 0.75)))" +
                            "  (rm " + tmpVec + "))";
            RapidsSchemaV3 rapidsParams = new RapidsSchemaV3();
            rapidsParams.sessionId = sessionId;
            rapidsParams.ast = splitExpr;
            h2o.rapidsExec(rapidsParams);
            */

            // STEP 6: Train the model (NOTE: step 5 is polling, which we don't require because we specified blocking for the parse above)
            GBMParametersV3 gbmParms = new GBMParametersV3();
            gbmParms.trainingFrame = H2oApi.stringToFrameKey(trainingDataParseSetup.destinationFrame);
            gbmParms.validationFrame = H2oApi.stringToFrameKey(testDataParseSetup.destinationFrame);

            ColSpecifierV3 responseColumn = new ColSpecifierV3();
            responseColumn.columnName = "reaction";
            gbmParms.responseColumn = responseColumn;
            gbmParms.minRows = 4;

            System.out.println("About to train GBM. . .");
            GBMV3 gbmBody = h2o.train_gbm(gbmParms);
            System.out.println("gbmBody: " + gbmBody);

            // STEP 7: poll for completion
            JobV3 job = h2o.waitForJobCompletion(gbmBody.job.key);
            System.out.println("GBM build done.");

            // STEP 8: fetch the model
            ModelKeyV3 model_key = (ModelKeyV3)job.dest;
            ModelsV3 models = h2o.model(model_key);
            System.out.println("models: " + models);
            GBMModelV3 model = (GBMModelV3)models.models[0];
            System.out.println("new GBM model: " + model);
            // System.out.println("new GBM model: " + models.models[0]);
            assert model.getClass() == GBMModelV3.class;
            assert model.output.getClass() == GBMModelOutputV3.class;
            assert model.parameters.getClass() == GBMParametersV3.class;

            // STEP 9 predict!
            ModelMetricsListSchemaV3 predict_params = new ModelMetricsListSchemaV3();
            predict_params.model = model_key;
            predict_params.frame = gbmParms.trainingFrame;
            predict_params.predictionsFrame = H2oApi.stringToFrameKey("predictions");

            ModelMetricsListSchemaV3 predictions = h2o.predict(predict_params);
            System.out.println("predictions: " + predictions);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateModel(SelfManagementEpisodeAnalysis ea) {

    }


    public Action classifiyState(State state) {
        if (model == null) {
            return null;
        }
        return null;
    }


}
