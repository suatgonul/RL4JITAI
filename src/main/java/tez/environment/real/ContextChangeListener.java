package tez.environment.real;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import tez.environment.context.*;

import java.util.*;

/**
 * Created by suat on 19-Jun-17.
 */
public class ContextChangeListener {
    private static ContextChangeListener instance;

    private Map<String, Context> latestContexts = new HashMap<>();
    private Map<String, Context> newContexts = new HashMap<>();
    private Map<String, Object> barriers = new HashMap<>();

    private ContextChangeListener() {

    }

    public static ContextChangeListener getInstance() {
        if(instance == null) {
            instance = new ContextChangeListener();
            instance.subscribeToUserContextChanges();
        }
        return instance;
    }

    private void subscribeToUserContextChanges() {
        Thread contextListenerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //Kafka consumer configuration settings
                String topicName = "raw.funf";
                Properties props = new Properties();

                props.put("bootstrap.servers", "localhost:9092");
                props.put("group.id", "test");
                props.put("enable.auto.commit", "true");
                props.put("auto.commit.interval.ms", "1000");
                props.put("session.timeout.ms", "30000");
                props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

                //Kafka Consumer subscribes list of topics here.
                consumer.subscribe(Arrays.asList(topicName));

                //print the topic name
                System.out.println("Subscribed to topic " + topicName);
                int i = 0;

                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(1000);
                    if (records.count() > 0) {
                        System.out.println("Update size: " + records.count());
                    }

                    // process the updates
                    List<Context> updatedContexts = new ArrayList<>();
                    for (ConsumerRecord<String, String> record : records) {

                        // print the offset,key and value for the consumer records.
                        System.out.printf("offset = %d, key = %s, value = %s\n",
                                record.offset(), record.key(), record.value());

                        Context updatedContext = processContextUpdate(record);
                        updatedContexts.add(updatedContext);
                    }

                    // notify the waiting environments for the updates
                    for(Context updatedContext : updatedContexts) {
                        String deviceIdentifier = updatedContext.getDeviceIdentifier();
                        boolean changed = false;
                        if(isContextChanged(updatedContext)) {
                            changed = true;
                        }
                        // update the latest context map
                        latestContexts.put(deviceIdentifier, updatedContext);

                        // notify the waiting environment if there is a change in the context parameters except time
                        if(changed) {
                            Object barrier = barriers.get(deviceIdentifier);
                            synchronized (barrier) {
                                newContexts.put(deviceIdentifier, updatedContext);
                                barrier.notify();
                            }
                        }
                    }
                }
            }
        });
        contextListenerThread.start();
    }

    private Context processContextUpdate(ConsumerRecord<String, String> consumerRecord) {
        String rawRecord = consumerRecord.value();
        JsonParser jsonParser = new JsonParser();
        JsonObject contextJson = jsonParser.parse(rawRecord).getAsJsonObject();
        String updateType = getUpdateType(contextJson);
        String deviceIdentifier = getDeviceIdentifier(contextJson, updateType);

        Context context = latestContexts.get(deviceIdentifier);
        if (context == null) {
            context = new Context(deviceIdentifier);
            latestContexts.put(deviceIdentifier, context);
        }
        Context updatedContext = context.copy();

        // check to skip the old updates coming from the topic
        if(!isRecentUpdate(contextJson, updateType)) {
            return null;
        }

        updatedContext.setTime(LocalTime.now());

        // check the schema id of the received context information
        if (contextJson.has("value")) {
            String probeName = contextJson.get("ProbeName").getAsString();
            if (probeName.contains("Screen")) {
                //{"PatientID":"864cb715ab0c67fc","ProbeName":"edu.mit.media.funf.probe.builtin.ScreenProbe","timestamp":"1.49786982E9","value":"{\"screenOn\":false,\"timestamp\":1497869774.445}"}
                Boolean value = rawRecord.charAt(rawRecord.indexOf("screenOn") + 11) == 'f' ? false : true;
                System.out.println("char: " + rawRecord.charAt(rawRecord.indexOf("screenOn") + 11));
                System.out.println("screen: " + value);
                if (value == true) {
                    updatedContext.setPhoneUsage(PhoneUsage.APPS_ACTIVE);
                } else {
                    updatedContext.setPhoneUsage(PhoneUsage.SCREEN_OFF);
                }

            } else if (probeName.contains("Activity")) {
                //String activityLevel = contextJson.get("value").getAsJsonObject().get("activityLevel").getAsString();
                char activityChar = rawRecord.charAt(rawRecord.indexOf("activityLevel") + 18);
                PhysicalActivity activityLevel = null;
                switch (activityChar) {
                    case 'h': activityLevel = PhysicalActivity.RUNNING; break;
                    case 'l': activityLevel = PhysicalActivity.WALKING; break;
                    case 'n': activityLevel = PhysicalActivity.SEDENTARY; break;
                }
                System.out.println("char: " + activityChar);
                System.out.println("activityLevel: " + activityLevel);
                // {"PatientID":"864cb715ab0c67fc","ProbeName":"edu.mit.media.funf.probe.builtin.ActivityProbe","timestamp":"1.49785882E9","value":"{\"activityLevel\":\"none\",\"timestamp\":1497858833.925}"}
                updatedContext.setPhysicalActivity(activityLevel);

            }
        } else if (contextJson.has("header")) {
            String contextType = contextJson.get("header").getAsJsonObject().get("schema_id").getAsJsonObject().get("name").getAsString();
            if (contextType.contentEquals("funf-wifi")) {
                // get location from wifi
                // {"body":{"effective_time_frame":{"date_time":"Jun 19, 2017 11:10:48 AM"},"values":[{"BSSID":"14:cc:20:9f:b9:a1","SSID":"akakce","isConnected":false,"level":-46},{"BSSID":"14:cc:20:c5:26:00","SSID":"srdc","isConnected":true,"level":-54},{"BSSID":"d8:50:e6:b4:4c:58","SSID":"HemoSilikon24","isConnected":false,"level":-59},{"BSSID":"d8:50:e6:b4:4c:59","SSID":"HemoSilikon_Konuk","isConnected":false,"level":-60},{"BSSID":"34:12:98:0c:c7:8a","SSID":"Alyo","isConnected":false,"level":-63},{"BSSID":"b4:b5:2f:19:38:ba","SSID":"HP-Print-BA-Officejet 6700","isConnected":false,"level":-70},{"BSSID":"82:2a:a8:42:52:16","SSID":"_wifi.land_","isConnected":false,"level":-71},{"BSSID":"92:2a:a8:42:52:16","SSID":"WIFI.LAND","isConnected":false,"level":-73},{"BSSID":"a2:6c:ac:a0:d7:7b","SSID":"infoTRON_Ankara_guest","isConnected":false,"level":-80},{"BSSID":"90:6c:ac:a0:d7:7b","SSID":"INFOTRON-Ankara","isConnected":false,"level":-81},{"BSSID":"8a:15:54:50:28:9c","SSID":"Udemy","isConnected":false,"level":-82},{"BSSID":"8e:15:54:50:28:9c","SSID":"udemy-guest","isConnected":false,"level":-82},{"BSSID":"82:2a:a8:42:4f:08","SSID":"_wifi.land_","isConnected":false,"level":-83},{"BSSID":"d8:50:e6:b4:4c:5c","SSID":"HemoSilikon50","isConnected":false,"level":-84},{"BSSID":"a2:6c:ac:a0:d7:83","SSID":"infoTRON_Ankara_guest","isConnected":false,"level":-84},{"BSSID":"90:6c:ac:a0:d7:83","SSID":"INFOTRON-Ankara","isConnected":false,"level":-84},{"BSSID":"92:2a:a8:42:4f:08","SSID":"WIFI.LAND","isConnected":false,"level":-84},{"BSSID":"18:28:61:3f:41:3f","SSID":"srdc2","isConnected":false,"level":-58},{"BSSID":"f4:f2:6d:8d:ce:52","SSID":"UDEA_Guest","isConnected":false,"level":-59},{"BSSID":"82:2a:a8:41:52:16","SSID":"WIFI.LAND","isConnected":false,"level":-64},{"BSSID":"80:2a:a8:41:52:16","SSID":"_wifi.land_","isConnected":false,"level":-64},{"BSSID":"90:ef:68:2e:6f:84","SSID":"akakce2","isConnected":false,"level":-67},{"BSSID":"00:14:c1:03:f4:4e","SSID":"hemosoft","isConnected":false,"level":-69},{"BSSID":"14:cc:20:c2:38:0c","SSID":"BILGIAS","isConnected":false,"level":-71},{"BSSID":"34:12:98:0c:bb:e2","SSID":"Alyo","isConnected":false,"level":-73},{"BSSID":"18:28:61:ef:1c:f1","SSID":"SPAC_WS","isConnected":false,"level":-75},{"BSSID":"8e:15:44:50:28:9c","SSID":"","isConnected":false,"level":-75},{"BSSID":"80:2a:a8:41:4f:08","SSID":"_wifi.land_","isConnected":false,"level":-77},{"BSSID":"32:cd:a7:3d:24:48","SSID":"DIRECT-2QM2070 Series","isConnected":false,"level":-77},{"BSSID":"b0:48:7a:fa:cb:1c","SSID":"A_BILGI_TEK","isConnected":false,"level":-80},{"BSSID":"8e:15:54:50:22:d8","SSID":"udemy-guest","isConnected":false,"level":-82},{"BSSID":"34:12:98:0c:bb:e3","SSID":"Alyo","isConnected":false,"level":-85},{"BSSID":"c4:6e:1f:a1:ec:fa","SSID":"ABYS5","isConnected":false,"level":-87},{"BSSID":"88:41:fc:1b:b2:67","SSID":"SBG_Ankara","isConnected":false,"level":-90}]},"header":{"creation_date_time":"Jun 19, 2017 11:10:48 AM","id":"1f724cbf-37d9-44ab-a5c7-f5f0d2d6445f","patient_id":"864cb715ab0c67fc","schema_id":{"name":"funf-wifi","namespace":"omh","version":"1.0"}},"timestamp":1497859848.263})
                Iterator<JsonElement> it = contextJson.get("body").getAsJsonObject().get("values").getAsJsonArray().iterator();
                boolean foundLocation = false;
                String homeWifi = UserRegistry.getInstance().getUser(deviceIdentifier).getWifiName();
                while (it.hasNext()) {
                    String wifi = it.next().getAsJsonObject().get("SSID").getAsString();
                    if (wifi.startsWith("srdc")) {
                        updatedContext.setLocation(Location.OFFICE);
                        foundLocation = true;
                    } else if (wifi.contentEquals(homeWifi)) {
                        updatedContext.setLocation(Location.HOME);
                        foundLocation = true;
                    }
                }
                if (!foundLocation) {
                    updatedContext.setLocation(Location.OUTSIDE);
                }
                System.out.println("location: " + updatedContext.getLocation());
            }
        }

        System.out.println("New context parsed from consumer record");
        return context;
    }

    private boolean isContextChanged(Context updated) {
        Context old = latestContexts.get(updated.getDeviceIdentifier());

        if(old.getLocation() != updated.getLocation()
                || old.getPhoneUsage() != updated.getPhoneUsage()
                || old.getPhysicalActivity() != updated.getPhysicalActivity()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isRecentUpdate(JsonObject contextJson, String updateType) {
        DateTime updateTime;
        String updateTimeStr;

        if(updateType.contains("Screen") || updateType.contentEquals("Activity")) {
            updateTimeStr = contextJson.get("timestamp").getAsString();
            updateTime = new DateTime(Double.valueOf(updateTimeStr).longValue() * 1000);
        } else {
            updateTimeStr = contextJson.get("header").getAsJsonObject().get("creation_date_time").getAsString();
            DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM dd, yyyy KK:mm:ss aa");
            updateTime = DateTime.parse(updateTimeStr, dtf);
        }

        // if the update was created at least 1 minute before now, skip it
        DateTime oneMinuteBeforeNow = DateTime.now().minusMinutes(1);
        return updateTime.isAfter(oneMinuteBeforeNow);
    }

    private String getDeviceIdentifier(JsonObject contextJson, String updateType) {
        if(updateType.contains("Screen") || updateType.contentEquals("Activity")) {
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
            if (newContexts.get(deviceIdentifier) == null) {
                try {
                    System.out.println("Processing will stop for: " + deviceIdentifier);
                    barrier.wait();
                    System.out.println("Processing continues for: " + deviceIdentifier);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            context = newContexts.get(deviceIdentifier);
            newContexts.remove(deviceIdentifier);
        }
        return context;
    }
}
