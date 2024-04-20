package com.ka.hospitalsos.Activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
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
    String latitude;
    Button Accepted;
    Button Rejected;
    String longitude;

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


        Intent intent = getIntent();

// Retrieve the extras from the intent
        Bundle extras = intent.getExtras();

        if (extras != null) {
            // Iterate over the keys in the extras bundle
            for (String key : extras.keySet()) {
                // Retrieve the value corresponding to each key
                Object value = extras.get(key);

                // Print the key-value pair
                Log.d("IntentExtras", key + " : " + value);
            }
        } else {
            Log.d("IntentExtras", "No extras found in the intent.");
        }

        // Check if extras are not null and contain latitude and longitude
        if (extras != null && extras.containsKey("latitude") && extras.containsKey("longitude")) {
            latitude = (String) extras.get("latitude");
            longitude = (String) extras.get("longitude");
            System.out.println("kjuyhgfds");
        }


        Accepted = findViewById(R.id.btn_action1);
        Rejected = findViewById(R.id.btn_action2);
        Accepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Audio.stopAudio();
                clearNotificationsByChannelIdAndName("0", "FCM_Channel");

                System.out.println("123" + latitude);
                System.out.println("123" + longitude);
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+ ","+longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mapIntent);
                finish();
            }
        });

        Rejected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Audio.stopAudio();
                clearNotificationsByChannelIdAndName("0", "FCM_Channel");
                finish();
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