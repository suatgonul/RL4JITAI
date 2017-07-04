package tez.environment.real;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static tez.util.LogUtil.*;

/**
 * Created by suat on 19-Jun-17.
 */
public class FirebaseClient {
    private static final String authorizationHeader = "key=AAAAU2sp_uk:APA91bFbE3H0eGoz9ABTNkp-EcfH-RVTh_TbSJeVJmxp5HhXQDsplvFLISVtWLPn1yciuS2XJr9ZPPJB6o_AdKWaFdWiSBehiL4bylXtF6X74lOfycKbyCG-f0-Jd21zo0aPHpYUoPl5";
    private static final Logger log = Logger.getLogger(FirebaseClient.class);
    private static Client client = ClientBuilder.newClient();
    private static WebTarget webResource = client.target("https://fcm.googleapis.com/fcm/send");

    public static void main(String[] args) {
        new FirebaseClient().sendNotificationToUsers(Arrays.asList("devid_1"));
    }

    public void sendNotificationToUsers(List<String> deviceIdentifiers) {
        log_generic(log, "Sending notification to users");
        for (String deviceIdentifier : deviceIdentifiers) {
            log_info(log, deviceIdentifier, "Sending notification");
            JsonObject notification = createData(deviceIdentifier);
            String notificationJson = notification.toString();
            log_info(log, deviceIdentifier, "Notification: " + notificationJson);
            Response response = webResource.request()
                    .header("Authorization", authorizationHeader)
                    .buildPost(Entity.entity(notificationJson, "application/json")).invoke();
            /*ClientResponse response = webResource.header("Authorization", authorizationHeader)
                    .header("Content-Type", "application/json")
                    .post(ClientResponse.class, notificationJson);*/

            log_info(log, deviceIdentifier, "Notification status: " + response.getStatus() + ", output: " + response.readEntity(String.class));

            NotificationManager.getInstance().registerNotification(deviceIdentifier, notification);
        }
    }

    private JsonObject createData(String deviceIdentifier) {
        JsonObject notification = new JsonObject();

        JsonObject data = new JsonObject();
        JsonPrimitive jsonPrimitive = new JsonPrimitive(UUID.randomUUID().toString());
        data.add("notificationId", jsonPrimitive);
        notification.add("data", data);

        JsonArray registrationIds = new JsonArray();
        registrationIds.add(UserRegistry.getInstance().getUser(deviceIdentifier).getRegistrationToken());
        notification.add("registration_ids", registrationIds);

        String notificationStr = notification.toString();
        log_info(log, deviceIdentifier, "notification data for firebase: \n" + notificationStr);
        return notification;
    }
}


