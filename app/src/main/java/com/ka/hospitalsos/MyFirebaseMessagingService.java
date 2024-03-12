package com.ka.hospitalsos;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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

        if (remoteMessage.getData().size() > 0) {
            // Handle data payload
            Map<String, String> data = remoteMessage.getData();

            // Retrieve data fields
            String emergency = data.get("emergency");
            String location = data.get("location");

            // Process the retrieved data as needed
            Log.d("ddd", "Emergency: " + emergency);
            Log.d("ddd", "Location: " + location);

            // Example: Display the notification content
            //  sendNotification(data);
        }

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            // Extract notification title and body
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();            // Create an intent for the activity you want to open when the notification is clicked
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            // Inflate custom layout for action buttons
            RemoteViews customNotificationActionsView = new RemoteViews(getPackageName(), R.layout.custom_notification_actions);

            // Set onClickPendingIntent for the custom action buttons
            Intent acceptedIntent = new Intent(this, AcceptedReceiver.class);
            acceptedIntent.setAction("Accepted");
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
        }


    }
}