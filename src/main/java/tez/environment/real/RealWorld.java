package tez.environment.real;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalTime;
import org.json.JSONObject;
import tez.domain.*;
import tez.environment.SelfManagementEnvironment;
import tez.environment.context.*;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static tez.domain.SelfManagementDomainGenerator.*;
import static tez.domain.SelfManagementDomainGenerator.ATT_STATE_OF_MIND;

/**
 * Created by suat on 26-May-17.
 */
public class RealWorld extends SelfManagementEnvironment {
    private DateTime trialStartDate;
    private int trialLength;

    private Context currentContext;
    private Context recentlyProcessedContext;
    private BlockingQueue<Context> changedContexts;

    public RealWorld(Domain domain, RewardFunction rf, TerminalFunction tf, int stateChangeFrequency, Object... params) {
        super(domain, rf, tf, stateChangeFrequency, params);
        currentTime = DateTime.now();

        publishDummyContextToTopic();
        System.out.println("Publishing dummy context data");
        subscribeToUserContextChanges();
        System.out.println("Subscribed to user context changes");
    }

    public static void main(String[] args) {
        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        TerminalFunction tf = new DayTerminalFunction();
        RewardFunction rf = new SelfManagementRewardFunction();

        new RealWorld(domain, rf, tf, 1, 1000);
    }

    @Override
    protected void initParameters(Object... params) {
        trialLength = (Integer) params[0];
        changedContexts = new ArrayBlockingQueue<Context>(1000);
        trialStartDate = DateTime.now();
    }

    @Override
    public State getNextState() {
        synchronized (changedContexts) {
            if (changedContexts.size() == 0) {
                try {
                    System.out.println("Processing will stop");
                    changedContexts.wait();
                    System.out.println("Processing continues");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        currentContext = changedContexts.poll();
        System.out.println("Polled context");
        State s = getStateFromCurrentContext();
        return s;
    }

    @Override
    public State getStateFromCurrentContext() {
        State s;
        SelfManagementDomain smdomain = (SelfManagementDomain) domain;

        int daysElapsed = Days.daysBetween(trialStartDate, DateTime.now()).getDays();
        if (daysElapsed <= trialLength) {
            s = new MutableState();
            s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));

            ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
            o.setValue(ATT_DAY_TYPE, currentTime.getDayOfWeek() == DateTimeConstants.SATURDAY || currentTime.getDayOfWeek() == DateTimeConstants.SUNDAY ? 1 : 0);
            o.setValue(ATT_LOCATION, currentContext.getLocation().ordinal());

            if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.EASY) {
                o.setValue(ATT_HOUR_OF_DAY, currentTime.getHourOfDay());

            } else if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.MEDIUM) {
                o.setValue(ATT_QUARTER_HOUR_OF_DAY, getQuarterStateRepresentation());
                o.setValue(ATT_ACTIVITY, currentContext.getPhysicalActivity().ordinal());

            } else if (smdomain.getComplexity() == SelfManagementDomain.DomainComplexity.HARD) {
                o.setValue(ATT_ACTIVITY_TIME, currentTime.getHourOfDay() + ":" + currentTime.getMinuteOfHour());
                o.setValue(ATT_ACTIVITY, currentContext.getPhysicalActivity().ordinal());
                o.setValue(ATT_PHONE_USAGE, currentContext.getPhoneUsage().ordinal());
                o.setValue(ATT_EMOTIONAL_STATUS, currentContext.getEmotionalStatus().ordinal());
                o.setValue(ATT_STATE_OF_MIND, currentContext.getStateOfMind().ordinal());
            }

        } else {
            s = new TerminalState();
        }

        return s;
    }

    private void subscribeToUserContextChanges() {
        Thread contextListenerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //Kafka consumer configuration settings
                String topicName = "userContext";
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
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    System.out.println("Update size: " + records.count());
                    for (ConsumerRecord<String, String> record : records) {

                        // print the offset,key and value for the consumer records.
                        System.out.printf("offset = %d, key = %s, value = %s\n",
                                record.offset(), record.key(), record.value());

                        changedContexts.add(getContext(record));
                        synchronized (changedContexts) {
                            changedContexts.notify();
                        }
                    }
                }
            }
        });
        contextListenerThread.start();
    }


    private Context getContext(ConsumerRecord<String, String> consumerRecord) {
        Context context;
        if (recentlyProcessedContext == null) {
            context = new Context();
        } else {
            context = recentlyProcessedContext.copy();
        }

        JsonParser jsonParser = new JsonParser();
        JsonObject contextJson = jsonParser.parse(consumerRecord.value()).getAsJsonObject();

        //TODO get time from the consumer record

        // check the schema id of the received context information
        if (contextJson.has("phoneUsage")) {
            String value = contextJson.get("phoneUsage").getAsString();
            if (value.contentEquals("screen_on")) {
                context.setPhoneUsage(PhoneUsage.APPS_ACTIVE);
            } else if (value.contentEquals("screen_off")) {
                context.setPhoneUsage(PhoneUsage.SCREEN_OFF);
            } else {
                context.setPhoneUsage(PhoneUsage.TALKING);
            }

        } else if (contextJson.has("activityLevel")) {
            // TODO parse none-low-high values

        } else if (contextJson.has("header")) {
            String contextType = contextJson.get("header").getAsJsonObject().get("schema_id").getAsJsonObject().get("name").getAsString();
            if (contextType.contentEquals("funf-location")) {

            } else if (contextType.contentEquals("funf-wifi")) {
                // get location from wifi
                Iterator<JsonElement> it = contextJson.get("body").getAsJsonObject().get("values").getAsJsonArray().iterator();
                boolean foundLocation = false;
                while (it.hasNext()) {
                    String wifi = it.next().getAsJsonObject().get("SSID").getAsString();
                    if (wifi.startsWith("srdc")) {
                        context.setLocation(Location.OFFICE);
                        foundLocation = true;
                    } else if (wifi.contentEquals("gsev")) {
                        context.setLocation(Location.HOME);
                        foundLocation = true;
                    }
                }
                if (!foundLocation) {
                    context.setLocation(Location.OUTSIDE);
                }
            }
        }

        System.out.println("New context parsed from consumer record");
        recentlyProcessedContext = context;
        return context;
    }

    private void publishDummyContextToTopic() {
        Thread publisherThread = new Thread(() -> {
            String topicName = "userContext";
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");

            Producer<String, String> producer = new KafkaProducer<String, String>(props);

            System.out.println("Starting to send messages");
            while (true) {
                try {
                    Random r = new Random();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("time", LocalTime.now().toString());
                    jsonObject.addProperty("dayType", currentTime.getDayOfWeek() == DateTimeConstants.SATURDAY || currentTime.getDayOfWeek() == DateTimeConstants.SUNDAY ? DayType.WEEKEND.toString() : DayType.WEEKDAY.toString());
                    jsonObject.addProperty("location", Location.values()[r.nextInt(Location.values().length)].toString());
                    jsonObject.addProperty("activity", PhysicalActivity.values()[r.nextInt(PhysicalActivity.values().length)].toString());
                    jsonObject.addProperty("phoneUsage", PhoneUsage.values()[r.nextInt(PhoneUsage.values().length)].toString());
                    jsonObject.addProperty("emotionalStatus", EmotionalStatus.values()[r.nextInt(EmotionalStatus.values().length)].toString());
                    jsonObject.addProperty("stateOfMind", StateOfMind.values()[r.nextInt(StateOfMind.values().length)].toString());
                    producer.send(new ProducerRecord<>(topicName, jsonObject.toString()));
                    System.out.println("Message sent successfully");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        publisherThread.start();
    }
}
