package com.ka.hospitalsos.Activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ka.hospitalsos.Audio;
import com.ka.hospitalsos.R;

public class DialogActivity extends AppCompatActivity {

    Button Accepted;
    Button Rejected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dialog);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    Accepted=findViewById(R.id.btn_action1);
    Rejected=findViewById(R.id.btn_action2);
    Accepted.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    });

    Rejected.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Audio.stopAudio();
            clearNotificationsByChannelIdAndName("0","FCM_Channel");

        }
    });

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

}