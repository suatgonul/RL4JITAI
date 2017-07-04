package tez.environment.real;

import org.apache.log4j.Logger;
import tez.util.LogUtil;
import webapp.model.DeviceTokenInfo;
import webapp.model.WifiInfo;

import java.util.*;

/**
 * Created by suat on 19-Jun-17.
 */
public class UserRegistry {
    private static final Logger log = Logger.getLogger(UserRegistry.class);

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
        /*UserData ud = new UserData();
        ud.setControl(true);
        ud.setWifiName("gsev");
        ud.setRegistrationToken("cxKcMfmf-aE:APA91bE8ZKbbOOrd2ayt2ISvInWnqqdEJ-30a32pWap6oV7avfj4H_wbewYRrtxXd0HvLjXctcvm0T4HL5VQf3bz-BIDtV4ZugJyRH3kKYs_4J3uU4WSCa30ogkQT3X8k2sMjvcdLlTm");
        ud.setDeviceIdentifier("devid_1");
        UserData ud2 = new UserData();
        ud2.setWifiName("gsev");
        ud2.setRegistrationToken("regtok2");
        ud2.setDeviceIdentifier("devid_2");
        users.put(ud.getDeviceIdentifier(), ud);
        users.put(ud2.getDeviceIdentifier(), ud2);*/
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

    public void setControlGroup(String deviceIdentifier, Boolean control) {
        UserData userData = createUserIfNotExists(deviceIdentifier);
        userData.setControl(control);
    }

    public boolean isSettingsConfigured(String deviceIdentifier) {
        UserData userData = UserRegistry.getInstance().getUser(deviceIdentifier);
        if(userData.getWifiName() == null) {
            return false;
        }
        if(userData.isControlSet() == false) {
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
        if(users.size() > 0) {
            for (UserData userData : users.values()) {
                if (isSettingsConfigured(userData.getDeviceIdentifier()) && userData.isControl()) {
                    controlGroup.add(userData);
                }
            }
        }
        LogUtil.log_generic(log, "Control group size: " + controlGroup.size());
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
        private String deviceIdentifier;
        private String registrationToken;
        private String wifiName;
        private boolean isControl = false;
        private boolean isControlSet = false;

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

        public boolean isControl() {
            return isControl;
        }

        public void setControl(boolean control) {
            isControlSet = true;
            isControl = control;
        }

        public boolean isControlSet() {
            return isControlSet;
        }
    }
}
