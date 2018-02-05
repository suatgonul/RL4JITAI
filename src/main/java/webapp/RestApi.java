package webapp;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import tez.environment.real.ContextChangeListener;
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
import java.util.List;

import static tez.util.LogUtil.log_info;

/**
 * Created by suat on 17-Jun-17.
 */
@Path("/")
public class RestApi {
    private static final Logger log = Logger.getLogger(RestApi.class);

    @Path("devicetoken")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response postDeviceToken(String deviceTokenJson) {
        log.info("Received device token info: " + deviceTokenJson);
        DeviceTokenInfo deviceTokenInfo = TransformationUtil.parseDeviceToken(deviceTokenJson);
        UserRegistry.getInstance().addRegistrationToken(deviceTokenInfo);
        ContextChangeListener.getInstance().onNewUser(deviceTokenInfo.getDeviceIdentifier());
        log_info(log, deviceTokenInfo.getDeviceIdentifier(), "Added device token: " + deviceTokenInfo.getRegistrationToken());
        return Response.ok().build();
    }

    @Path("reaction")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response postReactionResult(String reactionResultJson) {
        log.info("Received reaction results info: " + reactionResultJson);
        ReactionResult reactionResult = TransformationUtil.parseReactionResult(reactionResultJson);
        NotificationManager.getInstance().processUserReaction(reactionResult);
        log_info(log, reactionResult.getDeviceIdentifier(), "Processed reaction results notification: " + reactionResult.getInterventionId());
        return Response.ok().build();
    }

    @Path("wifiname")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response postWifiName(String wifiNameJson) {
        log.info("Received wifi info: " + wifiNameJson);
        WifiInfo wifiInfo = TransformationUtil.parseWifiInfo(wifiNameJson);
        UserRegistry.getInstance().addWifiInfo(wifiInfo);
        RealExperimentManager.getInstance().runExperiment(wifiInfo.getDeviceIdentifier());
        log_info(log, wifiInfo.getDeviceIdentifier(), "Processed wifi info");
        return Response.ok().build();
    }

    @Path("setcontrol/{deviceIdentifier}")
    @POST
    @Produces("text/plain")
    public Response setControlGroupUser(@PathParam("deviceIdentifier") String deviceIdentifier, String control) {
        log.info("Received control group: " + control + " for: " + deviceIdentifier);
        boolean controlBool;
        if (control.startsWith("\"")) {
            controlBool = Boolean.valueOf(control.substring(1, control.length() - 1));
        } else {
            controlBool = Boolean.valueOf(control);
        }
        log_info(log, deviceIdentifier, "Control: " + controlBool);
        UserRegistry.getInstance().setControlGroup(deviceIdentifier, controlBool);
        RealExperimentManager.getInstance().runExperiment(deviceIdentifier);
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
