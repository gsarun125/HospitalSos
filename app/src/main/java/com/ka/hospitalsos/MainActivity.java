package com.ka.hospitalsos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button sos;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton menuButton = findViewById(R.id.menuButton);
        db = FirebaseFirestore.getInstance();
        sos=findViewById(R.id.sosButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllFCMTokens();
            }
        });
    }

    private void getAllFCMTokens() {
        db.collection("hospitals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> tokens = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String fcmToken = document.getString("fcmToken");
                                if (fcmToken != null) {
                                    tokens.add(fcmToken);
                                }
                            }
                            System.out.println(tokens);
                            sendNotification(tokens);

                            // Do whatever you want with the tokens here
                            // For example, you can send notifications to all tokens
                            // or perform any other operation.
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to fetch FCM tokens", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendNotification(List<String> tokens) {
        // Construct notification message
        Map<String, String> data = new HashMap<>();
        data.put("title", "Emergency Alert");
        data.put("message", "Emergency situation detected. Please take necessary actions.");

        // Send notification to each token
        for (String token : tokens) {
            FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(token)
                    .setData(data)
                    .build());
        }

        Toast.makeText(MainActivity.this, "Notifications sent to all devices", Toast.LENGTH_SHORT).show();
    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.three_dot, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_item1) {
                    Intent i = new Intent(MainActivity.this, RegisterHospital.class);
                    startActivity(i);
                    return true;
                } else if (id == R.id.menu_item2) {
                    // Handle menu item 2 click
                    Toast.makeText(MainActivity.this, "Menu Item 2 clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_item3) {
                    // Handle menu item 3 click
                    Toast.makeText(MainActivity.this, "Menu Item 3 clicked", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }
}