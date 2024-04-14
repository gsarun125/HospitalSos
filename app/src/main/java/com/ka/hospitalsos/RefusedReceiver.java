package com.ka.hospitalsos;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class RefusedReceiver extends BroadcastReceiver {
    private static final Map<Integer, MediaPlayer> mediaPlayerMap = new HashMap<>();


    @Override
    public void onReceive(Context context, Intent intent) {
        dismissNotification(context);
        Audio.stopAudio();
        Toast.makeText(context, "Refused", Toast.LENGTH_SHORT).show();
    }
    private void dismissNotification(Context context) {
        // Dismiss the notification by using its ID
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1); // Assuming you're using ID 1 for your notification
    }
}
