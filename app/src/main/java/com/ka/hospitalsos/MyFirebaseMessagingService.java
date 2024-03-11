package com.ka.hospitalsos;





import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


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

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            // Extract notification title and body
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Create an intent for the activity you want to open when the notification is clicked
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            // Create intent for the first action button
            Intent action1Intent = new Intent(this, Action1Receiver.class);
            action1Intent.setAction("ACTION_1");
            PendingIntent action1PendingIntent = PendingIntent.getBroadcast(this, 0, action1Intent, PendingIntent.FLAG_IMMUTABLE);

            // Create intent for the second action button
            Intent action2Intent = new Intent(this, Action2Receiver.class);
            action2Intent.setAction("ACTION_2");
            PendingIntent action2PendingIntent = PendingIntent.getBroadcast(this, 0, action2Intent, PendingIntent.FLAG_IMMUTABLE);

            // Get the icon from the drawable resources
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_accept);

            // Create a notification builder
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, "default_channel_id")
                            .setSmallIcon(R.drawable.ambulance)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .addAction(R.drawable.ic_accept, "Accept", action1PendingIntent) // Add first action button
                            .addAction(R.drawable.ic_decline, "decline", action2PendingIntent) // Add second action button
                             .setStyle(new NotificationCompat.BigTextStyle()
                             .bigText("Longer text content goes here. This will allow you to display longer text in the notification view."));
            // Get the notification manager
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Check if the SDK version is Oreo or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a notification channel
                NotificationChannel channel = new NotificationChannel("default_channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            // Display the notification
            notificationManager.notify(0, notificationBuilder.build());
        }


    }

}