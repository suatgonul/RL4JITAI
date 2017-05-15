package tez.algorithm.collaborative_learning;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import org.apache.commons.io.IOUtils;
import tez.domain.SelfManagementState;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;
import water.bindings.pojos.*;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 13-May-17.
 */
public class StateClassifier {
    private static final String LEARNING_DATA_FOLDER = "D:\\mine\\odtu\\6\\tez\\codes\\data\\";
    private static final String FILE_ALL_AGENTS_ACTIONS_FILE_NAME = "all_items.csv";
    private static final String FILE_CURRENT_STATE_NAME = "current_state.csv"

    private List<LearningAgentFactory> agents;
    private DRFModelV3 collaborativeModel = null;
    private File allAgentsActionsFile = null;
    private Map<String, ModelsV3> individualModels = null;
    private H2oApi h2o = new H2oApi();

    public StateClassifier() throws CollaborativeLearningException {
        allAgentsActionsFile = new File(LEARNING_DATA_FOLDER + FILE_ALL_AGENTS_ACTIONS_FILE_NAME);
        File parentFolder = allAgentsActionsFile.getParentFile();
        if (parentFolder != null) {
            parentFolder.mkdirs();
        }
        addHeadersToFile(new String[]{"Time, DayType, Location, Activity, PhoneUsage, StateOfMind, EmotionalStatus, Action"}, allAgentsActionsFile);
    }

    public static void main(String[] args) throws CollaborativeLearningException {
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
        new StateClassifier().updateLearningModel();
    }

    public void appendDataItems(SelfManagementEpisodeAnalysis ea) throws CollaborativeLearningException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(allAgentsActionsFile, true);
            bw = new BufferedWriter(fw);

            StringBuilder transformedDataItems = ea.transformActionsWithPositiveRewardToCSV();
            bw.append(transformedDataItems.toString());

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to append episode analysis to all agents' actions file", e);
        }

        updateLearningModel();
    }

    private void updateLearningModel() {
        try {
            h2o.deleteAllFrames();
            h2o.deleteAllKeys();
            h2o.deleteAllModels();

            // STEP 0: init a session
            //String sessionId = h2o.newSession().sessionKey;

            // STEP 1: import raw file
            ImportFilesV3 trainingDataImport;
            trainingDataImport = h2o.importFiles(allAgentsActionsFile.getAbsolutePath());

            // STEP 2: parse setup
            ParseSetupV3 trainingDataParseSetup = h2o.guessParseSetup(H2oApi.stringArrayToKeyArray(trainingDataImport.destinationFrames, FrameKeyV3.class));

            // STEP 3: correct the guesses
            String[] columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            trainingDataParseSetup.columnTypes = columnTypes;
            trainingDataParseSetup = h2o.guessParseSetup(trainingDataParseSetup);

            // STEP 4: parse into columnar Frame
            ParseV3 trainingDataParseParams = new ParseV3();
            H2oApi.copyFields(trainingDataParseParams, trainingDataParseSetup);
            trainingDataParseParams.destinationFrame = H2oApi.stringToFrameKey(trainingDataParseSetup.destinationFrame + "");
            trainingDataParseParams.blocking = true;  // alternately, call h2o.waitForJobCompletion(trainingDataParseSetup.job)
            ParseV3 trainingParseBody = h2o.parse(trainingDataParseParams);

            // STEP 5: Train the collaborativeModel
            DRFParametersV3 drfParams = new DRFParametersV3();
            drfParams.trainingFrame = trainingParseBody.destinationFrame;

            ColSpecifierV3 responseColumn = new ColSpecifierV3();
            responseColumn.columnName = "reaction";
            drfParams.responseColumn = responseColumn;

            DRFV3 drfBody = h2o.train_drf(drfParams);

            // STEP 6: poll for completion
            JobV3 job = h2o.waitForJobCompletion(drfBody.job.key);

            // STEP 7: fetch the collaborativeModel
            ModelKeyV3 model_key = (ModelKeyV3) job.dest;
            ModelsV3 models = h2o.model(model_key);

            collaborativeModel = (DRFModelV3) models.models[0];

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Action guessAction(LearningAgentFactory learningAgent, State state) throws CollaborativeLearningException {
        String tempFilePath = learningAgent == null ? "" : learningAgent.getAgentName() + FILE_CURRENT_STATE_NAME;
        File tempFile = new File(tempFilePath);
        StringBuilder stateAsCSV = SelfManagementState.transformToCSV(state);

        addHeadersToFile(new String[]{"Time, DayType, Location, Activity, PhoneUsage, StateOfMind, EmotionalStatus"}, tempFile);

            FileWriter fw = new FileWriter(tempFile, true);;
            BufferedWriter bw = null;
            try {
                fw =
                bw = new BufferedWriter(fw);

                StringBuilder transformedDataItems = ea.transformActionsWithPositiveRewardToCSV();
                bw.append(transformedDataItems.toString());

            } catch (IOException e) {
                throw new CollaborativeLearningException("Failed to append episode analysis to all agents' actions file", e);
            }


        try {
            ImportFilesV3 trainingDataImport = h2o.importFiles(allAgentsActionsFile.getAbsolutePath());

            // STEP 2: parse setup
            ParseSetupV3 trainingDataParseSetup = h2o.guessParseSetup(H2oApi.stringArrayToKeyArray(trainingDataImport.destinationFrames, FrameKeyV3.class));

            // STEP 3: correct the guesses
            String[] columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            trainingDataParseSetup.columnTypes = columnTypes;
            trainingDataParseSetup = h2o.guessParseSetup(trainingDataParseSetup);

            // STEP 4: parse into columnar Frame
            ParseV3 trainingDataParseParams = new ParseV3();
            H2oApi.copyFields(trainingDataParseParams, trainingDataParseSetup);
            trainingDataParseParams.destinationFrame = H2oApi.stringToFrameKey(trainingDataParseSetup.destinationFrame + "");
            trainingDataParseParams.blocking = true;  // alternately, call h2o.waitForJobCompletion(trainingDataParseSetup.job)
        } catch (IOException e) {
            e.printStackTrace();
        }


        // STEP 9 predict!
        ModelMetricsListSchemaV3 predict_params = new ModelMetricsListSchemaV3();
        predict_params.model = model_key;
        predict_params.frame = drfParams.validationFrame;
        predict_params.predictionsFrame = H2oApi.stringToFrameKey("predictions");

        ModelMetricsListSchemaV3 predictions = h2o.predict(predict_params);
    }


    private void addHeadersToFile(String[] columnLabels, File file) throws CollaborativeLearningException {
        StringBuilder labelRow = new StringBuilder();
        for (int i = 0; i < columnLabels.length - 1; i++) {
            labelRow.append(columnLabels[i]).append(",");
        }
        labelRow.append(columnLabels[columnLabels.length - 1]);
        try {
            IOUtils.write(labelRow.toString(), new FileOutputStream(file));
        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to write header row to file", e);
        }
    }


    public Action classifiyState(State state) {
        if (collaborativeModel == null) {
            return null;
        }
        return null;
    }


}
