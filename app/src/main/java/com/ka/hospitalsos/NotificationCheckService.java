package com.ka.hospitalsos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationCheckService extends Service {
    private Timer timer;
    private static final long INTERVAL = 500; // Interval in milliseconds
    private static final int NOTIFICATION_ID = 1234; // Unique ID for the notification
    private static final String CHANNEL_ID = "NotificationCheckServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForNotifications();
            }
        }, 0, INTERVAL);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void checkForNotifications() {
        // Implement logic to check for notifications from the specific channel ID
        // You can use NotificationManager to check for notifications
        // For example, you can check if there are any notifications from the default channel ID
        System.out.println("hhsdfghjkl;kjhgfdsgh");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    System.out.println(notification.getId());
                    if (notification.getId() == 1) {
                        // Notification from the specific channel ID found
                        // You can perform further actions here
                        updateNotification(notification.getId());
                        Log.d("NotificationCheck", "Notification found from default_channel_id1");
                    }
                }
            }
        }
    }

    private void updateNotification(int notificationId) {
        // Build the updated notification
        Notification updatedNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Updated Notification Title")
                .setContentText("Updated Notification Content")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        // Notify the system to update the existing notification with the same ID
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, updatedNotification);
    }
    private Notification createNotification() {
        // Create a notification to keep the service running in the foreground
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Notification Check Service")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_accept)
                .setPriority(NotificationCompat.PRIORITY_LOW); // Set low priority to avoid disturbing the user

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification Check Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
