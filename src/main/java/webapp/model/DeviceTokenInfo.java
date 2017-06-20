package webapp.model;

/**
 * Created by suat on 17-Jun-17.
 */
public class DeviceTokenInfo {
    private String deviceIdentifier;
    private String registrationToken;

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }
}
