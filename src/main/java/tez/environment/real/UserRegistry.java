package tez.environment.real;

import webapp.model.DeviceTokenInfo;
import webapp.model.WifiInfo;

import java.util.*;

/**
 * Created by suat on 19-Jun-17.
 */
public class UserRegistry {
    // keys are device identifiers
    private Map<String, UserData> users = Collections.synchronizedMap(new HashMap<>());

    private static UserRegistry registry;

    public static UserRegistry getInstance() {
        if(registry == null) {
            registry = new UserRegistry();
        }
        return registry;
    }

    private UserRegistry() {
        UserData ud = new UserData();
        ud.setControl(true);
        ud.setWifiName("gsev");
        ud.setRegistrationToken("cxKcMfmf-aE:APA91bE8ZKbbOOrd2ayt2ISvInWnqqdEJ-30a32pWap6oV7avfj4H_wbewYRrtxXd0HvLjXctcvm0T4HL5VQf3bz-BIDtV4ZugJyRH3kKYs_4J3uU4WSCa30ogkQT3X8k2sMjvcdLlTm");
        ud.setDeviceIdentifier("devid_1");
        UserData ud2 = new UserData();
        ud2.setWifiName("gsev");
        ud2.setRegistrationToken("regtok2");
        ud2.setDeviceIdentifier("devid_2");
        users.put(ud.getDeviceIdentifier(), ud);
        users.put(ud2.getDeviceIdentifier(), ud2);
    }

    public void addWifiInfo(WifiInfo wifiInfo) {
        String deviceIdentifier = wifiInfo.getDeviceIdentifier();
        UserData userData = createUserIfNotExists(deviceIdentifier);
        userData.setWifiName(wifiInfo.getWifiName());
    }

    public void addRegistrationToken(DeviceTokenInfo deviceTokenInfo) {
        String deviceIdentifier = deviceTokenInfo.getDeviceIdentifier();
        UserData userData = createUserIfNotExists(deviceIdentifier);
        userData.setRegistrationToken(deviceTokenInfo.getRegistrationToken());
    }

    public void setControlGroup(String deviceIdentifier, String control) {
        UserData userData = createUserIfNotExists(deviceIdentifier);
        userData.setControl(Boolean.valueOf(control));
    }

    public boolean isSettingsConfigured(String deviceIdentifier) {
        UserData userData = UserRegistry.getInstance().getUser(deviceIdentifier);
        if(userData.getWifiName() == null) {
            return false;
        }
        if(userData.isControl() == null) {
            return false;
        }
        return true;
    }

    public UserData getUser(String deviceIdentifier) {
        return users.get(deviceIdentifier);
    }

    public List<UserData> getUsers() {
        return new ArrayList<>(users.values());
    }

    public List<UserData> getControlGroup() {
        List<UserData> controlGroup = new ArrayList<>();
        for(UserData userData : users.values()) {
            if(userData.isControl()) {
                controlGroup.add(userData);
            }
        }
        return controlGroup;
    }

    private UserData createUserIfNotExists(String deviceIdentifier) {
        UserData userData = users.get(deviceIdentifier);
        if(userData == null) {
            userData = new UserData();
            userData.setDeviceIdentifier(deviceIdentifier);
            users.put(deviceIdentifier, userData);
        }
        return userData;
    }

    public class UserData {
        String deviceIdentifier;
        String registrationToken;
        String wifiName;
        Boolean isControl;

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

        public String getWifiName() {
            return wifiName;
        }

        public void setWifiName(String wifiName) {
            this.wifiName = wifiName;
        }

        public Boolean isControl() {
            return isControl;
        }

        public void setControl(Boolean control) {
            isControl = control;
        }
    }
}
