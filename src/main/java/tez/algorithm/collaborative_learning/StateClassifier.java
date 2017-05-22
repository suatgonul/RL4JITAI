package tez.algorithm.collaborative_learning;

import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import com.google.gson.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.domain.SelfManagementRewardFunction;
import tez.domain.SelfManagementState;
import tez.domain.action.SelfManagementAction;
import tez.experiment.performance.SelfManagementEligibilityEpisodeAnalysis;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;
import water.bindings.pojos.*;

import java.io.*;
import java.util.*;

import static tez.domain.SelfManagementDomainGenerator.*;

/**
 * Created by suat on 13-May-17.
 */
public class StateClassifier {
    private static final String LEARNING_DATA_FOLDER = "D:\\mine\\odtu\\6\\tez\\codes\\data\\";
    private static final String FILE_ALL_AGENTS_ACTIONS_FILE_NAME = "all_items.csv";
    private static final String FILE_CURRENT_STATE_NAME = "current_state.csv";
    private static final String FILE_PREDICTION_NAME = "prediction_result.csv";
    private static final String FRAME_PREDICTION_RESULT = "prediction_result_frame";

    private static Client client = Client.create();
    private static WebResource webResource = client.resource("http://localhost:54321/3/Frames/" + FRAME_PREDICTION_RESULT + "?column_offset=0&column_count=20");

    private static StateClassifier instance = null;
    private List<LearningAgentFactory> agents;
    private Domain domain;
    private SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
    private Map<HashableState, Map<String, Integer>> stateActionCounts = new HashMap<>();
    private ModelKeyV3 collaborativeModelKey = null;
    private File allAgentsActionsFile = null;
    private Map<String, ModelsV3> individualModels = null;
    private SMH2oApi h2o = new SMH2oApi();

    private StateClassifier() {

    }

    public static StateClassifier getInstance() {
        if (instance == null) {
            instance = new StateClassifier();
        }
        return instance;
    }

    public static void main(String[] args) {
        // POST method
        /*Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:54321/3/Predictions/models/DRF_model_okhttp_1494720676293_2057/frames/testData.hex");

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


        StateClassifier sc = new StateClassifier();
        sc.setDomain(domain);
        //sc.updateLearningModel(Arrays.asList(new SelfManagementEligibilityEpisodeAnalysis[]{ea}));
        //sc.guessAction(s5);
        sc.guessActionShortcut(s5);
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public void updateLearningModel(List<SelfManagementEpisodeAnalysis> ea) {
        updateStateActionCounts(ea);
        clearDataDirectory();
        allAgentsActionsFile = new File(LEARNING_DATA_FOLDER + FILE_ALL_AGENTS_ACTIONS_FILE_NAME);
        addHeadersToFile(new String[]{"Time", "DayType", "Location", "Activity", "PhoneUsage", "StateOfMind", "EmotionalStatus", "Action"}, allAgentsActionsFile);
        List<DataItem> dataItems = generateDataSetFromDataItems();
        addDataToFile(transformDataItemsToCSV(dataItems), allAgentsActionsFile);
        updateLearningModel();
    }

    private void updateStateActionCounts(List<SelfManagementEpisodeAnalysis> eaList) {
        for (int t = 0; t < eaList.size(); t++) {
            SelfManagementEpisodeAnalysis ea = eaList.get(t);
            for (int i = 0; i < ea.actionSequence.size(); i++) {
                // do not keep the state as data item if no intervention is delivered
                // i.e. keep only the states and actions where an intervention is delivered (as an indicator of preference)
                double r = ea.rewardSequence.get(i);
                String actionName = ea.actionSequence.get(i).actionName();

                if (r == SelfManagementRewardFunction.getRewardNoIntervention()) {
                    continue;
                } else if (r == SelfManagementRewardFunction.getRewardNonReactionToIntervention()) {
                    actionName = ACTION_NO_ACTION;
                }

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
    }

    private List<DataItem> generateDataSetFromDataItems() {
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

    private StringBuilder transformDataItemsToCSV(List<DataItem> dataItems) {
        StringBuilder sb = new StringBuilder();
        for (DataItem dataItem : dataItems) {
            sb.append(SelfManagementState.transformToCSV(dataItem.getState(), dataItem.getActionName()));
        }
        return sb;
    }

    private void updateLearningModel() {
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
            System.out.println("Trained");

            // STEP 6: poll for completion
            JobV3 job = h2o.waitForJobCompletion(drfBody.job.key);

            // STEP 7: fetch the collaborativeModel
            collaborativeModelKey = (ModelKeyV3) job.dest;

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to update the collaborative learning model", e);
        }
    }

    /*public Action guessAction(State state) {
        // if there isn't a learning model do not suggest any action
        if (collaborativeModelKey == null || allAgentsActionsFile == null) {
            return null;
        }

        String tempFilePath = LEARNING_DATA_FOLDER + UUID.randomUUID().toString() + FILE_CURRENT_STATE_NAME;
        File tempFile = new File(tempFilePath);
        String predictionFilePath = LEARNING_DATA_FOLDER + UUID.randomUUID().toString() + FILE_PREDICTION_NAME;
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

            Action guessedAction = readPredictionResult(predictionFile);
            return guessedAction;

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to classify state", e);
        } finally {
            removeFile(tempFile);
            removeFile(predictionFile);
        }
    }*/

    private static int guessCount = 0;
    private static int guessInt = 0;
    public Action guessActionShortcut(State state) {
        // if there isn't a learning model do not suggest any action
        if (collaborativeModelKey == null || allAgentsActionsFile == null) {
            return null;
        }

        String id = UUID.randomUUID().toString();
        String tempFilePath = LEARNING_DATA_FOLDER + id + FILE_CURRENT_STATE_NAME;
        File tempFile = new File(tempFilePath);

        try {
            addHeadersToFile(new String[]{"Time", "DayType", "Location", "Activity", "PhoneUsage", "StateOfMind", "EmotionalStatus"}, tempFile);
            StringBuilder stateAsCSV = SelfManagementState.transformToCSV(state);
            addDataToFile(stateAsCSV, tempFile);

            ImportFilesV3 stateDataImport = h2o.importFiles(tempFile.getAbsolutePath());
            ParseV3 stateDataParseParams = new ParseV3();
            stateDataParseParams.destinationFrame = SMH2oApi.stringToFrameKey(FRAME_PREDICTION_RESULT);
            stateDataParseParams.sourceFrames = SMH2oApi.stringArrayToKeyArray(stateDataImport.destinationFrames, FrameKeyV3.class);
            stateDataParseParams.parseType = ApiParseTypeValuesProvider.CSV;
            stateDataParseParams.numberColumns = 7;
            stateDataParseParams.separator = 44;
            stateDataParseParams.singleQuotes = false;
            stateDataParseParams.columnNames = new String[]{"Time", "DayType", "Location", "Activity", "PhoneUsage", "StateOfMind", "EmotionalStatus"};
            stateDataParseParams.columnTypes = new String[]{"Time", "Enum", "Enum", "Enum", "Enum", "Enum", "Enum"};
            stateDataParseParams.checkHeader = 1;
            stateDataParseParams.blocking = true;  // alternately, call h2o.waitForJobCompletion(stateDataParseSetup.job)
            stateDataParseParams.chunkSize = 1024 * 1024;

            h2o.parse(stateDataParseParams);

            ModelMetricsListSchemaV3 predict_params = new ModelMetricsListSchemaV3();
            predict_params.model = collaborativeModelKey;
            predict_params.frame = stateDataParseParams.destinationFrame;
            predict_params.predictionsFrame = SMH2oApi.stringToFrameKey(FRAME_PREDICTION_RESULT);

            ModelMetricsListSchemaV3 modelMetrics = h2o.predict(predict_params);
            ClientResponse response = webResource.accept("application/json")
                    .get(ClientResponse.class);

            String output = response.getEntity(String.class);
            Action guessedAction = parseActionFromPredictionResponse(output);
            guessCount++;
            //if(guessedAction.getName().equals(ACTION_INT_DELIVERY)) {
                System.out.println(guessCount + " - " + ++guessInt + ") guessed action: " + guessedAction.getName());
            //}
            return guessedAction;

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to classify state", e);
        } finally {
            removeFile(tempFile);
        }
    }

    private Action parseActionFromPredictionResponse (String response) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(response).getAsJsonObject();
        JsonArray columns = jsonObject.get("frames").getAsJsonArray().get(0).getAsJsonObject().get("columns").getAsJsonArray();
        for(int i=0; i<columns.size(); i++) {
            JsonObject column = columns.get(i).getAsJsonObject();
            String columnLabel = columns.get(i).getAsJsonObject().get("label").getAsString();
            if(columnLabel.equals(ACTION_INT_DELIVERY)) {
                double prediction = column.get("data").getAsJsonArray().get(0).getAsDouble();
                if(prediction > 0.5) {
                    return domain.getAction(ACTION_INT_DELIVERY);
                } else {
                    return domain.getAction(ACTION_NO_ACTION);
                }
            }
        }
        throw new CollaborativeLearningException("No action selected from prediction response");
    }

/*    private Action readPredictionResult(File predictionFile) {
        try {
            List<String> lines = new ArrayList<>();
            boolean predictionFileComplete = false;
            while (!predictionFileComplete) {
                if (!predictionFile.exists()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    lines = FileUtils.readLines(predictionFile);
                    if (lines.size() == 2) {
                        predictionFileComplete = true;
                    }
                }
            }

            String actionLabel = lines.get(1).split(",")[0].replace("\"", "");
            SelfManagementAction a = (SelfManagementAction) domain.getAction(actionLabel);
            a.setSelectedBy(SelfManagementAction.SelectedBy.STATE_CLASSIFIER);
            return a;

        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to read prediction results from the file", e);
        }
    }
*/
    private void removeFile(File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDataToFile(StringBuilder data, File file) {
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

    private void addHeadersToFile(String[] columnLabels, File file) {
        StringBuilder labelRow = new StringBuilder();
        for (int i = 0; i < columnLabels.length - 1; i++) {
            labelRow.append(columnLabels[i]).append(",");
        }
        labelRow.append(columnLabels[columnLabels.length - 1]).append("\n");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            IOUtils.write(labelRow.toString(), fos);
        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to write header row to file", e);
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void clearDataDirectory() {
        File dataDirectory = new File(LEARNING_DATA_FOLDER);
        try {
            FileUtils.cleanDirectory(dataDirectory);
        } catch (IOException e) {
            throw new CollaborativeLearningException("Failed to clear the data directory", e);
        }
    }
}
