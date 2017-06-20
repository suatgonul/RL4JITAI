package webapp.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by suat on 18-Jun-17.
 */
public class TransformationUtil {
    public static DeviceTokenInfo parseDeviceToken(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject contextJson = jsonParser.parse(json).getAsJsonObject();
        DeviceTokenInfo deviceTokenInfo = new DeviceTokenInfo();
        deviceTokenInfo.setDeviceIdentifier(contextJson.get("deviceIdentifier").getAsString());
        deviceTokenInfo.setRegistrationToken(contextJson.get("registrationToken").getAsString());
        return deviceTokenInfo;
    }

    public static ReactionResult parseReactionResult(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject contextJson = jsonParser.parse(json).getAsJsonObject();
        ReactionResult reactionResult = new ReactionResult();
        reactionResult.setReaction(contextJson.get("reaction").getAsString());
        reactionResult.setInterventionId(contextJson.get("notificationId").getAsString());
        reactionResult.setDeviceIdentifier(contextJson.get("deviceIdentifier").getAsString());
        return reactionResult;
    }

    public static WifiInfo parseWifiInfo(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject contextJson = jsonParser.parse(json).getAsJsonObject();
        WifiInfo wifiInfo = new WifiInfo();
        wifiInfo.setDeviceIdentifier(contextJson.get("deviceIdentifier").getAsString());
        wifiInfo.setWifiName(contextJson.get("wifiName").getAsString());
        return wifiInfo;
    }
}
