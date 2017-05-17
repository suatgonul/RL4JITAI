package tez.algorithm.collaborative_learning;

import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.SelfManagementState;
import tez.experiment.performance.SelfManagementEligibilityEpisodeAnalysis;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;
import water.bindings.pojos.*;

import java.io.*;
import java.util.List;
import java.util.Map;

import static tez.domain.SelfManagementDomainGenerator.*;
import static tez.domain.SelfManagementDomainGenerator.ATT_STATE_OF_MIND;

/**
 * Created by suat on 13-May-17.
 */
public class StateClassifier {
    private static final String LEARNING_DATA_FOLDER = "D:\\mine\\odtu\\6\\tez\\codes\\data\\";
    private static final String FILE_ALL_AGENTS_ACTIONS_FILE_NAME = "all_items.csv";
    private static final String FILE_CURRENT_STATE_NAME = "current_state.csv";
    private static final String FILE_PREDICTION_NAME = "prediction_result.csv";

    private List<LearningAgentFactory> agents;
    private Domain domain;
    private ModelKeyV3 collaborativeModelKey = null;
    private File allAgentsActionsFile = null;
    private Map<String, ModelsV3> individualModels = null;
    private SMH2oApi h2o = new SMH2oApi();

    public StateClassifier(Domain domain) throws CollaborativeLearningException {
        this.domain = domain;
        allAgentsActionsFile = new File(LEARNING_DATA_FOLDER + FILE_ALL_AGENTS_ACTIONS_FILE_NAME);
        /*File parentFolder = allAgentsActionsFile.getParentFile();
        if (parentFolder != null) {
            parentFolder.mkdirs();
        }*/
        addHeadersToFile(new String[]{"Time", "DayType", "Location", "Activity", "PhoneUsage", "StateOfMind", "EmotionalStatus", "Action"}, allAgentsActionsFile);
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

        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        ObjectClass oc = new ObjectClass(domain, CLASS_STATE);
        State s = new MutableState();
        s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));
        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_DAY_TYPE, 0);
        o.setValue(ATT_LOCATION, 0);
        o.setValue(ATT_ACTIVITY_TIME, "15:40");
        o.setValue(ATT_ACTIVITY, 0);
        o.setValue(ATT_PHONE_USAGE, 0);
        o.setValue(ATT_EMOTIONAL_STATUS, 0);
        o.setValue(ATT_STATE_OF_MIND, 0);

        State s2 = s.copy();
        s2.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s2.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_LOCATION, 1);

        State s3 = s.copy();
        s3.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s3.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_DAY_TYPE, 1);

        State s4 = s.copy();
        s4.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s4.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_LOCATION, 2);

        State s5 = s.copy();
        s5.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s5.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_ACTIVITY, 3);

        SelfManagementEligibilityEpisodeAnalysis ea = new SelfManagementEligibilityEpisodeAnalysis(s);
        ea.stateSequence.add(s2);
        ea.stateSequence.add(s3);
        ea.stateSequence.add(s4);
        ea.rewardSequence.add(5.0);
        ea.rewardSequence.add(5.0);
        ea.rewardSequence.add(-2.0);
        ea.actionSequence.add(domain.getAction(ACTION_INT_DELIVERY).getGroundedAction());
        ea.actionSequence.add(domain.getAction(ACTION_NO_ACTION).getGroundedAction());
        ea.actionSequence.add(domain.getAction(ACTION_NO_ACTION).getGroundedAction());


        StateClassifier sc = new StateClassifier(domain);
        sc.updateLearningModel(ea);
        sc.guessAction(null, s5);
    }

    public void updateLearningModel(SelfManagementEpisodeAnalysis ea) throws CollaborativeLearningException {
        addDataToFile(ea.transformActionsWithPositiveRewardToCSV(), allAgentsActionsFile);
        updateLearningModel();
    }

    private void updateLearningModel() throws CollaborativeLearningException {
        try {
            h2o.deleteAllFrames();
            h2o.deleteAllKeys();
            h2o.deleteAllModels();

            // STEP 0: init a session
            String sessionId = h2o.newSession().sessionKey;

            // STEP 1: import raw file
            ImportFilesV3 trainingDataImport;
            trainingDataImport = h2o.importFiles(allAgentsActionsFile.getAbsolutePath());

            // STEP 2: parse setup
            ParseSetupV3 trainingDataParseSetup = h2o.guessParseSetup(SMH2oApi.stringArrayToKeyArray(trainingDataImport.destinationFrames, FrameKeyV3.class));

            // STEP 3: correct the guesses
            String[] columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            trainingDataParseSetup.columnTypes = columnTypes;
            trainingDataParseSetup = h2o.guessParseSetup(trainingDataParseSetup);

            // STEP 4: parse into columnar Frame
            ParseV3 trainingDataParseParams = new ParseV3();
            SMH2oApi.copyFields(trainingDataParseParams, trainingDataParseSetup);
            trainingDataParseParams.destinationFrame = SMH2oApi.stringToFrameKey(trainingDataParseSetup.destinationFrame + "");
            trainingDataParseParams.blocking = true;  // alternately, call h2o.waitForJobCompletion(trainingDataParseSetup.job)
            ParseV3 trainingParseBody = h2o.parse(trainingDataParseParams);

            // STEP 5: Train the collaborativeModel
            DRFParametersV3 drfParams = new DRFParametersV3();
            drfParams.trainingFrame = trainingParseBody.destinationFrame;

            ColSpecifierV3 responseColumn = new ColSpecifierV3();
            responseColumn.columnName = "Action";
            drfParams.responseColumn = responseColumn;

            DRFV3 drfBody = h2o.train_drf(drfParams);

            // STEP 6: poll for completion
            JobV3 job = h2o.waitForJobCompletion(drfBody.job.key);

            // STEP 7: fetch the collaborativeModel
            collaborativeModelKey = (ModelKeyV3) job.dest;

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to update the collaborative learning model", e);
        }
    }

    public Action guessAction(LearningAgentFactory learningAgent, State state) throws CollaborativeLearningException {
        String tempFilePath = LEARNING_DATA_FOLDER + (learningAgent == null ? "" : learningAgent.getAgentName()) + FILE_CURRENT_STATE_NAME;
        File tempFile = new File(tempFilePath);
        String predictionFilePath = LEARNING_DATA_FOLDER + (learningAgent == null ? "" : learningAgent.getAgentName()) + FILE_PREDICTION_NAME;
        File predictionFile = new File(predictionFilePath);

        try {
            addHeadersToFile(new String[]{"Time", "DayType", "Location", "Activity", "PhoneUsage", "StateOfMind", "EmotionalStatus"}, tempFile);
            StringBuilder stateAsCSV = SelfManagementState.transformToCSV(state);
            addDataToFile(stateAsCSV, tempFile);

            ImportFilesV3 stateDataImport = h2o.importFiles(tempFile.getAbsolutePath());

            ParseSetupV3 stateDataParseSetup = h2o.guessParseSetup(SMH2oApi.stringArrayToKeyArray(stateDataImport.destinationFrames, FrameKeyV3.class));
            String[] columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            stateDataParseSetup.columnTypes = columnTypes;
            stateDataParseSetup = h2o.guessParseSetup(stateDataParseSetup);
            ParseV3 stateDataParseParams = new ParseV3();
            SMH2oApi.copyFields(stateDataParseParams, stateDataParseSetup);
            stateDataParseParams.destinationFrame = SMH2oApi.stringToFrameKey(stateDataParseSetup.destinationFrame + "");
            stateDataParseParams.blocking = true;  // alternately, call h2o.waitForJobCompletion(stateDataParseSetup.job)
            h2o.parse(stateDataParseParams);

            ModelMetricsListSchemaV3 predict_params = new ModelMetricsListSchemaV3();
            predict_params.model = collaborativeModelKey;
            predict_params.frame = stateDataParseParams.destinationFrame;
            predict_params.predictionsFrame = SMH2oApi.stringToFrameKey("predictions");

            ModelMetricsListSchemaV3 modelMetrics = h2o.predict(predict_params);
            FramesV3 predictionParams = new FramesV3();
            predictionParams.column = "";
            predictionParams.columnCount = 3;
            predictionParams.frameId = modelMetrics.predictionsFrame;
            predictionParams.path = predictionFilePath;
            h2o.exportFrame(predictionParams);

            return readPredictionResult(predictionFile);

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to classify state", e);
        } finally {
            removeFile(tempFile);
            removeFile(predictionFile);
        }
    }

    private Action readPredictionResult(File predictionFile) throws CollaborativeLearningException {
        try {
            List<String> lines = FileUtils.readLines(predictionFile);
            String actionLabel = lines.get(1).split(",")[0].replace("\"", "");
            return domain.getAction(actionLabel);

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to read prediction results from the file", e);
        }
    }

    private void removeFile(File file) {
        file.delete();
    }

    private void addDataToFile(StringBuilder data, File file) throws CollaborativeLearningException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);

            bw.append(data.toString());

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to data to file", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addHeadersToFile(String[] columnLabels, File file) throws CollaborativeLearningException {
        StringBuilder labelRow = new StringBuilder();
        for (int i = 0; i < columnLabels.length - 1; i++) {
            labelRow.append(columnLabels[i]).append(",");
        }
        labelRow.append(columnLabels[columnLabels.length - 1]).append("\n");
        try {
            IOUtils.write(labelRow.toString(), new FileOutputStream(file));
        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to write header row to file", e);
        }
    }


    public Action classifiyState(State state) {
        if (collaborativeModelKey == null) {
            return null;
        }
        return null;
    }
}
