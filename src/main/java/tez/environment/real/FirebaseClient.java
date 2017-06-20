package tez.environment.real;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by suat on 19-Jun-17.
 */
public class FirebaseClient {
    private static final String authorizationHeader = "key=AAAAU2sp_uk:APA91bFbE3H0eGoz9ABTNkp-EcfH-RVTh_TbSJeVJmxp5HhXQDsplvFLISVtWLPn1yciuS2XJr9ZPPJB6o_AdKWaFdWiSBehiL4bylXtF6X74lOfycKbyCG-f0-Jd21zo0aPHpYUoPl5";
    private static Client client = Client.create();
    private static WebResource webResource = client.resource("https://fcm.googleapis.com/fcm/send");

    public static void main(String[] args) {
        new FirebaseClient().sendNotificationToUsers(Arrays.asList("devid_1"));
    }

    public void sendNotificationToUsers(List<String> deviceIdentifiers) {
        for (String deviceIdentifier : deviceIdentifiers) {
            JsonObject notification = createData(deviceIdentifier);
            String notificationJson = notification.toString();
            ClientResponse response = webResource.header("Authorization", authorizationHeader)
                    .header("Content-Type", "application/json")
                    .post(ClientResponse.class, notificationJson);

            System.out.println("Send notification for device ids: " + String.join(", ", deviceIdentifiers) + " status: " + response.getStatus() + ", output: " + response.getEntity(String.class));

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
        System.out.println("notification data for firebase below: \n" + notificationStr);
        return notification;
    }
}


