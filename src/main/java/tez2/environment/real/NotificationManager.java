package tez2.environment.real;

import com.google.gson.JsonObject;
import tez.environment.real.FirebaseClient;
import webapp.model.ReactionResult;

import java.util.*;

/**
 * Created by suat on 19-Jun-17.
 */
public class NotificationManager {
    private static NotificationManager instance;

    private FirebaseClient firebaseClient;
    private Map<String, LinkedList<NotificationMetadata>> sentNotifications = new HashMap<>();
    private Map<String, Boolean> recentReactions = new HashMap<>();

    private NotificationManager() {
        firebaseClient = new FirebaseClient();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void sendNotificaitonToUser(String deviceIdentifier) {
        sendNotificationToUsers(Arrays.asList(new String[]{deviceIdentifier}));
    }

    public void sendNotificationToUsers(List<String> deviceIdentifiers) {
        firebaseClient.sendNotificationToUsers(deviceIdentifiers);
    }

    public void registerNotification(String deviceIdentifier, JsonObject notification) {
        String notificationId = notification.get("data").getAsJsonObject().get("notificationId").getAsString();
        NotificationMetadata metadata = new NotificationMetadata(notificationId, "Sent");
        synchronized (sentNotifications) {
            LinkedList notifications = getNotificationsForUser(deviceIdentifier);
            notifications.add(metadata);
        }
    }

    private LinkedList<NotificationMetadata> getNotificationsForUser(String deviceIdentifier) {
        LinkedList<NotificationMetadata> notificationsForUser = sentNotifications.get(deviceIdentifier);
        if (notificationsForUser == null) {
            notificationsForUser = new LinkedList<>();
            sentNotifications.put(deviceIdentifier, notificationsForUser);
        }
        return notificationsForUser;
    }

    public void processUserReaction(ReactionResult reactionResult) {
        NotificationMetadata notificationMetadata = getNotification(reactionResult.getDeviceIdentifier(), reactionResult.getInterventionId());
        notificationMetadata.setReaction(reactionResult.getReaction());
        refreshRecentReaction(reactionResult.getDeviceIdentifier(), reactionResult.getReaction());
        System.out.println("Notification metadata processed for: " + reactionResult.getInterventionId());
    }

    public void refreshRecentReaction(String deviceIdentifier, String reaction) {
        synchronized (recentReactions) {
            if(reaction.contentEquals("Positive")) {
                recentReactions.put(deviceIdentifier, true);
            }
        }
    }

    public boolean checkRecentReaction(String deviceIdentifier) {
        synchronized (recentReactions) {
            boolean reaction;
            if(recentReactions.containsKey(deviceIdentifier)) {
                reaction = recentReactions.get(deviceIdentifier);
            } else {
                reaction = false;
            }
            recentReactions.put(deviceIdentifier, false);
            return reaction;
        }
    }

    public NotificationMetadata getLastNotification(String deviceIdentifier) {
        synchronized (sentNotifications) {
            LinkedList<NotificationMetadata> notifications = getNotificationsForUser(deviceIdentifier);
            return notifications.getLast();
        }
    }

    private NotificationMetadata getNotification(String deviceIdentifier, String notificationId) {
        synchronized (sentNotifications) {
            LinkedList<NotificationMetadata> notifications = getNotificationsForUser(deviceIdentifier);
            NotificationMetadata notificationMetadata = null;
            for (NotificationMetadata metadata : notifications) {
                if (metadata.getNotificationId().contentEquals(notificationId)) {
                    notificationMetadata = metadata;
                    break;
                }
            }
            return notificationMetadata;
        }
    }

    public class NotificationMetadata {
        private String notificationId;
        private String reaction;

        public NotificationMetadata(String notificationId, String reaction) {
            this.notificationId = notificationId;
            this.reaction = reaction;
        }

        public String getNotificationId() {
            return notificationId;
        }

        public void setNotificationId(String notificationId) {
            this.notificationId = notificationId;
        }

        public String getReaction() {
            return reaction;
        }

        public void setReaction(String reaction) {
            this.reaction = reaction;
        }
    }
}
