package tez2.environment.real;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import tez.environment.context.*;
import tez.environment.real.UserRegistry;

import java.util.*;

import static tez.util.LogUtil.*;

/**
 * Created by suat on 19-Jun-17.
 */
public class ContextChangeListener {
    private static final Logger log = Logger.getLogger(tez.environment.real.ContextChangeListener.class);
    private static tez.environment.real.ContextChangeListener instance;
    private Thread listenerThread;
    // this map keeps the recently processed contexts
    private Map<String, Context> latestContexts = new HashMap<>();
    // learning thread retrieves the context from this map
    //private Map<String, Context> newContexts = new HashMap<>();
    private Map<String, Object> barriers = new HashMap<>();

    private ContextChangeListener() {

    }

    public static tez.environment.real.ContextChangeListener getInstance() {
        if (instance == null) {
            instance = new tez.environment.real.ContextChangeListener();
        }
        return instance;
    }

    public void subscribeToUserContextChanges() {
        listenerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //Kafka consumer configuration settings
                String topicName = "raw.funf";
                Properties props = new Properties();

                props.put("bootstrap.servers", "localhost:9092");
                props.put("group.id", "test");
                props.put("enable.auto.commit", "true");
                props.put("auto.offset.reset", "latest");
                props.put("auto.commit.interval.ms", "1000");
                props.put("session.timeout.ms", "30000");
                props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

                //Kafka Consumer subscribes list of topics here.
                consumer.subscribe(Arrays.asList(topicName));

                //print the topic name
                log_generic(log, "Subscribed to topic " + topicName);
                int i = 0;

                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(5000);
                    if (records.count() > 0) {
                        log_generic(log, "Update size: " + records.count());
                    }

                    // process the updates
                    Map<String, Context> contextsProcessedInBatch = new HashMap<>();
                    for (ConsumerRecord<String, String> record : records) {
                        processContextUpdate(record, contextsProcessedInBatch);
                        /*Context updatedContext = processContextUpdate(record, contextsProcessedInBatch);
                        if (updatedContext != null) {
                            contextsProcessedInBatch.put(updatedContext.getDeviceIdentifier(), updatedContext);
                        }*/
                    }

                    // notify the waiting environments for the updates
                    for (Context updatedContext : contextsProcessedInBatch.values()) {
                        String deviceIdentifier = updatedContext.getDeviceIdentifier();

                        boolean changed = false;
                        if (isContextChanged(updatedContext)) {
                            changed = true;
                        }

                        latestContexts.put(deviceIdentifier, updatedContext);

                        // notify the waiting environment if there is a change in the context parameters except time
                        if (changed) {
                            new Timer().schedule(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            Object barrier = barriers.get(deviceIdentifier);
                                            if(barrier != null) {
                                                synchronized (barrier) {
                                                    //newContexts.put(deviceIdentifier, updatedContext);
                                                    log_info(log, deviceIdentifier, "Waiting thread on context change will be notified");
                                                    barrier.notify();
                                                }
                                            }
                                        }
                                    },
                                    3000
                            );
                        }
                    }
                }
            }
        });
        listenerThread.start();
    }

    public void stopListening() {
        listenerThread.stop();
    }

    private void processContextUpdate(ConsumerRecord<String, String> consumerRecord, Map<String, Context> contextsProcessedInBatch) {
        String rawRecord = consumerRecord.value();
        JsonParser jsonParser = new JsonParser();
        JsonObject contextJson = jsonParser.parse(rawRecord).getAsJsonObject();
        String updateType = getUpdateType(contextJson);
        String deviceIdentifier = getDeviceIdentifier(contextJson, updateType);

        // check to skip the old updates coming from the topic
        if (!isRecentUpdate(contextJson, deviceIdentifier, updateType)) {
            return;
        }

        Context context = contextsProcessedInBatch.get(deviceIdentifier);
        if(context == null) {
            Context latestContext = latestContexts.get(deviceIdentifier);
            if(latestContext == null) {
                latestContext = new Context(deviceIdentifier);
                latestContexts.put(deviceIdentifier, latestContext);

            }
            context = latestContext.copy();
            contextsProcessedInBatch.put(deviceIdentifier, context);
        }

        context.setTime(LocalTime.now());

        // check the schema id of the received context information
        if (contextJson.has("value")) {
            String probeName = contextJson.get("ProbeName").getAsString();
            if (probeName.contains("Screen")) {
                //{"PatientID":"864cb715ab0c67fc","ProbeName":"edu.mit.media.funf.probe.builtin.ScreenProbe","timestamp":"1.49786982E9","value":"{\"screenOn\":false,\"timestamp\":1497869774.445}"}
                Boolean value = rawRecord.charAt(rawRecord.indexOf("screenOn") + 11) == 'f' ? false : true;
                if (value == true) {
                    context.setPhoneUsage(PhoneUsage.APPS_ACTIVE);
                } else {
                    context.setPhoneUsage(PhoneUsage.SCREEN_OFF);
                }
                log_info(log, deviceIdentifier, "new screen status: " + context.getPhoneUsage());

            } else if (probeName.contains("Activity")) {
                //String activityLevel = contextJson.get("value").getAsJsonObject().get("activityLevel").getAsString();
                char activityChar = rawRecord.charAt(rawRecord.indexOf("activityLevel") + 18);
                PhysicalActivity activityLevel = null;
                switch (activityChar) {
                    case 'h':
                        activityLevel = PhysicalActivity.RUNNING;
                        break;
                    case 'l':
                        activityLevel = PhysicalActivity.WALKING;
                        break;
                    case 'n':
                        activityLevel = PhysicalActivity.SEDENTARY;
                        break;
                }
                // {"PatientID":"864cb715ab0c67fc","ProbeName":"edu.mit.media.funf.probe.builtin.ActivityProbe","timestamp":"1.49785882E9","value":"{\"activityLevel\":\"none\",\"timestamp\":1497858833.925}"}
                context.setPhysicalActivity(activityLevel);
                log_info(log, deviceIdentifier, "new activity level: " + context.getPhysicalActivity());
            } else if (probeName.contains("Wifi")) {
                UserRegistry.UserData user = UserRegistry.getInstance().getUser(deviceIdentifier);
                if (user == null) {
                    log_info(log, deviceIdentifier, "Skipping wifi update as user is not registered");
                    return;
                }

                String homeWifi = UserRegistry.getInstance().getUser(deviceIdentifier).getWifiName();
                if (homeWifi == null) {
                    log_info(log,deviceIdentifier, "Skipping wifi update as home wifi is not set");
                    return;
                }

                if (rawRecord.contains("srdc")) {
                    context.setLocation(Location.OFFICE);
                } else if (rawRecord.contains(homeWifi)) {
                    context.setLocation(Location.HOME);
                } else {
                    context.setLocation(Location.OUTSIDE);
                }
                log_info(log, deviceIdentifier, "new location: " + context.getLocation());
            }
        }
    }

    private boolean isContextChanged(Context updated) {
        Context old = latestContexts.get(updated.getDeviceIdentifier());
        log_info(log, updated.getDeviceIdentifier(), "Old context: " + old.toString());
        log_info(log, updated.getDeviceIdentifier(), "Updated context: " + updated.toString());

        if (old.getLocation() != updated.getLocation()
                || old.getPhoneUsage() != updated.getPhoneUsage()
                || old.getPhysicalActivity() != updated.getPhysicalActivity()) {
            log_info(log, updated.getDeviceIdentifier(), "Context changed");
            return true;
        } else {
            return false;
        }
    }

    private boolean isRecentUpdate(JsonObject contextJson, String deviceIdentifier, String updateType) {
        DateTime updateTime;
        String updateTimeStr;

        if (updateType.contains("Screen") || updateType.contentEquals("Activity")) {
            updateTimeStr = contextJson.get("timestamp").getAsString();
            updateTime = new DateTime(Double.valueOf(updateTimeStr).longValue() * 1000);
        } else {
            updateTimeStr = contextJson.get("header").getAsJsonObject().get("creation_date_time").getAsString();
            DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM dd, yyyy KK:mm:ss aa");
            updateTime = DateTime.parse(updateTimeStr, dtf);
        }

        // if the update was created at least 1 minute before now, skip it
        DateTime fiveMinuteBeforeNow = DateTime.now().minusMinutes(5);
        boolean isRecent = updateTime.isAfter(fiveMinuteBeforeNow);
        if (!isRecent) {
            log_info(log, deviceIdentifier,"Skipping " + updateType + " update, current time: " + DateTime.now() + " update time: " + fiveMinuteBeforeNow);
        }
        return isRecent;
    }

    private String getDeviceIdentifier(JsonObject contextJson, String updateType) {
        if (updateType.contains("Screen") || updateType.contentEquals("Activity")) {
            return contextJson.get("PatientID").getAsString();
        } else {
            return contextJson.get("header").getAsJsonObject().get("patient_id").getAsString();
        }
    }

    private String getUpdateType(JsonObject contextJson) {
        String updateType;
        if (contextJson.has("value")) {
            String probeName = contextJson.get("ProbeName").getAsString();
            if (probeName.contains("Screen")) {
                return "Screen";
            } else {
                return "Activity";
            }
        } else {
            return "Wifi";
        }
    }

    public void onNewUser(String deviceIdentifier) {
        barriers.put(deviceIdentifier, new Object());
    }

    public Context getUpdatedContext(String deviceIdentifier) {
        Object barrier = barriers.get(deviceIdentifier);
        Context context;

        synchronized (barrier) {
            //if (latestContexts.get(deviceIdentifier) == null) {
            try {
                log_info(log, deviceIdentifier, "Processing will stop");
                barrier.wait();
                log_info(log, deviceIdentifier, "Processing continues");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //}
            context = latestContexts.get(deviceIdentifier);
            //newContexts.remove(deviceIdentifier);
        }
        return context;
    }
}
