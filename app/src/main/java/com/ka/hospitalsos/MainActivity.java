package com.ka.hospitalsos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        // Iterate through each token and send notification
         for (String token : tokens) {
             try{
                 JSONObject jsonObject=new JSONObject();
                 JSONObject notificationObj=new JSONObject();

                 notificationObj.put("title","Emergency Alert");
                 notificationObj.put("body","dfhjdj");
                 notificationObj.put("icon","ambulance");
                 notificationObj.put("color","#FA1818");
                 notificationObj.put("sound","sound.mp3");
                 notificationObj.put("click_action","com.ka.hospitalsos.CLICK_ACTION");
                 notificationObj.put(" default_vibrate_timings",false);

                 JSONArray vibrateTimingsArray = new JSONArray();
                 vibrateTimingsArray.put("0.0s");
                 vibrateTimingsArray.put("0.2s");
                 vibrateTimingsArray.put("0.1s");
                 vibrateTimingsArray.put("0.2s");
                 notificationObj.put("vibrate_timings", vibrateTimingsArray);

                 jsonObject.put("notification",notificationObj);
                 jsonObject.put("to",token);
                 callApi(jsonObject);
             }catch (Exception e){

             }
             Log.d("Notification", "Sending notification to token: " + token);
         }

         // Show a toast indicating that notifications have been sent
        Toast.makeText(MainActivity.this, "Notifications sent to all hospitals", Toast.LENGTH_SHORT).show();


    }
    void  callApi(JSONObject jsonObject){
     MediaType JSON = MediaType.get("application/json");

        OkHttpClient client = new OkHttpClient();
        String url="https://fcm.googleapis.com/fcm/send";
        RequestBody body=RequestBody.create(jsonObject.toString(),JSON);
        Request request=new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAACo0WmAc:APA91bGaE8nY5xOm5DPocnYXeWqAXSJZ5-ZZCjLrDqF5KPt79NUs4aVh67mHKBGLvfjdwW6vOZxfd5_wrL0hFSPt1drfO-OqXix4UT36PL2HhcO-rFdbWVeISqJJZJMV1p41nKHUXFmP")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("fhjfjfk");
                String responseBody = response.body().string();
                System.out.println(responseBody);
            }
        });
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