package webapp.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by suat on 18-Jun-17.
 */
public class TransformationUtil {
    public DeviceToken parseDeviceToken(String json) {
        return null;
    }

    public ReactionResult parseReactionResults(String json) {
        return null;
    }

    public WifiInfo parseWifiInfo(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject contextJson = jsonParser.parse(json).getAsJsonObject();
        WifiInfo wifiInfo = new WifiInfo();
        wifiInfo.setDeviceIdentifier(contextJson.get("deviceIdentifier").getAsString());
        wifiInfo.setWifiName(contextJson.get("wifiName").getAsString());
        return wifiInfo;
    }
}
