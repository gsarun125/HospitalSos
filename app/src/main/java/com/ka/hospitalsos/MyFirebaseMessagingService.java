package com.ka.hospitalsos;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ka.hospitalsos.Activity.MainActivity;

import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Save the FCM token locally or send it to your server
        saveTokenLocally(token);
    }

    private void saveTokenLocally(String token) {
        // Implement your logic to save the token locally (e.g., use SharedPreferences).
        Log.d("FCM Token", "Saving token locally: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        double latitude = 0;
        double longitude = 0;
        if (remoteMessage.getData().size() > 0) {
            // Handle data payload
            Map<String, String> data = remoteMessage.getData();

            // Retrieve data fields
            String emergency = data.get("emergency");
            String latitudeString = remoteMessage.getData().get("latitude");
            String longitudeString = remoteMessage.getData().get("longitude");

            if (latitudeString != null && longitudeString != null) {
                latitude = Double.parseDouble(latitudeString);
                longitude = Double.parseDouble(longitudeString);
            } else {
                // Handle the case where latitude or longitude extras are null
                // You can log a message or provide default values, depending on your requirements
            }

            // Process the retrieved data as needed
            Log.d("ddd", "Emergency: " +longitude);
            Log.d("ddd", "Location: " +  latitude );

            // Example: Display the notification content
            //  sendNotification(data);
        }

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            // Extract notification title and body
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            String ChannelId = remoteMessage.getNotification().getChannelId();
            if (ChannelId.equals("10")) {
                // Create an intent for the activity you want to open when the notification is clicked
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                // Inflate custom layout for action buttons
                RemoteViews customNotificationActionsView = new RemoteViews(getPackageName(), R.layout.custom_notification_actions);

                // Set onClickPendingIntent for the custom action buttons
                Intent acceptedIntent = new Intent(this, AcceptedReceiver.class);
                acceptedIntent.setAction("Accepted");
                acceptedIntent.putExtra("latitude", latitude); // Add latitude as extra
                acceptedIntent.putExtra("longitude", longitude);
                
                PendingIntent action1PendingIntent = PendingIntent.getBroadcast(this, 0, acceptedIntent, PendingIntent.FLAG_IMMUTABLE);
                customNotificationActionsView.setOnClickPendingIntent(R.id.btn_action1, action1PendingIntent);

                Intent refusedIntent = new Intent(this, RefusedReceiver.class);
                refusedIntent.setAction("Refused");
                PendingIntent action2PendingIntent = PendingIntent.getBroadcast(this, 0, refusedIntent, PendingIntent.FLAG_IMMUTABLE);
                customNotificationActionsView.setOnClickPendingIntent(R.id.btn_action2, action2PendingIntent);
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                customNotificationActionsView.setTextViewText(R.id.tv_notification_title, title);
                customNotificationActionsView.setTextViewText(R.id.tv_notification_body, body);
                // Create a notification builder with custom layout for actions
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, "default_channel_id")
                                .setSmallIcon(R.drawable.ambulance)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setCustomBigContentView(customNotificationActionsView)
                                .setSound(sound);

                ; // Set custom big content view for expanded notification

                // Get the notification manager
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // Check if the SDK version is Oreo or higher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Create a notification channel
                    NotificationChannel channel = new NotificationChannel("default_channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(1, notificationBuilder.build());
            }else {
                sendNotification2(this);
            }
        }

    }
    public static void sendNotification2(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification channel ID
            String channelId = "45";

            // Notification title
            String title = "Emergency Alert";

            // Notification content text
            String contentText = "Emergency situation detected. Please take necessary actions.";

            // Create the notification builder
            Notification.Builder builder = new Notification.Builder(context, channelId)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setSmallIcon(android.R.drawable.ic_dialog_info); // Set your desired icon here

            // Get the notification manager
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Check if the notification manager is not null
            if (notificationManager != null) {
                // Build and display the notification
                notificationManager.notify(0, builder.build());
            }
        }
    }
}