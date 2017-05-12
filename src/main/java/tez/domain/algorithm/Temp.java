package tez.domain.algorithm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import retrofit2.Retrofit;
import water.AutoBuffer;
import water.api.ImportFilesHandler;
import water.api.schemas3.JobV3;
import water.api.schemas3.KeyV3;
import water.bindings.H2oApi;
import water.bindings.pojos.ImportFilesV3;
import water.bindings.proxies.retrofit.ImportFiles;
import water.parser.ParseSetup;

import java.io.IOException;

/**
 * Created by suat on 12-May-17.
 */
public class Temp {
    public static void main(String[] args) {
        try {
            H2oApi h2o = new H2oApi();

            try {
                // Utility var:
                JobV3 job = null;

                // STEP 0: init a session
                String sessionId = h2o.newSession().sessionKey;


                // STEP 1: import raw file
                ImportFilesV3 importBody = h2o.importFiles(
                        "D:\\mine\\odtu\\6\\tez\\codes\\data\\tempStates.csv"
                );
                System.out.println("import: " + importBody);
            }
            catch (IOException e) {
                System.err.println("Caught exception: " + e);
            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }
}
