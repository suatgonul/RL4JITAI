package tez.algorithm.collaborative_learning;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import okhttp3.OkHttpClient;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.*;
import water.bindings.pojos.*;
import water.bindings.proxies.retrofit.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * Created by suat on 14-May-17.
 */
public class SMH2oApi {
    public static String DEFAULT_URL = "http://localhost:54321/";

    public SMH2oApi() {
        this(DEFAULT_URL);
    }
    public SMH2oApi(String url) {
        _url = url;
    }

    /**
     * Continuously poll server for the status of the given job, until it completes.
     *   @param jobKey job to query
     *   @return the finished job
     */
    public JobV3 waitForJobCompletion(JobKeyV3 jobKey) {
        return waitForJobCompletion(keyToString(jobKey));
    }
    public JobV3 waitForJobCompletion(String jobId) {
        Jobs jobService = getService(Jobs.class);
        Response<JobsV3> jobsResponse = null;
        int retries = 3;
        JobsV3 jobs = null;
        do {
            try {
                Thread.sleep(pollInterval_ms);
                jobsResponse = jobService.fetch(jobId).execute();
            } catch (IOException e) {
                System.err.println("Caught exception: " + e);
            } catch (InterruptedException e) { /* pass */ }
            if (jobsResponse == null || !jobsResponse.isSuccessful())
                if (retries-- > 0)
                    continue;
                else
                    throw new RuntimeException("/3/Jobs/" + jobId + " failed 3 times.");
            jobs = jobsResponse.body();
            if (jobs.jobs == null || jobs.jobs.length != 1)
                throw new RuntimeException("Failed to find Job: " + jobId);
        } while (jobs != null && jobs.jobs[0].status.equals("RUNNING"));
        return jobs == null? null : jobs.jobs[0];
    }

    /**
     * Import raw data files into a single-column H2O Frame.
     */
    public ImportFilesV3 importFiles(String path) throws IOException {
        ImportFiles s = getService(ImportFiles.class);
        return s.importFiles(path).execute().body();
    }

    /**
     * Guess the parameters for parsing raw byte-oriented data into an H2O Frame.
     */
    public ParseSetupV3 guessParseSetup(FrameKeyV3[] sourceFrames) throws IOException {
        ParseSetup s = getService(ParseSetup.class);
        return s.guessSetup(keyArrayToStringArray(sourceFrames)).execute().body();
    }
    public ParseSetupV3 guessParseSetup(ParseSetupV3 params) throws IOException {
        ParseSetup s = getService(ParseSetup.class);
        return s.guessSetup(
                keyArrayToStringArray(params.sourceFrames),
                params.parseType,
                params.separator,
                params.singleQuotes,
                params.checkHeader,
                params.columnNames,
                params.columnTypes,
                params.naStrings,
                params.columnNameFilter,
                params.columnOffset,
                params.columnCount,
                params.totalFilteredColumnCount,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Parse a raw byte-oriented Frame into a useful columnar data Frame.
     */
    public ParseV3 parse(ParseV3 params) throws IOException {
        Parse s = getService(Parse.class);
        return s.parse(
                keyToString(params.destinationFrame),
                keyArrayToStringArray(params.sourceFrames),
                params.parseType,
                params.separator,
                params.singleQuotes,
                params.checkHeader,
                params.numberColumns,
                params.columnNames,
                params.columnTypes,
                params.domains,
                params.naStrings,
                params.chunkSize,
                params.deleteOnDone,
                params.blocking,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Parse a raw byte-oriented Frame into a useful columnar data Frame.
     */
    public JobV3 parseSvmLight(FrameKeyV3[] sourceFrames) throws IOException {
        ParseSVMLight s = getService(ParseSVMLight.class);
        return s.parseSVMLight(keyArrayToStringArray(sourceFrames)).execute().body();
    }
    public JobV3 parseSvmLight(FrameKeyV3 destinationFrame, FrameKeyV3[] sourceFrames) throws IOException {
        ParseSVMLight s = getService(ParseSVMLight.class);
        return s.parseSVMLight(keyToString(destinationFrame), keyArrayToStringArray(sourceFrames), "").execute().body();
    }
    public JobV3 parseSvmLight(FrameKeyV3 destinationFrame, FrameKeyV3[] sourceFrames, String _excludeFields) throws IOException {
        ParseSVMLight s = getService(ParseSVMLight.class);
        return s.parseSVMLight(keyToString(destinationFrame), keyArrayToStringArray(sourceFrames), _excludeFields).execute().body();
    }

    /**
     * Determine the status of the nodes in the H2O cloud.
     */
    public CloudV3 cloudStatus() throws IOException {
        Cloud s = getService(Cloud.class);
        return s.status().execute().body();
    }
    public CloudV3 cloudStatus(boolean skipTicks) throws IOException {
        Cloud s = getService(Cloud.class);
        return s.status(skipTicks, "").execute().body();
    }
    public CloudV3 cloudStatus(boolean skipTicks, String _excludeFields) throws IOException {
        Cloud s = getService(Cloud.class);
        return s.status(skipTicks, _excludeFields).execute().body();
    }

    /**
     * Determine the status of the nodes in the H2O cloud.
     */
    public CloudV3 cloudStatusMinimal() throws IOException {
        Cloud s = getService(Cloud.class);
        return s.head().execute().body();
    }
    public CloudV3 cloudStatusMinimal(boolean skipTicks) throws IOException {
        Cloud s = getService(Cloud.class);
        return s.head(skipTicks, "").execute().body();
    }
    public CloudV3 cloudStatusMinimal(boolean skipTicks, String _excludeFields) throws IOException {
        Cloud s = getService(Cloud.class);
        return s.head(skipTicks, _excludeFields).execute().body();
    }

    /**
     * Get a list of all the H2O Jobs (long-running actions).
     */
    public JobsV3 jobs() throws IOException {
        Jobs s = getService(Jobs.class);
        return s.list().execute().body();
    }
    public JobsV3 jobs(JobKeyV3 jobId) throws IOException {
        Jobs s = getService(Jobs.class);
        return s.list(keyToString(jobId), "").execute().body();
    }
    public JobsV3 jobs(JobKeyV3 jobId, String _excludeFields) throws IOException {
        Jobs s = getService(Jobs.class);
        return s.list(keyToString(jobId), _excludeFields).execute().body();
    }

    /**
     * Debugging tool that provides information on current communication between nodes.
     */
    public TimelineV3 timeline() throws IOException {
        Timeline s = getService(Timeline.class);
        return s.fetch().execute().body();
    }
    public TimelineV3 timeline(String _excludeFields) throws IOException {
        Timeline s = getService(Timeline.class);
        return s.fetch(_excludeFields).execute().body();
    }

    /**
     * Report real-time profiling information for all nodes (sorted, aggregated stack traces).
     */
    public ProfilerV3 profiler(int depth) throws IOException {
        Profiler s = getService(Profiler.class);
        return s.fetch(depth).execute().body();
    }
    public ProfilerV3 profiler(int depth, String _excludeFields) throws IOException {
        Profiler s = getService(Profiler.class);
        return s.fetch(depth, _excludeFields).execute().body();
    }

    /**
     * Report stack traces for all threads on all nodes.
     */
    public JStackV3 stacktraces() throws IOException {
        JStack s = getService(JStack.class);
        return s.fetch().execute().body();
    }
    public JStackV3 stacktraces(String _excludeFields) throws IOException {
        JStack s = getService(JStack.class);
        return s.fetch(_excludeFields).execute().body();
    }

    /**
     * Run a network test to measure the performance of the cluster interconnect.
     */
    public NetworkTestV3 testNetwork() throws IOException {
        NetworkTest s = getService(NetworkTest.class);
        return s.fetch().execute().body();
    }
    public NetworkTestV3 testNetwork(String _excludeFields) throws IOException {
        NetworkTest s = getService(NetworkTest.class);
        return s.fetch(_excludeFields).execute().body();
    }

    /**
     * Unlock all keys in the H2O distributed K/V store, to attempt to recover from a crash.
     */
    public UnlockKeysV3 unlockAllKeys() throws IOException {
        UnlockKeys s = getService(UnlockKeys.class);
        return s.unlock().execute().body();
    }
    public UnlockKeysV3 unlockAllKeys(String _excludeFields) throws IOException {
        UnlockKeys s = getService(UnlockKeys.class);
        return s.unlock(_excludeFields).execute().body();
    }

    /**
     * Shut down the cluster.
     */
    public ShutdownV3 shutdownCluster() throws IOException {
        Shutdown s = getService(Shutdown.class);
        return s.shutdown().execute().body();
    }
    public ShutdownV3 shutdownCluster(String _excludeFields) throws IOException {
        Shutdown s = getService(Shutdown.class);
        return s.shutdown(_excludeFields).execute().body();
    }

    /**
     * Return information about this H2O cluster.
     */
    public AboutV3 about() throws IOException {
        About s = getService(About.class);
        return s.get().execute().body();
    }
    public AboutV3 about(String _excludeFields) throws IOException {
        About s = getService(About.class);
        return s.get(_excludeFields).execute().body();
    }

    /**
     * Return the list of (almost) all REST API endpoints.
     */
    public MetadataV3 endpoints() throws IOException {
        Metadata s = getService(Metadata.class);
        return s.listRoutes().execute().body();
    }
    public MetadataV3 endpoints(MetadataV3 params) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.listRoutes(
                params.num,
                params.httpMethod,
                params.path,
                params.classname,
                params.schemaname,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the REST API endpoint metadata, including documentation, for the endpoint specified by path or index.
     */
    public MetadataV3 endpoint(String path) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.fetchRoute(path).execute().body();
    }
    public MetadataV3 endpoint(MetadataV3 params) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.fetchRoute(
                params.path,
                params.num,
                params.httpMethod,
                params.classname,
                params.schemaname,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the REST API schema metadata for specified schema class.
     */
    public MetadataV3 schemaForClass(String classname) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.fetchSchemaMetadataByClass(classname).execute().body();
    }
    public MetadataV3 schemaForClass(MetadataV3 params) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.fetchSchemaMetadataByClass(
                params.classname,
                params.num,
                params.httpMethod,
                params.path,
                params.schemaname,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the REST API schema metadata for specified schema.
     */
    public MetadataV3 schema(String schemaname) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.fetchSchemaMetadata(schemaname).execute().body();
    }
    public MetadataV3 schema(MetadataV3 params) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.fetchSchemaMetadata(
                params.schemaname,
                params.num,
                params.httpMethod,
                params.path,
                params.classname,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return list of all REST API schemas.
     */
    public MetadataV3 schemas() throws IOException {
        Metadata s = getService(Metadata.class);
        return s.listSchemas().execute().body();
    }
    public MetadataV3 schemas(MetadataV3 params) throws IOException {
        Metadata s = getService(Metadata.class);
        return s.listSchemas(
                params.num,
                params.httpMethod,
                params.path,
                params.classname,
                params.schemaname,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Typeahead hander for filename completion.
     */
    public TypeaheadV3 typeaheadFileSuggestions(String src) throws IOException {
        Typeahead s = getService(Typeahead.class);
        return s.files(src).execute().body();
    }
    public TypeaheadV3 typeaheadFileSuggestions(String src, int limit) throws IOException {
        Typeahead s = getService(Typeahead.class);
        return s.files(src, limit, "").execute().body();
    }
    public TypeaheadV3 typeaheadFileSuggestions(String src, int limit, String _excludeFields) throws IOException {
        Typeahead s = getService(Typeahead.class);
        return s.files(src, limit, _excludeFields).execute().body();
    }

    /**
     * Get the status of the given H2O Job (long-running action).
     */
    public JobsV3 job(JobKeyV3 jobId) throws IOException {
        Jobs s = getService(Jobs.class);
        return s.fetch(keyToString(jobId)).execute().body();
    }
    public JobsV3 job(JobKeyV3 jobId, String _excludeFields) throws IOException {
        Jobs s = getService(Jobs.class);
        return s.fetch(keyToString(jobId), _excludeFields).execute().body();
    }

    /**
     * Cancel a running job.
     */
    public JobsV3 cancelJob(JobKeyV3 jobId) throws IOException {
        Jobs s = getService(Jobs.class);
        return s.cancel(keyToString(jobId)).execute().body();
    }
    public JobsV3 cancelJob(JobKeyV3 jobId, String _excludeFields) throws IOException {
        Jobs s = getService(Jobs.class);
        return s.cancel(keyToString(jobId), _excludeFields).execute().body();
    }

    /**
     * Find a value within a Frame.
     */
    public FindV3 findInFrame(FrameV3 key, long row) throws IOException {
        Find s = getService(Find.class);
        return s.find(key, row).execute().body();
    }
    public FindV3 findInFrame(FindV3 params) throws IOException {
        Find s = getService(Find.class);
        return s.find(
                params.key,
                params.column,
                params.row,
                params.match,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Export a Frame to the given path with optional overwrite.
     */
    public FramesV3 exportFrame(FrameKeyV3 frameId) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.export(keyToString(frameId)).execute().body();
    }
    public FramesV3 exportFrame(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.export(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the summary metrics for a column, e.g. min, max, mean, sigma, percentiles, etc.
     */
    public FramesV3 frameColumnSummary(FrameKeyV3 frameId, String column) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.columnSummary(keyToString(frameId), column).execute().body();
    }
    public FramesV3 frameColumnSummary(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.columnSummary(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the domains for the specified categorical column ("null" if the column is not a categorical).
     */
    public FramesV3 frameColumnDomain(FrameKeyV3 frameId, String column) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.columnDomain(keyToString(frameId), column).execute().body();
    }
    public FramesV3 frameColumnDomain(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.columnDomain(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the specified column from a Frame.
     */
    public FramesV3 frameColumn(FrameKeyV3 frameId, String column) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.column(keyToString(frameId), column).execute().body();
    }
    public FramesV3 frameColumn(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.column(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return all the columns from a Frame.
     */
    public FramesV3 frameColumns(FrameKeyV3 frameId) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.columns(keyToString(frameId)).execute().body();
    }
    public FramesV3 frameColumns(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.columns(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return a Frame, including the histograms, after forcing computation of rollups.
     */
    public FramesV3 frameSummary(FrameKeyV3 frameId) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.summary(keyToString(frameId)).execute().body();
    }
    public FramesV3 frameSummary(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.summary(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the specified Frame.
     */
    public FramesV3 frame(FrameKeyV3 frameId) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.fetch(keyToString(frameId)).execute().body();
    }
    public FramesV3 frame(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.fetch(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return all Frames in the H2O distributed K/V store.
     */
    public FramesV3 frames() throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.list().execute().body();
    }
    public FramesV3 frames(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.list(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Delete the specified Frame from the H2O distributed K/V store.
     */
    public FramesV3 deleteFrame(FrameKeyV3 frameId) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.delete(keyToString(frameId)).execute().body();
    }
    public FramesV3 deleteFrame(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.delete(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Delete all Frames from the H2O distributed K/V store.
     */
    public FramesV3 deleteAllFrames() throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.deleteAll().execute().body();
    }
    public FramesV3 deleteAllFrames(FramesV3 params) throws IOException {
        SMFrames s = getService(SMFrames.class);
        return s.deleteAll(
                keyToString(params.frameId),
                params.column,
                params.rowOffset,
                params.rowCount,
                params.columnOffset,
                params.columnCount,
                params.findCompatibleModels,
                params.path,
                params.force,
                params.numParts,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the specified Model from the H2O distributed K/V store, optionally with the list of compatible Frames.
     */
    public ModelsV3 model(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.fetch(keyToString(modelId)).execute().body();
    }
    public ModelsV3 model(ModelsV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.fetch(
                keyToString(params.modelId),
                params.preview,
                params.findCompatibleFrames,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return all Models from the H2O distributed K/V store.
     */
    public ModelsV3 models() throws IOException {
        Models s = getService(Models.class);
        return s.list().execute().body();
    }
    public ModelsV3 models(ModelsV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.list(
                keyToString(params.modelId),
                params.preview,
                params.findCompatibleFrames,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Delete the specified Model from the H2O distributed K/V store.
     */
    public ModelsV3 deleteModel(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.delete(keyToString(modelId)).execute().body();
    }
    public ModelsV3 deleteModel(ModelsV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.delete(
                keyToString(params.modelId),
                params.preview,
                params.findCompatibleFrames,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Delete all Models from the H2O distributed K/V store.
     */
    public ModelsV3 deleteAllModels() throws IOException {
        Models s = getService(Models.class);
        return s.deleteAll().execute().body();
    }
    public ModelsV3 deleteAllModels(ModelsV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.deleteAll(
                keyToString(params.modelId),
                params.preview,
                params.findCompatibleFrames,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return potentially abridged model suitable for viewing in a browser (currently only used for java model code).
     */
    public StreamingSchema modelPreview(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.fetchPreview(keyToString(modelId)).execute().body();
    }
    public StreamingSchema modelPreview(ModelsV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.fetchPreview(
                keyToString(params.modelId),
                params.preview,
                params.findCompatibleFrames,
                params._excludeFields
        ).execute().body();
    }

    /**
     * [DEPRECATED] Return the stream containing model implementation in Java code.
     */
    public StreamingSchema modelJavaCode(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.fetchJavaCode(keyToString(modelId)).execute().body();
    }
    public StreamingSchema modelJavaCode(ModelsV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.fetchJavaCode(
                keyToString(params.modelId),
                params.preview,
                params.findCompatibleFrames,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the model in the MOJO format. This format can then be interpreted by gen_model.jar in order to perform
     * prediction / scoring. Currently works for GBM and DRF algos only.
     */
    public StreamingSchema modelMojo(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.fetchMojo(keyToString(modelId)).execute().body();
    }
    public StreamingSchema modelMojo(ModelsV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.fetchMojo(
                keyToString(params.modelId),
                params.preview,
                params.findCompatibleFrames,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Create data for partial dependence plot(s) for the specified model and frame.
     */
    public JobV3 makePDP() throws IOException {
        PartialDependence s = getService(PartialDependence.class);
        return s.makePartialDependence().execute().body();
    }
    public JobV3 makePDP(PartialDependenceV3 params) throws IOException {
        PartialDependence s = getService(PartialDependence.class);
        return s.makePartialDependence(
                keyToString(params.modelId),
                keyToString(params.frameId),
                params.cols,
                params.nbins,
                keyToString(params.destinationKey)
        ).execute().body();
    }

    /**
     * Fetch partial dependence data.
     */
    public PartialDependenceV3 fetchPDP(String name) throws IOException {
        PartialDependence s = getService(PartialDependence.class);
        return s.fetchPartialDependence(name).execute().body();
    }
    public PartialDependenceV3 fetchPDP(String name, String type, String url) throws IOException {
        PartialDependence s = getService(PartialDependence.class);
        return s.fetchPartialDependence(name, type, url).execute().body();
    }

    /**
     * Import given binary model into H2O.
     */
    public ModelsV3 importModel(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.importModel(keyToString(modelId)).execute().body();
    }
    public ModelsV3 importModel(ModelImportV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.importModel(
                keyToString(params.modelId),
                params.dir,
                params.force,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Export given model.
     */
    public ModelExportV3 exportModel(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.exportModel(keyToString(modelId)).execute().body();
    }
    public ModelExportV3 exportModel(ModelExportV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.exportModel(
                keyToString(params.modelId),
                params.dir,
                params.force,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Export given model as Mojo.
     */
    public ModelExportV3 exportMojo(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.exportMojo(keyToString(modelId)).execute().body();
    }
    public ModelExportV3 exportMojo(ModelExportV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.exportMojo(
                keyToString(params.modelId),
                params.dir,
                params.force,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Export given model details in json format.
     */
    public ModelExportV3 exportModelDetails(ModelKeyV3 modelId) throws IOException {
        Models s = getService(Models.class);
        return s.exportModelDetails(keyToString(modelId)).execute().body();
    }
    public ModelExportV3 exportModelDetails(ModelExportV3 params) throws IOException {
        Models s = getService(Models.class);
        return s.exportModelDetails(
                keyToString(params.modelId),
                params.dir,
                params.force,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the specified grid search result.
     */
    public GridSchemaV99 grid(GridKeyV3 gridId) throws IOException {
        Grids s = getService(Grids.class);
        return s.fetch(keyToString(gridId)).execute().body();
    }
    public GridSchemaV99 grid(GridSchemaV99 params) throws IOException {
        Grids s = getService(Grids.class);
        return s.fetch(
                keyToString(params.gridId),
                params.sortBy,
                params.decreasing,
                keyArrayToStringArray(params.modelIds)
        ).execute().body();
    }

    /**
     * Return all grids from H2O distributed K/V store.
     */
    public GridsV99 grids() throws IOException {
        Grids s = getService(Grids.class);
        return s.list().execute().body();
    }

    /**
     * Return a new unique model_id for the specified algorithm.
     */
    public ModelIdV3 newModelId(String algo) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.calcModelId(algo).execute().body();
    }
    public ModelIdV3 newModelId(String algo, String _excludeFields) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.calcModelId(algo, _excludeFields).execute().body();
    }

    /**
     * Return the Model Builder metadata for the specified algorithm.
     */
    public ModelBuildersV3 modelBuilder(String algo) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.fetch(algo).execute().body();
    }
    public ModelBuildersV3 modelBuilder(String algo, String _excludeFields) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.fetch(algo, _excludeFields).execute().body();
    }

    /**
     * Return the Model Builder metadata for all available algorithms.
     */
    public ModelBuildersV3 modelBuilders() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.list().execute().body();
    }
    public ModelBuildersV3 modelBuilders(String algo) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.list(algo, "").execute().body();
    }
    public ModelBuildersV3 modelBuilders(String algo, String _excludeFields) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.list(algo, _excludeFields).execute().body();
    }

    /**
     * Return the saved scoring metrics for the specified Model and Frame.
     */
    public ModelMetricsListSchemaV3 _mmFetch1(ModelKeyV3 model, FrameKeyV3 frame) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.fetch(keyToString(model), keyToString(frame)).execute().body();
    }
    public ModelMetricsListSchemaV3 _mmFetch1(ModelMetricsListSchemaV3 params) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.fetch(
                keyToString(params.model),
                keyToString(params.frame),
                keyToString(params.predictionsFrame),
                keyToString(params.deviancesFrame),
                params.reconstructionError,
                params.reconstructionErrorPerFeature,
                params.deepFeaturesHiddenLayer,
                params.reconstructTrain,
                params.projectArchetypes,
                params.reverseTransform,
                params.leafNodeAssignment,
                params.exemplarIndex,
                params.deviances,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the saved scoring metrics for the specified Model and Frame.
     */
    public ModelMetricsListSchemaV3 _mmDelete1(ModelKeyV3 model, FrameKeyV3 frame) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.delete(keyToString(model), keyToString(frame)).execute().body();
    }
    public ModelMetricsListSchemaV3 _mmDelete1(ModelMetricsListSchemaV3 params) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.delete(
                keyToString(params.model),
                keyToString(params.frame),
                keyToString(params.predictionsFrame),
                keyToString(params.deviancesFrame),
                params.reconstructionError,
                params.reconstructionErrorPerFeature,
                params.deepFeaturesHiddenLayer,
                params.reconstructTrain,
                params.projectArchetypes,
                params.reverseTransform,
                params.leafNodeAssignment,
                params.exemplarIndex,
                params.deviances,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return the scoring metrics for the specified Frame with the specified Model.  If the Frame has already been scored
     * with the Model then cached results will be returned; otherwise predictions for all rows in the Frame will be
     * generated and the metrics will be returned.
     */
    public ModelMetricsListSchemaV3 score(ModelKeyV3 model, FrameKeyV3 frame) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.score(keyToString(model), keyToString(frame)).execute().body();
    }
    public ModelMetricsListSchemaV3 score(ModelMetricsListSchemaV3 params) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.score(
                keyToString(params.model),
                keyToString(params.frame),
                keyToString(params.predictionsFrame),
                keyToString(params.deviancesFrame),
                params.reconstructionError,
                params.reconstructionErrorPerFeature,
                params.deepFeaturesHiddenLayer,
                params.reconstructTrain,
                params.projectArchetypes,
                params.reverseTransform,
                params.leafNodeAssignment,
                params.exemplarIndex,
                params.deviances,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Score (generate predictions) for the specified Frame with the specified Model.  Both the Frame of predictions and
     * the metrics will be returned.
     */
    public ModelMetricsListSchemaV3 predict(ModelKeyV3 model, FrameKeyV3 frame) throws IOException {
        Predictions s = getService(Predictions.class);
        return s.predict(keyToString(model), keyToString(frame)).execute().body();
    }
    public ModelMetricsListSchemaV3 predict(ModelMetricsListSchemaV3 params) throws IOException {
        Predictions s = getService(Predictions.class);
        return s.predict(
                keyToString(params.model),
                keyToString(params.frame),
                keyToString(params.predictionsFrame),
                keyToString(params.deviancesFrame),
                params.reconstructionError,
                params.reconstructionErrorPerFeature,
                params.deepFeaturesHiddenLayer,
                params.reconstructTrain,
                params.projectArchetypes,
                params.reverseTransform,
                params.leafNodeAssignment,
                params.exemplarIndex,
                params.deviances,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Score (generate predictions) for the specified Frame with the specified Model.  Both the Frame of predictions and
     * the metrics will be returned.
     */
    public JobV3 predict_async(ModelKeyV3 model, FrameKeyV3 frame) throws IOException {
        Predictions s = getService(Predictions.class);
        return s.predictAsync(keyToString(model), keyToString(frame)).execute().body();
    }
    public JobV3 predict_async(ModelMetricsListSchemaV3 params) throws IOException {
        Predictions s = getService(Predictions.class);
        return s.predictAsync(
                keyToString(params.model),
                keyToString(params.frame),
                keyToString(params.predictionsFrame),
                keyToString(params.deviancesFrame),
                params.reconstructionError,
                params.reconstructionErrorPerFeature,
                params.deepFeaturesHiddenLayer,
                params.reconstructTrain,
                params.projectArchetypes,
                params.reverseTransform,
                params.leafNodeAssignment,
                params.exemplarIndex,
                params.deviances,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Create a ModelMetrics object from the predicted and actual values, and a domain for classification problems or a
     * distribution family for regression problems.
     */
    public ModelMetricsMakerSchemaV3 makeMetrics(String predictionsFrame, String actualsFrame) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.make(predictionsFrame, actualsFrame).execute().body();
    }
    public ModelMetricsMakerSchemaV3 makeMetrics(ModelMetricsMakerSchemaV3 params) throws IOException {
        ModelMetrics s = getService(ModelMetrics.class);
        return s.make(
                params.predictionsFrame,
                params.actualsFrame,
                params.domain,
                params.distribution
        ).execute().body();
    }

    /**
     * Return a CPU usage snapshot of all cores of all nodes in the H2O cluster.
     */
    public WaterMeterCpuTicksV3 waterMeterCpuTicks(int nodeidx) throws IOException {
        WaterMeterCpuTicks s = getService(WaterMeterCpuTicks.class);
        return s.fetch(nodeidx).execute().body();
    }
    public WaterMeterCpuTicksV3 waterMeterCpuTicks(int nodeidx, String _excludeFields) throws IOException {
        WaterMeterCpuTicks s = getService(WaterMeterCpuTicks.class);
        return s.fetch(nodeidx, _excludeFields).execute().body();
    }

    /**
     * Return IO usage snapshot of all nodes in the H2O cluster.
     */
    public WaterMeterIoV3 waterMeterIoForNode(int nodeidx) throws IOException {
        WaterMeterIo s = getService(WaterMeterIo.class);
        return s.fetch(nodeidx).execute().body();
    }
    public WaterMeterIoV3 waterMeterIoForNode(int nodeidx, String _excludeFields) throws IOException {
        WaterMeterIo s = getService(WaterMeterIo.class);
        return s.fetch(nodeidx, _excludeFields).execute().body();
    }

    /**
     * Return IO usage snapshot of all nodes in the H2O cluster.
     */
    public WaterMeterIoV3 waterMeterIoForCluster() throws IOException {
        WaterMeterIo s = getService(WaterMeterIo.class);
        return s.fetch_all().execute().body();
    }
    public WaterMeterIoV3 waterMeterIoForCluster(int nodeidx) throws IOException {
        WaterMeterIo s = getService(WaterMeterIo.class);
        return s.fetch_all(nodeidx, "").execute().body();
    }
    public WaterMeterIoV3 waterMeterIoForCluster(int nodeidx, String _excludeFields) throws IOException {
        WaterMeterIo s = getService(WaterMeterIo.class);
        return s.fetch_all(nodeidx, _excludeFields).execute().body();
    }

    /**
     * Return true or false.
     */
    public NodePersistentStorageV3 npsContains(String category, String name) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.exists(category, name).execute().body();
    }
    public NodePersistentStorageV3 npsContains(NodePersistentStorageV3 params) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.exists(
                params.category,
                params.name,
                params.value,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return true or false.
     */
    public NodePersistentStorageV3 npsEnabled() throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.configured().execute().body();
    }
    public NodePersistentStorageV3 npsEnabled(NodePersistentStorageV3 params) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.configured(
                params.category,
                params.name,
                params.value,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Store a named value.
     */
    public NodePersistentStorageV3 npsPut(String category, String name) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.put_with_name(category, name).execute().body();
    }
    public NodePersistentStorageV3 npsPut(NodePersistentStorageV3 params) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.put_with_name(
                params.category,
                params.name,
                params.value,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return value for a given name.
     */
    public NodePersistentStorageV3 npsGet(String category, String name) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.get_as_string(category, name).execute().body();
    }
    public NodePersistentStorageV3 npsGet(NodePersistentStorageV3 params) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.get_as_string(
                params.category,
                params.name,
                params.value,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Delete a key.
     */
    public NodePersistentStorageV3 npsRemove(String category, String name) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.delete(category, name).execute().body();
    }
    public NodePersistentStorageV3 npsRemove(NodePersistentStorageV3 params) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.delete(
                params.category,
                params.name,
                params.value,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Store a value.
     */
    public NodePersistentStorageV3 npsCreateCategory(String category) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.put(category).execute().body();
    }
    public NodePersistentStorageV3 npsCreateCategory(NodePersistentStorageV3 params) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.put(
                params.category,
                params.name,
                params.value,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Return all keys stored for a given category.
     */
    public NodePersistentStorageV3 npsKeys(String category) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.list(category).execute().body();
    }
    public NodePersistentStorageV3 npsKeys(NodePersistentStorageV3 params) throws IOException {
        NodePersistentStorage s = getService(NodePersistentStorage.class);
        return s.list(
                params.category,
                params.name,
                params.value,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Get named log file for a node.
     */
    public LogsV3 logs(int nodeidx, String name) throws IOException {
        Logs s = getService(Logs.class);
        return s.fetch(nodeidx, name).execute().body();
    }
    public LogsV3 logs(int nodeidx, String name, String _excludeFields) throws IOException {
        Logs s = getService(Logs.class);
        return s.fetch(nodeidx, name, _excludeFields).execute().body();
    }

    /**
     * Kill minus 3 on *this* node
     */
    public KillMinus3V3 logThreadDump() throws IOException {
        KillMinus3 s = getService(KillMinus3.class);
        return s.killm3().execute().body();
    }
    public KillMinus3V3 logThreadDump(String _excludeFields) throws IOException {
        KillMinus3 s = getService(KillMinus3.class);
        return s.killm3(_excludeFields).execute().body();
    }

    /**
     * Execute an Rapids AstRoot.
     */
    public RapidsSchemaV3 rapidsExec(String ast) throws IOException {
        Rapids s = getService(Rapids.class);
        return s.rapidsExec(ast).execute().body();
    }
    public RapidsSchemaV3 rapidsExec(RapidsSchemaV3 params) throws IOException {
        Rapids s = getService(Rapids.class);
        return s.rapidsExec(
                params.ast,
                params.sessionId,
                params.id,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Generate a Java POJO from the Assembly
     */
    public AssemblyV99 _assembly_toJava(String assemblyId, String pojoName) throws IOException {
        Assembly s = getService(Assembly.class);
        return s.toJava(assemblyId, pojoName).execute().body();
    }
    public AssemblyV99 _assembly_toJava(AssemblyV99 params) throws IOException {
        Assembly s = getService(Assembly.class);
        return s.toJava(
                params.assemblyId,
                params.pojoName,
                params.steps,
                keyToString(params.frame),
                params._excludeFields
        ).execute().body();
    }

    /**
     * Fit an assembly to an input frame
     */
    public AssemblyV99 _assembly_fit() throws IOException {
        Assembly s = getService(Assembly.class);
        return s.fit().execute().body();
    }
    public AssemblyV99 _assembly_fit(AssemblyV99 params) throws IOException {
        Assembly s = getService(Assembly.class);
        return s.fit(
                params.steps,
                keyToString(params.frame),
                params.pojoName,
                params.assemblyId,
                params._excludeFields
        ).execute().body();
    }

    /**
     * Download dataset as a CSV.
     */
    public DownloadDataV3 _downloadDataset_fetch(FrameKeyV3 frameId) throws IOException {
        DownloadDataset s = getService(DownloadDataset.class);
        return s.fetch(keyToString(frameId)).execute().body();
    }
    public DownloadDataV3 _downloadDataset_fetch(FrameKeyV3 frameId, boolean hexString) throws IOException {
        DownloadDataset s = getService(DownloadDataset.class);
        return s.fetch(keyToString(frameId), hexString, "").execute().body();
    }
    public DownloadDataV3 _downloadDataset_fetch(FrameKeyV3 frameId, boolean hexString, String _excludeFields) throws IOException {
        DownloadDataset s = getService(DownloadDataset.class);
        return s.fetch(keyToString(frameId), hexString, _excludeFields).execute().body();
    }

    /**
     * Download dataset as a CSV.
     */
    public DownloadDataV3 _downloadDataset_fetchStreaming(FrameKeyV3 frameId) throws IOException {
        DownloadDataset s = getService(DownloadDataset.class);
        return s.fetchStreaming(keyToString(frameId)).execute().body();
    }
    public DownloadDataV3 _downloadDataset_fetchStreaming(FrameKeyV3 frameId, boolean hexString) throws IOException {
        DownloadDataset s = getService(DownloadDataset.class);
        return s.fetchStreaming(keyToString(frameId), hexString, "").execute().body();
    }
    public DownloadDataV3 _downloadDataset_fetchStreaming(FrameKeyV3 frameId, boolean hexString, String _excludeFields) throws IOException {
        DownloadDataset s = getService(DownloadDataset.class);
        return s.fetchStreaming(keyToString(frameId), hexString, _excludeFields).execute().body();
    }

    /**
     * Remove an arbitrary key from the H2O distributed K/V store.
     */
    public RemoveV3 deleteKey(KeyV3 key) throws IOException {
        DKV s = getService(DKV.class);
        return s.remove(keyToString(key)).execute().body();
    }
    public RemoveV3 deleteKey(KeyV3 key, String _excludeFields) throws IOException {
        DKV s = getService(DKV.class);
        return s.remove(keyToString(key), _excludeFields).execute().body();
    }

    /**
     * Remove all keys from the H2O distributed K/V store.
     */
    public RemoveAllV3 deleteAllKeys() throws IOException {
        DKV s = getService(DKV.class);
        return s.removeAll().execute().body();
    }
    public RemoveAllV3 deleteAllKeys(String _excludeFields) throws IOException {
        DKV s = getService(DKV.class);
        return s.removeAll(_excludeFields).execute().body();
    }

    /**
     * Save a message to the H2O logfile.
     */
    public LogAndEchoV3 logAndEcho() throws IOException {
        LogAndEcho s = getService(LogAndEcho.class);
        return s.echo().execute().body();
    }
    public LogAndEchoV3 logAndEcho(String message) throws IOException {
        LogAndEcho s = getService(LogAndEcho.class);
        return s.echo(message, "").execute().body();
    }
    public LogAndEchoV3 logAndEcho(String message, String _excludeFields) throws IOException {
        LogAndEcho s = getService(LogAndEcho.class);
        return s.echo(message, _excludeFields).execute().body();
    }

    /**
     * Issue a new session ID.
     */
    public InitIDV3 newSession() throws IOException {
        InitID s = getService(InitID.class);
        return s.startSession().execute().body();
    }
    public InitIDV3 newSession(String sessionKey) throws IOException {
        InitID s = getService(InitID.class);
        return s.startSession(sessionKey, "").execute().body();
    }
    public InitIDV3 newSession(String sessionKey, String _excludeFields) throws IOException {
        InitID s = getService(InitID.class);
        return s.startSession(sessionKey, _excludeFields).execute().body();
    }

    /**
     * End a session.
     */
    public InitIDV3 endSession() throws IOException {
        InitID s = getService(InitID.class);
        return s.endSession().execute().body();
    }
    public InitIDV3 endSession(String sessionKey) throws IOException {
        InitID s = getService(InitID.class);
        return s.endSession(sessionKey, "").execute().body();
    }
    public InitIDV3 endSession(String sessionKey, String _excludeFields) throws IOException {
        InitID s = getService(InitID.class);
        return s.endSession(sessionKey, _excludeFields).execute().body();
    }

    /**
     * Explicitly call System.gc().
     */
    public GarbageCollectV3 garbageCollect() throws IOException {
        GarbageCollect s = getService(GarbageCollect.class);
        return s.gc().execute().body();
    }

    /**
     * Example of an experimental endpoint.  Call via /EXPERIMENTAL/Sample.  Experimental endpoints can change at any
     * moment.
     */
    public CloudV3 _sample_status() throws IOException {
        Sample s = getService(Sample.class);
        return s.status().execute().body();
    }
    public CloudV3 _sample_status(boolean skipTicks) throws IOException {
        Sample s = getService(Sample.class);
        return s.status(skipTicks, "").execute().body();
    }
    public CloudV3 _sample_status(boolean skipTicks, String _excludeFields) throws IOException {
        Sample s = getService(Sample.class);
        return s.status(skipTicks, _excludeFields).execute().body();
    }

    /**
     * Produce help for Rapids AstRoot language.
     */
    public RapidsHelpV3 rapids_help() throws IOException {
        Rapids s = getService(Rapids.class);
        return s.genHelp().execute().body();
    }

    /**
     * Train a DeepLearning model.
     */
    public DeepLearningV3 train_deeplearning() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainDeeplearning().execute().body();
    }
    public DeepLearningV3 train_deeplearning(DeepLearningParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainDeeplearning(
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.activation,
                params.hidden,
                params.epochs,
                params.trainSamplesPerIteration,
                params.targetRatioCommToComp,
                params.seed,
                params.adaptiveRate,
                params.rho,
                params.epsilon,
                params.rate,
                params.rateAnnealing,
                params.rateDecay,
                params.momentumStart,
                params.momentumRamp,
                params.momentumStable,
                params.nesterovAcceleratedGradient,
                params.inputDropoutRatio,
                params.hiddenDropoutRatios,
                params.l1,
                params.l2,
                params.maxW2,
                params.initialWeightDistribution,
                params.initialWeightScale,
                keyArrayToStringArray(params.initialWeights),
                keyArrayToStringArray(params.initialBiases),
                params.loss,
                params.scoreInterval,
                params.scoreTrainingSamples,
                params.scoreValidationSamples,
                params.scoreDutyCycle,
                params.classificationStop,
                params.regressionStop,
                params.quietMode,
                params.scoreValidationSampling,
                params.overwriteWithBestModel,
                params.autoencoder,
                params.useAllFactorLevels,
                params.standardize,
                params.diagnostics,
                params.variableImportances,
                params.fastMode,
                params.forceLoadBalance,
                params.replicateTrainingData,
                params.singleNodeMode,
                params.shuffleTrainingData,
                params.missingValuesHandling,
                params.sparse,
                params.colMajor,
                params.averageActivation,
                params.sparsityBeta,
                params.maxCategoricalFeatures,
                params.reproducible,
                params.exportWeightsAndBiases,
                params.miniBatchSize,
                params.elasticAveraging,
                params.elasticAveragingMovingRate,
                params.elasticAveragingRegularization,
                keyToString(params.pretrainedAutoencoder),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of DeepLearning model builder parameters.
     */
    public DeepLearningV3 validate_deeplearning() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersDeeplearning().execute().body();
    }
    public DeepLearningV3 validate_deeplearning(DeepLearningParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersDeeplearning(
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.activation,
                params.hidden,
                params.epochs,
                params.trainSamplesPerIteration,
                params.targetRatioCommToComp,
                params.seed,
                params.adaptiveRate,
                params.rho,
                params.epsilon,
                params.rate,
                params.rateAnnealing,
                params.rateDecay,
                params.momentumStart,
                params.momentumRamp,
                params.momentumStable,
                params.nesterovAcceleratedGradient,
                params.inputDropoutRatio,
                params.hiddenDropoutRatios,
                params.l1,
                params.l2,
                params.maxW2,
                params.initialWeightDistribution,
                params.initialWeightScale,
                keyArrayToStringArray(params.initialWeights),
                keyArrayToStringArray(params.initialBiases),
                params.loss,
                params.scoreInterval,
                params.scoreTrainingSamples,
                params.scoreValidationSamples,
                params.scoreDutyCycle,
                params.classificationStop,
                params.regressionStop,
                params.quietMode,
                params.scoreValidationSampling,
                params.overwriteWithBestModel,
                params.autoencoder,
                params.useAllFactorLevels,
                params.standardize,
                params.diagnostics,
                params.variableImportances,
                params.fastMode,
                params.forceLoadBalance,
                params.replicateTrainingData,
                params.singleNodeMode,
                params.shuffleTrainingData,
                params.missingValuesHandling,
                params.sparse,
                params.colMajor,
                params.averageActivation,
                params.sparsityBeta,
                params.maxCategoricalFeatures,
                params.reproducible,
                params.exportWeightsAndBiases,
                params.miniBatchSize,
                params.elasticAveraging,
                params.elasticAveragingMovingRate,
                params.elasticAveragingRegularization,
                keyToString(params.pretrainedAutoencoder),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for DeepLearning model.
     */
    public DeepLearningV3 grid_search_deeplearning() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainDeeplearning().execute().body();
    }
    public DeepLearningV3 grid_search_deeplearning(DeepLearningParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainDeeplearning(
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.activation,
                params.hidden,
                params.epochs,
                params.trainSamplesPerIteration,
                params.targetRatioCommToComp,
                params.seed,
                params.adaptiveRate,
                params.rho,
                params.epsilon,
                params.rate,
                params.rateAnnealing,
                params.rateDecay,
                params.momentumStart,
                params.momentumRamp,
                params.momentumStable,
                params.nesterovAcceleratedGradient,
                params.inputDropoutRatio,
                params.hiddenDropoutRatios,
                params.l1,
                params.l2,
                params.maxW2,
                params.initialWeightDistribution,
                params.initialWeightScale,
                keyArrayToStringArray(params.initialWeights),
                keyArrayToStringArray(params.initialBiases),
                params.loss,
                params.scoreInterval,
                params.scoreTrainingSamples,
                params.scoreValidationSamples,
                params.scoreDutyCycle,
                params.classificationStop,
                params.regressionStop,
                params.quietMode,
                params.scoreValidationSampling,
                params.overwriteWithBestModel,
                params.autoencoder,
                params.useAllFactorLevels,
                params.standardize,
                params.diagnostics,
                params.variableImportances,
                params.fastMode,
                params.forceLoadBalance,
                params.replicateTrainingData,
                params.singleNodeMode,
                params.shuffleTrainingData,
                params.missingValuesHandling,
                params.sparse,
                params.colMajor,
                params.averageActivation,
                params.sparsityBeta,
                params.maxCategoricalFeatures,
                params.reproducible,
                params.exportWeightsAndBiases,
                params.miniBatchSize,
                params.elasticAveraging,
                params.elasticAveragingMovingRate,
                params.elasticAveragingRegularization,
                keyToString(params.pretrainedAutoencoder),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a GLM model.
     */
    public GLMV3 train_glm() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainGlm().execute().body();
    }
    public GLMV3 train_glm(GLMParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainGlm(
                params.seed,
                params.family,
                params.tweedieVariancePower,
                params.tweedieLinkPower,
                params.solver,
                params.alpha,
                params.lambda,
                params.lambdaSearch,
                params.earlyStopping,
                params.nlambdas,
                params.standardize,
                params.missingValuesHandling,
                params.nonNegative,
                params.maxIterations,
                params.betaEpsilon,
                params.objectiveEpsilon,
                params.gradientEpsilon,
                params.objReg,
                params.link,
                params.intercept,
                params.prior,
                params.lambdaMinRatio,
                keyToString(params.betaConstraints),
                params.maxActivePredictors,
                params.interactions,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.computePValues,
                params.removeCollinearColumns,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of GLM model builder parameters.
     */
    public GLMV3 validate_glm() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersGlm().execute().body();
    }
    public GLMV3 validate_glm(GLMParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersGlm(
                params.seed,
                params.family,
                params.tweedieVariancePower,
                params.tweedieLinkPower,
                params.solver,
                params.alpha,
                params.lambda,
                params.lambdaSearch,
                params.earlyStopping,
                params.nlambdas,
                params.standardize,
                params.missingValuesHandling,
                params.nonNegative,
                params.maxIterations,
                params.betaEpsilon,
                params.objectiveEpsilon,
                params.gradientEpsilon,
                params.objReg,
                params.link,
                params.intercept,
                params.prior,
                params.lambdaMinRatio,
                keyToString(params.betaConstraints),
                params.maxActivePredictors,
                params.interactions,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.computePValues,
                params.removeCollinearColumns,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for GLM model.
     */
    public GLMV3 grid_search_glm() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainGlm().execute().body();
    }
    public GLMV3 grid_search_glm(GLMParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainGlm(
                params.seed,
                params.family,
                params.tweedieVariancePower,
                params.tweedieLinkPower,
                params.solver,
                params.alpha,
                params.lambda,
                params.lambdaSearch,
                params.earlyStopping,
                params.nlambdas,
                params.standardize,
                params.missingValuesHandling,
                params.nonNegative,
                params.maxIterations,
                params.betaEpsilon,
                params.objectiveEpsilon,
                params.gradientEpsilon,
                params.objReg,
                params.link,
                params.intercept,
                params.prior,
                params.lambdaMinRatio,
                keyToString(params.betaConstraints),
                params.maxActivePredictors,
                params.interactions,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.computePValues,
                params.removeCollinearColumns,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a GLRM model.
     */
    public GLRMV3 train_glrm(int k) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainGlrm(k).execute().body();
    }
    public GLRMV3 train_glrm(GLRMParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainGlrm(
                params.transform,
                params.k,
                params.loss,
                params.multiLoss,
                params.lossByCol,
                params.lossByColIdx,
                params.period,
                params.regularizationX,
                params.regularizationY,
                params.gammaX,
                params.gammaY,
                params.maxIterations,
                params.maxUpdates,
                params.initStepSize,
                params.minStepSize,
                params.seed,
                params.init,
                params.svdMethod,
                keyToString(params.userY),
                keyToString(params.userX),
                params.loadingName,
                params.expandUserY,
                params.imputeOriginal,
                params.recoverSvd,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of GLRM model builder parameters.
     */
    public GLRMV3 validate_glrm(int k) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersGlrm(k).execute().body();
    }
    public GLRMV3 validate_glrm(GLRMParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersGlrm(
                params.transform,
                params.k,
                params.loss,
                params.multiLoss,
                params.lossByCol,
                params.lossByColIdx,
                params.period,
                params.regularizationX,
                params.regularizationY,
                params.gammaX,
                params.gammaY,
                params.maxIterations,
                params.maxUpdates,
                params.initStepSize,
                params.minStepSize,
                params.seed,
                params.init,
                params.svdMethod,
                keyToString(params.userY),
                keyToString(params.userX),
                params.loadingName,
                params.expandUserY,
                params.imputeOriginal,
                params.recoverSvd,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for GLRM model.
     */
    public GLRMV3 grid_search_glrm(int k) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainGlrm(k).execute().body();
    }
    public GLRMV3 grid_search_glrm(GLRMParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainGlrm(
                params.transform,
                params.k,
                params.loss,
                params.multiLoss,
                params.lossByCol,
                params.lossByColIdx,
                params.period,
                params.regularizationX,
                params.regularizationY,
                params.gammaX,
                params.gammaY,
                params.maxIterations,
                params.maxUpdates,
                params.initStepSize,
                params.minStepSize,
                params.seed,
                params.init,
                params.svdMethod,
                keyToString(params.userY),
                keyToString(params.userX),
                params.loadingName,
                params.expandUserY,
                params.imputeOriginal,
                params.recoverSvd,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a KMeans model.
     */
    public KMeansV3 train_kmeans() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainKmeans().execute().body();
    }
    public KMeansV3 train_kmeans(KMeansParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainKmeans(
                keyToString(params.userPoints),
                params.maxIterations,
                params.standardize,
                params.seed,
                params.init,
                params.estimateK,
                params.k,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of KMeans model builder parameters.
     */
    public KMeansV3 validate_kmeans() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersKmeans().execute().body();
    }
    public KMeansV3 validate_kmeans(KMeansParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersKmeans(
                keyToString(params.userPoints),
                params.maxIterations,
                params.standardize,
                params.seed,
                params.init,
                params.estimateK,
                params.k,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for KMeans model.
     */
    public KMeansV3 grid_search_kmeans() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainKmeans().execute().body();
    }
    public KMeansV3 grid_search_kmeans(KMeansParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainKmeans(
                keyToString(params.userPoints),
                params.maxIterations,
                params.standardize,
                params.seed,
                params.init,
                params.estimateK,
                params.k,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a NaiveBayes model.
     */
    public NaiveBayesV3 train_naivebayes() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainNaivebayes().execute().body();
    }
    public NaiveBayesV3 train_naivebayes(NaiveBayesParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainNaivebayes(
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.laplace,
                params.minSdev,
                params.epsSdev,
                params.minProb,
                params.epsProb,
                params.computeMetrics,
                params.seed,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of NaiveBayes model builder parameters.
     */
    public NaiveBayesV3 validate_naivebayes() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersNaivebayes().execute().body();
    }
    public NaiveBayesV3 validate_naivebayes(NaiveBayesParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersNaivebayes(
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.laplace,
                params.minSdev,
                params.epsSdev,
                params.minProb,
                params.epsProb,
                params.computeMetrics,
                params.seed,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for NaiveBayes model.
     */
    public NaiveBayesV3 grid_search_naivebayes() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainNaivebayes().execute().body();
    }
    public NaiveBayesV3 grid_search_naivebayes(NaiveBayesParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainNaivebayes(
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.laplace,
                params.minSdev,
                params.epsSdev,
                params.minProb,
                params.epsProb,
                params.computeMetrics,
                params.seed,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a PCA model.
     */
    public PCAV3 train_pca(int k) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainPca(k).execute().body();
    }
    public PCAV3 train_pca(PCAParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainPca(
                params.transform,
                params.pcaMethod,
                params.k,
                params.maxIterations,
                params.seed,
                params.useAllFactorLevels,
                params.computeMetrics,
                params.imputeMissing,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of PCA model builder parameters.
     */
    public PCAV3 validate_pca(int k) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersPca(k).execute().body();
    }
    public PCAV3 validate_pca(PCAParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersPca(
                params.transform,
                params.pcaMethod,
                params.k,
                params.maxIterations,
                params.seed,
                params.useAllFactorLevels,
                params.computeMetrics,
                params.imputeMissing,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for PCA model.
     */
    public PCAV3 grid_search_pca(int k) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainPca(k).execute().body();
    }
    public PCAV3 grid_search_pca(PCAParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainPca(
                params.transform,
                params.pcaMethod,
                params.k,
                params.maxIterations,
                params.seed,
                params.useAllFactorLevels,
                params.computeMetrics,
                params.imputeMissing,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a SVD model.
     */
    public SVDV99 train_svd() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainSvd().execute().body();
    }
    public SVDV99 train_svd(SVDParametersV99 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainSvd(
                params.transform,
                params.svdMethod,
                params.nv,
                params.maxIterations,
                params.seed,
                params.keepU,
                params.uName,
                params.useAllFactorLevels,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of SVD model builder parameters.
     */
    public SVDV99 validate_svd() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersSvd().execute().body();
    }
    public SVDV99 validate_svd(SVDParametersV99 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersSvd(
                params.transform,
                params.svdMethod,
                params.nv,
                params.maxIterations,
                params.seed,
                params.keepU,
                params.uName,
                params.useAllFactorLevels,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for SVD model.
     */
    public SVDV99 grid_search_svd() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainSvd().execute().body();
    }
    public SVDV99 grid_search_svd(SVDParametersV99 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainSvd(
                params.transform,
                params.svdMethod,
                params.nv,
                params.maxIterations,
                params.seed,
                params.keepU,
                params.uName,
                params.useAllFactorLevels,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a DRF model.
     */
    public DRFV3 train_drf() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainDrf().execute().body();
    }
    public DRFV3 train_drf(DRFParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainDrf(
                params.mtries,
                params.binomialDoubleTrees,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.ntrees,
                params.maxDepth,
                params.minRows,
                params.nbins,
                params.nbinsTopLevel,
                params.nbinsCats,
                params.r2Stopping,
                params.seed,
                params.buildTreeOneNode,
                params.sampleRate,
                params.sampleRatePerClass,
                params.colSampleRatePerTree,
                params.colSampleRateChangePerLevel,
                params.scoreTreeInterval,
                params.minSplitImprovement,
                params.histogramType,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of DRF model builder parameters.
     */
    public DRFV3 validate_drf() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersDrf().execute().body();
    }
    public DRFV3 validate_drf(DRFParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersDrf(
                params.mtries,
                params.binomialDoubleTrees,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.ntrees,
                params.maxDepth,
                params.minRows,
                params.nbins,
                params.nbinsTopLevel,
                params.nbinsCats,
                params.r2Stopping,
                params.seed,
                params.buildTreeOneNode,
                params.sampleRate,
                params.sampleRatePerClass,
                params.colSampleRatePerTree,
                params.colSampleRateChangePerLevel,
                params.scoreTreeInterval,
                params.minSplitImprovement,
                params.histogramType,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for DRF model.
     */
    public DRFV3 grid_search_drf() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainDrf().execute().body();
    }
    public DRFV3 grid_search_drf(DRFParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainDrf(
                params.mtries,
                params.binomialDoubleTrees,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.ntrees,
                params.maxDepth,
                params.minRows,
                params.nbins,
                params.nbinsTopLevel,
                params.nbinsCats,
                params.r2Stopping,
                params.seed,
                params.buildTreeOneNode,
                params.sampleRate,
                params.sampleRatePerClass,
                params.colSampleRatePerTree,
                params.colSampleRateChangePerLevel,
                params.scoreTreeInterval,
                params.minSplitImprovement,
                params.histogramType,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a GBM model.
     */
    public GBMV3 train_gbm() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainGbm().execute().body();
    }
    public GBMV3 train_gbm(GBMParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainGbm(
                params.learnRate,
                params.learnRateAnnealing,
                params.colSampleRate,
                params.maxAbsLeafnodePred,
                params.predNoiseBandwidth,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.ntrees,
                params.maxDepth,
                params.minRows,
                params.nbins,
                params.nbinsTopLevel,
                params.nbinsCats,
                params.r2Stopping,
                params.seed,
                params.buildTreeOneNode,
                params.sampleRate,
                params.sampleRatePerClass,
                params.colSampleRatePerTree,
                params.colSampleRateChangePerLevel,
                params.scoreTreeInterval,
                params.minSplitImprovement,
                params.histogramType,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of GBM model builder parameters.
     */
    public GBMV3 validate_gbm() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersGbm().execute().body();
    }
    public GBMV3 validate_gbm(GBMParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersGbm(
                params.learnRate,
                params.learnRateAnnealing,
                params.colSampleRate,
                params.maxAbsLeafnodePred,
                params.predNoiseBandwidth,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.ntrees,
                params.maxDepth,
                params.minRows,
                params.nbins,
                params.nbinsTopLevel,
                params.nbinsCats,
                params.r2Stopping,
                params.seed,
                params.buildTreeOneNode,
                params.sampleRate,
                params.sampleRatePerClass,
                params.colSampleRatePerTree,
                params.colSampleRateChangePerLevel,
                params.scoreTreeInterval,
                params.minSplitImprovement,
                params.histogramType,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for GBM model.
     */
    public GBMV3 grid_search_gbm() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainGbm().execute().body();
    }
    public GBMV3 grid_search_gbm(GBMParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainGbm(
                params.learnRate,
                params.learnRateAnnealing,
                params.colSampleRate,
                params.maxAbsLeafnodePred,
                params.predNoiseBandwidth,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                params.maxConfusionMatrixSize,
                params.maxHitRatioK,
                params.ntrees,
                params.maxDepth,
                params.minRows,
                params.nbins,
                params.nbinsTopLevel,
                params.nbinsCats,
                params.r2Stopping,
                params.seed,
                params.buildTreeOneNode,
                params.sampleRate,
                params.sampleRatePerClass,
                params.colSampleRatePerTree,
                params.colSampleRateChangePerLevel,
                params.scoreTreeInterval,
                params.minSplitImprovement,
                params.histogramType,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a Aggregator model.
     */
    public AggregatorV99 train_aggregator() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainAggregator().execute().body();
    }
    public AggregatorV99 train_aggregator(AggregatorParametersV99 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainAggregator(
                params.radiusScale,
                params.transform,
                params.pcaMethod,
                params.k,
                params.maxIterations,
                params.seed,
                params.useAllFactorLevels,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of Aggregator model builder parameters.
     */
    public AggregatorV99 validate_aggregator() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersAggregator().execute().body();
    }
    public AggregatorV99 validate_aggregator(AggregatorParametersV99 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersAggregator(
                params.radiusScale,
                params.transform,
                params.pcaMethod,
                params.k,
                params.maxIterations,
                params.seed,
                params.useAllFactorLevels,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for Aggregator model.
     */
    public AggregatorV99 grid_search_aggregator() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainAggregator().execute().body();
    }
    public AggregatorV99 grid_search_aggregator(AggregatorParametersV99 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainAggregator(
                params.radiusScale,
                params.transform,
                params.pcaMethod,
                params.k,
                params.maxIterations,
                params.seed,
                params.useAllFactorLevels,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a DeepWater model.
     */
    public DeepWaterV3 train_deepwater() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainDeepwater().execute().body();
    }
    public DeepWaterV3 train_deepwater(DeepWaterParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainDeepwater(
                params.problemType,
                params.activation,
                params.hidden,
                params.inputDropoutRatio,
                params.hiddenDropoutRatios,
                params.maxConfusionMatrixSize,
                params.sparse,
                params.maxHitRatioK,
                params.epochs,
                params.trainSamplesPerIteration,
                params.targetRatioCommToComp,
                params.seed,
                params.learningRate,
                params.learningRateAnnealing,
                params.momentumStart,
                params.momentumRamp,
                params.momentumStable,
                params.scoreInterval,
                params.scoreTrainingSamples,
                params.scoreValidationSamples,
                params.scoreDutyCycle,
                params.classificationStop,
                params.regressionStop,
                params.quietMode,
                params.overwriteWithBestModel,
                params.autoencoder,
                params.diagnostics,
                params.variableImportances,
                params.replicateTrainingData,
                params.singleNodeMode,
                params.shuffleTrainingData,
                params.miniBatchSize,
                params.clipGradient,
                params.network,
                params.backend,
                params.imageShape,
                params.channels,
                params.gpu,
                params.deviceId,
                params.networkDefinitionFile,
                params.networkParametersFile,
                params.meanImageFile,
                params.exportNativeParametersPrefix,
                params.standardize,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of DeepWater model builder parameters.
     */
    public DeepWaterV3 validate_deepwater() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersDeepwater().execute().body();
    }
    public DeepWaterV3 validate_deepwater(DeepWaterParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersDeepwater(
                params.problemType,
                params.activation,
                params.hidden,
                params.inputDropoutRatio,
                params.hiddenDropoutRatios,
                params.maxConfusionMatrixSize,
                params.sparse,
                params.maxHitRatioK,
                params.epochs,
                params.trainSamplesPerIteration,
                params.targetRatioCommToComp,
                params.seed,
                params.learningRate,
                params.learningRateAnnealing,
                params.momentumStart,
                params.momentumRamp,
                params.momentumStable,
                params.scoreInterval,
                params.scoreTrainingSamples,
                params.scoreValidationSamples,
                params.scoreDutyCycle,
                params.classificationStop,
                params.regressionStop,
                params.quietMode,
                params.overwriteWithBestModel,
                params.autoencoder,
                params.diagnostics,
                params.variableImportances,
                params.replicateTrainingData,
                params.singleNodeMode,
                params.shuffleTrainingData,
                params.miniBatchSize,
                params.clipGradient,
                params.network,
                params.backend,
                params.imageShape,
                params.channels,
                params.gpu,
                params.deviceId,
                params.networkDefinitionFile,
                params.networkParametersFile,
                params.meanImageFile,
                params.exportNativeParametersPrefix,
                params.standardize,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for DeepWater model.
     */
    public DeepWaterV3 grid_search_deepwater() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainDeepwater().execute().body();
    }
    public DeepWaterV3 grid_search_deepwater(DeepWaterParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainDeepwater(
                params.problemType,
                params.activation,
                params.hidden,
                params.inputDropoutRatio,
                params.hiddenDropoutRatios,
                params.maxConfusionMatrixSize,
                params.sparse,
                params.maxHitRatioK,
                params.epochs,
                params.trainSamplesPerIteration,
                params.targetRatioCommToComp,
                params.seed,
                params.learningRate,
                params.learningRateAnnealing,
                params.momentumStart,
                params.momentumRamp,
                params.momentumStable,
                params.scoreInterval,
                params.scoreTrainingSamples,
                params.scoreValidationSamples,
                params.scoreDutyCycle,
                params.classificationStop,
                params.regressionStop,
                params.quietMode,
                params.overwriteWithBestModel,
                params.autoencoder,
                params.diagnostics,
                params.variableImportances,
                params.replicateTrainingData,
                params.singleNodeMode,
                params.shuffleTrainingData,
                params.miniBatchSize,
                params.clipGradient,
                params.network,
                params.backend,
                params.imageShape,
                params.channels,
                params.gpu,
                params.deviceId,
                params.networkDefinitionFile,
                params.networkParametersFile,
                params.meanImageFile,
                params.exportNativeParametersPrefix,
                params.standardize,
                params.balanceClasses,
                params.classSamplingFactors,
                params.maxAfterBalanceSize,
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a Word2Vec model.
     */
    public Word2VecV3 train_word2vec() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainWord2vec().execute().body();
    }
    public Word2VecV3 train_word2vec(Word2VecParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainWord2vec(
                params.vecSize,
                params.windowSize,
                params.sentSampleRate,
                params.normModel,
                params.epochs,
                params.minWordFreq,
                params.initLearningRate,
                params.wordModel,
                keyToString(params.preTrained),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of Word2Vec model builder parameters.
     */
    public Word2VecV3 validate_word2vec() throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersWord2vec().execute().body();
    }
    public Word2VecV3 validate_word2vec(Word2VecParametersV3 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersWord2vec(
                params.vecSize,
                params.windowSize,
                params.sentSampleRate,
                params.normModel,
                params.epochs,
                params.minWordFreq,
                params.initLearningRate,
                params.wordModel,
                keyToString(params.preTrained),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for Word2Vec model.
     */
    public Word2VecV3 grid_search_word2vec() throws IOException {
        Grid s = getService(Grid.class);
        return s.trainWord2vec().execute().body();
    }
    public Word2VecV3 grid_search_word2vec(Word2VecParametersV3 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainWord2vec(
                params.vecSize,
                params.windowSize,
                params.sentSampleRate,
                params.normModel,
                params.epochs,
                params.minWordFreq,
                params.initLearningRate,
                params.wordModel,
                keyToString(params.preTrained),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Train a StackedEnsemble model.
     */
    public StackedEnsembleV99 train_stackedensemble(ModelKeyV3[] baseModels) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainStackedensemble(keyArrayToStringArray(baseModels)).execute().body();
    }
    public StackedEnsembleV99 train_stackedensemble(StackedEnsembleParametersV99 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.trainStackedensemble(
                params.selectionStrategy,
                keyArrayToStringArray(params.baseModels),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Validate a set of StackedEnsemble model builder parameters.
     */
    public StackedEnsembleV99 validate_stackedensemble(ModelKeyV3[] baseModels) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersStackedensemble(keyArrayToStringArray(baseModels)).execute().body();
    }
    public StackedEnsembleV99 validate_stackedensemble(StackedEnsembleParametersV99 params) throws IOException {
        ModelBuilders s = getService(ModelBuilders.class);
        return s.validate_parametersStackedensemble(
                params.selectionStrategy,
                keyArrayToStringArray(params.baseModels),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Run grid search for StackedEnsemble model.
     */
    public StackedEnsembleV99 grid_search_stackedensemble(ModelKeyV3[] baseModels) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainStackedensemble(keyArrayToStringArray(baseModels)).execute().body();
    }
    public StackedEnsembleV99 grid_search_stackedensemble(StackedEnsembleParametersV99 params) throws IOException {
        Grid s = getService(Grid.class);
        return s.trainStackedensemble(
                params.selectionStrategy,
                keyArrayToStringArray(params.baseModels),
                keyToString(params.modelId),
                keyToString(params.trainingFrame),
                keyToString(params.validationFrame),
                params.nfolds,
                params.keepCrossValidationPredictions,
                params.keepCrossValidationFoldAssignment,
                params.parallelizeCrossValidation,
                params.distribution,
                params.tweediePower,
                params.quantileAlpha,
                params.huberAlpha,
                colToString(params.responseColumn),
                colToString(params.weightsColumn),
                colToString(params.offsetColumn),
                colToString(params.foldColumn),
                params.foldAssignment,
                params.categoricalEncoding,
                params.ignoredColumns,
                params.ignoreConstCols,
                params.scoreEachIteration,
                keyToString(params.checkpoint),
                params.stoppingRounds,
                params.maxRuntimeSecs,
                params.stoppingMetric,
                params.stoppingTolerance
        ).execute().body();
    }

    /**
     * Make a new GLM model based on existing one
     */
    public GLMModelV3 make_glm_model(ModelKeyV3 model, String[] names, double[] beta) throws IOException {
        MakeGLMModel s = getService(MakeGLMModel.class);
        return s.make_model(keyToString(model), names, beta).execute().body();
    }
    public GLMModelV3 make_glm_model(MakeGLMModelV3 params) throws IOException {
        MakeGLMModel s = getService(MakeGLMModel.class);
        return s.make_model(
                keyToString(params.model),
                keyToString(params.dest),
                params.names,
                params.beta,
                params.threshold
        ).execute().body();
    }

    /**
     * Get full regularization path
     */
    public GLMRegularizationPathV3 glm_regularization_path(ModelKeyV3 model) throws IOException {
        GetGLMRegPath s = getService(GetGLMRegPath.class);
        return s.extractRegularizationPath(keyToString(model)).execute().body();
    }
    public GLMRegularizationPathV3 glm_regularization_path(GLMRegularizationPathV3 params) throws IOException {
        GetGLMRegPath s = getService(GetGLMRegPath.class);
        return s.extractRegularizationPath(
                keyToString(params.model),
                params.lambdas,
                params.explainedDevianceTrain,
                params.explainedDevianceValid,
                params.coefficients,
                params.coefficientsStd,
                params.coefficientNames
        ).execute().body();
    }

    /**
     * Get weighted gram matrix
     */
    public GramV3 weighted_gram_matrix(FrameKeyV3 x) throws IOException {
        ComputeGram s = getService(ComputeGram.class);
        return s.computeGram(keyToString(x)).execute().body();
    }
    public GramV3 weighted_gram_matrix(GramV3 params) throws IOException {
        ComputeGram s = getService(ComputeGram.class);
        return s.computeGram(
                keyToString(params.x),
                colToString(params.w),
                params.useAllFactorLevels,
                params.standardize,
                params.skipMissing
        ).execute().body();
    }

    /**
     * Find synonyms using a word2vec model
     */
    public Word2VecSynonymsV3 word2vec_synonyms(ModelKeyV3 model, String word, int count) throws IOException {
        Word2VecSynonyms s = getService(Word2VecSynonyms.class);
        return s.findSynonyms(keyToString(model), word, count).execute().body();
    }
    public Word2VecSynonymsV3 word2vec_synonyms(Word2VecSynonymsV3 params) throws IOException {
        Word2VecSynonyms s = getService(Word2VecSynonyms.class);
        return s.findSynonyms(
                keyToString(params.model),
                params.word,
                params.count,
                params.synonyms,
                params.scores
        ).execute().body();
    }

    /**
     * Transform words to vectors using a word2vec model
     */
    public Word2VecTransformV3 word2vec_transform(ModelKeyV3 model, FrameKeyV3 wordsFrame) throws IOException {
        Word2VecTransform s = getService(Word2VecTransform.class);
        return s.transform(keyToString(model), keyToString(wordsFrame)).execute().body();
    }
    public Word2VecTransformV3 word2vec_transform(ModelKeyV3 model, FrameKeyV3 wordsFrame, Word2VecModelAggregateMethod aggregateMethod) throws IOException {
        Word2VecTransform s = getService(Word2VecTransform.class);
        return s.transform(keyToString(model), keyToString(wordsFrame), aggregateMethod).execute().body();
    }

    /**
     * Test only
     */
    public DataInfoFrameV3 glm_datainfo_frame() throws IOException {
        DataInfoFrame s = getService(DataInfoFrame.class);
        return s.getDataInfoFrame().execute().body();
    }
    public DataInfoFrameV3 glm_datainfo_frame(DataInfoFrameV3 params) throws IOException {
        DataInfoFrame s = getService(DataInfoFrame.class);
        return s.getDataInfoFrame(
                keyToString(params.frame),
                params.interactions,
                params.useAll,
                params.standardize,
                params.interactionsOnly
        ).execute().body();
    }



    //--------- PRIVATE --------------------------------------------------------------------------------------------------

    private Retrofit retrofit;
    private String _url = DEFAULT_URL;
    private int timeout_s = 60;
    private int pollInterval_ms = 1000;

    private void initializeRetrofit() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new SMH2oApi.ModelV3TypeAdapter())
                .registerTypeAdapter(KeyV3.class, new SMH2oApi.KeySerializer())
                .registerTypeAdapter(ColSpecifierV3.class, new SMH2oApi.ColSerializer())
                .registerTypeAdapter(ModelBuilderSchema.class, new SMH2oApi.ModelDeserializer())
                .registerTypeAdapter(ModelSchemaBaseV3.class, new SMH2oApi.ModelSchemaDeserializer())
                .registerTypeAdapter(ModelOutputSchemaV3.class, new SMH2oApi.ModelOutputDeserializer())
                .registerTypeAdapter(ModelParametersSchemaV3.class, new SMH2oApi.ModelParametersDeserializer())
                .setLenient()
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout_s, TimeUnit.SECONDS)
                .writeTimeout(timeout_s, TimeUnit.SECONDS)
                .readTimeout(timeout_s, TimeUnit.SECONDS)
                .build();

        this.retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(_url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) initializeRetrofit();
        return retrofit;
    }

    private <T> T getService(Class<T> clazz) {
        return getRetrofit().create(clazz);
    }


    /**
     * Keys get sent as Strings and returned as objects also containing the type and URL,
     * so they need a custom GSON serializer.
     */
    private static class KeySerializer implements JsonSerializer<KeyV3>, JsonDeserializer<KeyV3> {
        @Override
        public JsonElement serialize(KeyV3 key, Type typeOfKey, JsonSerializationContext context) {
            return new JsonPrimitive(key.name);
        }
        @Override
        public KeyV3 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            if (json.isJsonNull()) return null;
            JsonObject jobj = json.getAsJsonObject();
            String type = jobj.get("type").getAsString();
            switch (type) {
                // TODO: dynamically generate all possible cases
                case "Key<Model>": return context.deserialize(jobj, ModelKeyV3.class);
                case "Key<Job>":   return context.deserialize(jobj, JobKeyV3.class);
                case "Key<Grid>":  return context.deserialize(jobj, GridKeyV3.class);
                case "Key<Frame>": return context.deserialize(jobj, FrameKeyV3.class);
                default: throw new JsonParseException("Unable to deserialize key of type " + type);
            }
        }
    }

    private static class ColSerializer implements JsonSerializer<ColSpecifierV3> {
        @Override
        public JsonElement serialize(ColSpecifierV3 col, Type typeOfCol, JsonSerializationContext context) {
            return new JsonPrimitive(col.columnName);
        }
    }


    /**
     * Factory method for parsing a ModelBuilderSchema json object into an instance of the model-specific subclass.
     */
    private static class ModelDeserializer implements JsonDeserializer<ModelBuilderSchema> {
        @Override
        public ModelBuilderSchema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) return null;
            if (json.isJsonObject()) {
                JsonObject jobj = json.getAsJsonObject();
                if (jobj.has("algo")) {
                    String algo = jobj.get("algo").getAsJsonPrimitive().getAsString().toLowerCase();
                    switch (algo) {
                        case "deeplearning": return context.deserialize(json, DeepLearningV3.class);
                        case "glm": return context.deserialize(json, GLMV3.class);
                        case "glrm": return context.deserialize(json, GLRMV3.class);
                        case "kmeans": return context.deserialize(json, KMeansV3.class);
                        case "naivebayes": return context.deserialize(json, NaiveBayesV3.class);
                        case "pca": return context.deserialize(json, PCAV3.class);
                        case "svd": return context.deserialize(json, SVDV99.class);
                        case "drf": return context.deserialize(json, DRFV3.class);
                        case "gbm": return context.deserialize(json, GBMV3.class);
                        case "aggregator": return context.deserialize(json, AggregatorV99.class);
                        case "deepwater": return context.deserialize(json, DeepWaterV3.class);
                        case "word2vec": return context.deserialize(json, Word2VecV3.class);
                        case "stackedensemble": return context.deserialize(json, StackedEnsembleV99.class);
                        default:
                            throw new JsonParseException("Unable to deserialize model of type " + algo);
                    }
                }
            }
            throw new JsonParseException("Invalid ModelBuilderSchema element " + json.toString());
        }
    }

    /**
     * Factory method for parsing a ModelSchemaBaseV3 json object into an instance of the model-specific subclass.
     */
    private static class ModelSchemaDeserializer implements JsonDeserializer<ModelSchemaBaseV3> {
        @Override
        public ModelSchemaBaseV3 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) return null;
            if (json.isJsonObject()) {
                JsonObject jobj = json.getAsJsonObject();
                if (jobj.has("algo")) {
                    String algo = jobj.get("algo").getAsJsonPrimitive().getAsString().toLowerCase();
                    switch (algo) {
                        case "deeplearning": return context.deserialize(json, DeepLearningModelV3.class);
                        case "glm": return context.deserialize(json, GLMModelV3.class);
                        case "glrm": return context.deserialize(json, GLRMModelV3.class);
                        case "kmeans": return context.deserialize(json, KMeansModelV3.class);
                        case "naivebayes": return context.deserialize(json, NaiveBayesModelV3.class);
                        case "pca": return context.deserialize(json, PCAModelV3.class);
                        case "svd": return context.deserialize(json, SVDModelV99.class);
                        case "drf": return context.deserialize(json, DRFModelV3.class);
                        case "gbm": return context.deserialize(json, GBMModelV3.class);
                        case "aggregator": return context.deserialize(json, AggregatorModelV99.class);
                        case "deepwater": return context.deserialize(json, DeepWaterModelV3.class);
                        case "word2vec": return context.deserialize(json, Word2VecModelV3.class);
                        case "stackedensemble": return context.deserialize(json, StackedEnsembleModelV99.class);
                        default:
                            throw new JsonParseException("Unable to deserialize model of type " + algo);
                    }
                }
            }
            throw new JsonParseException("Invalid ModelSchemaBaseV3 element " + json.toString());
        }
    }

    /**
     * Factory method for parsing a ModelOutputSchemaV3 json object into an instance of the model-specific subclass.
     */
    private static class ModelOutputDeserializer implements JsonDeserializer<ModelOutputSchemaV3> {
        @Override
        public ModelOutputSchemaV3 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) return null;
            if (json.isJsonObject()) {
                JsonObject jobj = json.getAsJsonObject();
                if (jobj.has("algo")) {
                    String algo = jobj.get("algo").getAsJsonPrimitive().getAsString().toLowerCase();
                    switch (algo) {
                        case "deeplearning": return context.deserialize(json, DeepLearningModelOutputV3.class);
                        case "glm": return context.deserialize(json, GLMModelOutputV3.class);
                        case "glrm": return context.deserialize(json, GLRMModelOutputV3.class);
                        case "kmeans": return context.deserialize(json, KMeansModelOutputV3.class);
                        case "naivebayes": return context.deserialize(json, NaiveBayesModelOutputV3.class);
                        case "pca": return context.deserialize(json, PCAModelOutputV3.class);
                        case "svd": return context.deserialize(json, SVDModelOutputV99.class);
                        case "drf": return context.deserialize(json, DRFModelOutputV3.class);
                        case "gbm": return context.deserialize(json, GBMModelOutputV3.class);
                        case "aggregator": return context.deserialize(json, AggregatorModelOutputV99.class);
                        case "deepwater": return context.deserialize(json, DeepWaterModelOutputV3.class);
                        case "word2vec": return context.deserialize(json, Word2VecModelOutputV3.class);
                        case "stackedensemble": return context.deserialize(json, StackedEnsembleModelOutputV99.class);
                        default:
                            throw new JsonParseException("Unable to deserialize model of type " + algo);
                    }
                }
            }
            throw new JsonParseException("Invalid ModelOutputSchemaV3 element " + json.toString());
        }
    }

    /**
     * Factory method for parsing a ModelParametersSchemaV3 json object into an instance of the model-specific subclass.
     */
    private static class ModelParametersDeserializer implements JsonDeserializer<ModelParametersSchemaV3> {
        @Override
        public ModelParametersSchemaV3 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) return null;
            if (json.isJsonObject()) {
                JsonObject jobj = json.getAsJsonObject();
                if (jobj.has("algo")) {
                    String algo = jobj.get("algo").getAsJsonPrimitive().getAsString().toLowerCase();
                    switch (algo) {
                        case "deeplearning": return context.deserialize(json, DeepLearningParametersV3.class);
                        case "glm": return context.deserialize(json, GLMParametersV3.class);
                        case "glrm": return context.deserialize(json, GLRMParametersV3.class);
                        case "kmeans": return context.deserialize(json, KMeansParametersV3.class);
                        case "naivebayes": return context.deserialize(json, NaiveBayesParametersV3.class);
                        case "pca": return context.deserialize(json, PCAParametersV3.class);
                        case "svd": return context.deserialize(json, SVDParametersV99.class);
                        case "drf": return context.deserialize(json, DRFParametersV3.class);
                        case "gbm": return context.deserialize(json, GBMParametersV3.class);
                        case "aggregator": return context.deserialize(json, AggregatorParametersV99.class);
                        case "deepwater": return context.deserialize(json, DeepWaterParametersV3.class);
                        case "word2vec": return context.deserialize(json, Word2VecParametersV3.class);
                        case "stackedensemble": return context.deserialize(json, StackedEnsembleParametersV99.class);
                        default:
                            throw new JsonParseException("Unable to deserialize model of type " + algo);
                    }
                }
            }
            throw new JsonParseException("Invalid ModelParametersSchemaV3 element " + json.toString());
        }
    }


    private static class ModelV3TypeAdapter implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final Class<? super T> rawType = type.getRawType();
            if (!ModelBuilderSchema.class.isAssignableFrom(rawType) &&
                    !ModelSchemaBaseV3.class.isAssignableFrom(rawType)) return null;
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }
                @Override
                public T read(JsonReader in) throws IOException {
                    JsonObject jobj = new JsonParser().parse(in).getAsJsonObject();
                    if (jobj.has("parameters") && jobj.get("parameters").isJsonArray()) {
                        JsonArray jarr = jobj.get("parameters").getAsJsonArray();
                        JsonObject paramsNew = new JsonObject();
                        for (JsonElement item : jarr) {
                            JsonObject itemObj = item.getAsJsonObject();
                            paramsNew.add(itemObj.get("name").getAsString(), itemObj.get("actual_value"));
                        }
                        jobj.add("parameters", paramsNew);
                    }
                    // noinspection unchecked
                    return (T) new Gson().fromJson(jobj, rawType);
                }
            };
        }
    }


    /**
     * Return an array of Strings for an array of keys.
     */
    public static String[] keyArrayToStringArray(KeyV3[] keys) {
        if (keys == null) return null;
        String[] ids = new String[keys.length];
        int i = 0;
        for (KeyV3 key : keys) ids[i++] = key.name;
        return ids;
    }

    /**
     * Return an array of keys from an array of Strings.
     * @param ids array of string ids to convert to KeyV3's
     * @param clz class of key objects to create. Since we have JobKeyV3, FrameKeyV3, ModelKeyV3, etc -- this
     *            method needs to know which of these keys you want to create
     */
    public static <T extends KeyV3> T[] stringArrayToKeyArray(String[] ids, Class<T> clz) {
        if (ids == null) return null;
        // noinspection unchecked
        T[] keys = (T[]) Array.newInstance(clz, ids.length);
        String keyType = clz.getSimpleName();
        if (keyType.endsWith("KeyV3")) keyType = keyType.substring(0, keyType.length()-5);
        try {
            int i = 0;
            for (String id: ids) {
                keys[i] = clz.getConstructor().newInstance();
                keys[i].name = id;
                keys[i].type = keyType;
                i++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    /**
     *
     */
    public static String keyToString(KeyV3 key) {
        return key == null? null : key.name;
    }

    /**
     *
     */
    public static FrameKeyV3 stringToFrameKey(String key) {
        if (key == null) return null;
        FrameKeyV3 k = new FrameKeyV3();
        k.name = key;
        return k;
    }

    /**
     *
     */
    private static String colToString(ColSpecifierV3 col) {
        return col == null? null : col.columnName;
    }

    /**
     *
     */
    public static String stringToCol(String col) {
        if (col == null) return null;
        ColSpecifierV3 c = new ColSpecifierV3();
        c.columnName = col;
        return col;
    }


    public static void copyFields(Object to, Object from) {
        Field[] fromFields = from.getClass().getDeclaredFields();
        Field[] toFields   = to.getClass().getDeclaredFields();

        for (Field fromField : fromFields){
            Field toField;
            try {
                toField = to.getClass().getDeclaredField(fromField.getName());
                fromField.setAccessible(true);
                toField.setAccessible(true);
                toField.set(to, fromField.get(from));
            }
            catch (Exception ignored) {
                // NoSuchField is the normal case
            }
        }
    }
}
