package com.ka.hospitalsos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
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
       // System.out.println("hhsdfghjkl;kjhgfdsgh");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    String channelId = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        channelId = notification.getNotification().getChannelId();
                    }
                    String channelName = getChannelName(notificationManager, channelId);
           //         System.out.println("Notification from channel: " + channelName);
             //       System.out.println("Notification ID: " + notification.getId());

                    int id=notification.getId();
                    if (channelName.equals("chanel2")){
                        clearNotificationsByChannelIdAndName("0","FCM_Channel");

                    }
                    if(notification.getId()==1){
                        if (Audio.isPlaying()) {
                        } else {
                            Audio.playAudio(this);
                        }

                        Log.d("NotificationCheck", "Notification found from channel: " + channelName);

                    }
                    if (channelName.equals("FCM_Channel") && id==0  ) {
                        System.out.println("kljghjdkfjfdhjk");
                        if (Audio.isPlaying()) {
                        } else {
                            Audio.playAudio(this);
                        }

                        Log.d("NotificationCheck", "Notification found from channel: " + channelName);
                    }


                }
            }
        }
    }


    private void clearNotificationsByChannelIdAndName(String channelId, String channelName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    String notificationChannelId = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        notificationChannelId = notification.getNotification().getChannelId();
                    }
                    String notificationChannelName = getChannelName(notificationManager, notificationChannelId);
                    if (notificationChannelId.equals(channelId) && notificationChannelName.equals(channelName)) {
                        int notificationId = notification.getId();
                        notificationManager.cancel(notificationId);
                    }
                }
            }
        }
    }

    private String getChannelName(NotificationManager notificationManager, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel != null) {
                return channel.getName().toString();
            }
        }
        return "Unknown";
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


    private void playAudio() {
        // Play your audio here
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mediaPlayer.start();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
