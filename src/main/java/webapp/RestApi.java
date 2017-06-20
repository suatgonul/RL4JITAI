package webapp;

import com.google.gson.Gson;
import tez.environment.real.NotificationManager;
import tez.environment.real.UserRegistry;
import tez.experiment.real.RealExperimentManager;
import webapp.model.DeviceTokenInfo;
import webapp.model.ReactionResult;
import webapp.model.TransformationUtil;
import webapp.model.WifiInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by suat on 17-Jun-17.
 */
@Path("/")
public class RestApi {
    @Path("devicetoken")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response postDeviceToken(String deviceTokenJson) {
        System.out.println("Received device token info: " + deviceTokenJson);
        DeviceTokenInfo deviceTokenInfo = TransformationUtil.parseDeviceToken(deviceTokenJson);
        UserRegistry.getInstance().addRegistrationToken(deviceTokenInfo);
        return Response.ok().build();
    }

    @Path("reaction")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response postReactionResult(String reactionResultJson) {
        System.out.println("Received reaction results info: " + reactionResultJson);
        ReactionResult reactionResult = TransformationUtil.parseReactionResult(reactionResultJson);
        NotificationManager.getInstance().processUserReaction(reactionResult);
        return Response.ok().build();
    }

    @Path("wifiname")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response postWifiName(String wifiNameJson) {
        System.out.println("Received wifi info: " + wifiNameJson);
        WifiInfo wifiInfo = TransformationUtil.parseWifiInfo(wifiNameJson);
        UserRegistry.getInstance().addWifiInfo(wifiInfo);
        boolean settingsConfigured = UserRegistry.getInstance().isSettingsConfigured(wifiInfo.getDeviceIdentifier());
        if(settingsConfigured) {
            RealExperimentManager.getInstance().runExperiment(wifiInfo.getDeviceIdentifier());
        }
        return Response.ok().build();
    }

    @Path("setcontrol/{deviceIdentifier}")
    @POST
    @Produces("text/plain")
    public Response setControlGroupUser(@PathParam("deviceIdentifier") String deviceIdentifier, String control) {
        System.out.println("Received control group: " + control + " for: " + deviceIdentifier);
        UserRegistry.getInstance().setControlGroup(deviceIdentifier, control);
        boolean settingsConfigured = UserRegistry.getInstance().isSettingsConfigured(deviceIdentifier);
        if(settingsConfigured) {
            RealExperimentManager.getInstance().runExperiment(deviceIdentifier);
        }
        return Response.ok().build();
    }

    @Path("users")
    @GET
    @Produces("application/json")
    public Response getUsers() {
        List<UserRegistry.UserData> users = UserRegistry.getInstance().getUsers();
        Gson gson = new Gson();
        String jsonInString = gson.toJson(users);
        return Response.ok(jsonInString, MediaType.APPLICATION_JSON).build();
    }

}
