package tez2.environment.real.control;

import org.apache.kafka.common.network.Send;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tez.environment.real.FirebaseClient;
import tez.environment.real.NotificationManager;
import tez.environment.real.UserRegistry;
import tez.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 19-Jun-17.
 */
public class SendNotificationJob implements Job {
    private static final Logger log = Logger.getLogger(tez.environment.real.control.SendNotificationJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LogUtil.log_generic(log, "Notification job started");
        List<UserRegistry.UserData> controlGroup = UserRegistry.getInstance().getControlGroup();
        List<String> deviceIdentifiers = new ArrayList<>();

        for(UserRegistry.UserData userData : controlGroup) {
            deviceIdentifiers.add(userData.getDeviceIdentifier());
        }
        NotificationManager.getInstance().sendNotificationToUsers(deviceIdentifiers);
        LogUtil.log_generic(log, "Notification job ended");
    }
}
