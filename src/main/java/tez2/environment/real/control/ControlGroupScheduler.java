package tez2.environment.real.control;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import spire.algebra.Trig;
import tez.environment.real.control.SendNotificationJob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by suat on 19-Jun-17.
 */
public class ControlGroupScheduler {
    private static ControlGroupScheduler instance;

    private Scheduler scheduler;

    private ControlGroupScheduler() {

    }

    public static ControlGroupScheduler getInstance() {
        if (instance == null) {
            instance = new ControlGroupScheduler();
        }
        return instance;
    }

    public void startNotificationSender() {
        JobDetail notificationJob = JobBuilder.newJob(SendNotificationJob.class)
                .withIdentity("notificationJob").build();

        Trigger trigger1 = TriggerBuilder.newTrigger()
                .withIdentity("trigger1")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(10, 30))
                .build();

        Trigger trigger2 = TriggerBuilder.newTrigger()
                .withIdentity("trigger2")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(14, 0))
                .build();

        Trigger trigger3 = TriggerBuilder.newTrigger()
                .withIdentity("trigger3")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(16, 30))
                .build();

        Trigger trigger4 = TriggerBuilder.newTrigger()
                .withIdentity("trigger4")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(20, 30))
                .build();

        // each minute
        Trigger trigger5 = TriggerBuilder.newTrigger()
                .withIdentity("trigger5")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *"))
                .build();

        Set<Trigger> triggers = new HashSet<>();
        triggers.add(trigger1);
        triggers.add(trigger2);
        triggers.add(trigger3);
        triggers.add(trigger4);
        //triggers.add(trigger5);

        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(notificationJob, triggers, false);
            System.out.println("Control group scheduler started");
        } catch (SchedulerException e) {
            System.out.println("Failed to start notification scheduler");
            e.printStackTrace();
        }
    }

    public void stopNotificationSender() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            System.out.println("Failed to stop notification scheduler");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        tez.environment.real.control.ControlGroupScheduler.getInstance().startNotificationSender();
    }
}
