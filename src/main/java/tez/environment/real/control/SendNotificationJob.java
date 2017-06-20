package tez.environment.real.control;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tez.environment.real.FirebaseClient;
import tez.environment.real.NotificationManager;
import tez.environment.real.UserRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 19-Jun-17.
 */
public class SendNotificationJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("Notification job started");
        List<UserRegistry.UserData> controlGroup = UserRegistry.getInstance().getControlGroup();
        List<String> deviceIdentifiers = new ArrayList<>();

        for(UserRegistry.UserData userData : controlGroup) {
            deviceIdentifiers.add(userData.getDeviceIdentifier());
        }
        NotificationManager.getInstance().sendNotificationToUsers(deviceIdentifiers);
        System.out.println("Notification job ended");
    }
}
