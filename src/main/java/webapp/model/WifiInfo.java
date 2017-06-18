package webapp.model;

/**
 * Created by suat on 18-Jun-17.
 */
public class WifiInfo {
    private String deviceIdentifier;
    private String wifiName;

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }
}
