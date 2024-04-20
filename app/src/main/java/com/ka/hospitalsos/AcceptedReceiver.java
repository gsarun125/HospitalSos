package com.ka.hospitalsos;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AcceptedReceiver extends BroadcastReceiver {

    private FirebaseFirestore db;
    @Override
    public void onReceive(Context context, Intent intent) {
        dismissNotification(context);
        Audio.stopAudio();
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+ ","+longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mapIntent);

        db = FirebaseFirestore.getInstance();
        Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
        getAllFCMTokens(context);
    }

    private void dismissNotification(Context context) {
        // Dismiss the notification by using its ID
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1); // Assuming you're using ID 1 for your notification
    }

    private void getAllFCMTokens(Context context) {
        db.collection("hospitals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            HashSet<String> tokens = new HashSet<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String fcmToken = document.getString("fcmToken");
                                if (fcmToken != null) {
                                    tokens.add(fcmToken);
                                }
                            }

                            sendNotification(tokens,context);

                            // Do whatever you want with the tokens here
                            // For example, you can send notifications to all tokens
                            // or perform any other operation.
                        } else {
                            Toast.makeText(context, "Failed to fetch FCM tokens", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void sendNotification(HashSet<String> tokens,Context context) {
        // Iterate through each token and send notification
        for (String token : tokens) {
            try {
                JSONObject jsonObject = new JSONObject();
                JSONObject notificationObj = new JSONObject();
                JSONObject dataObj = new JSONObject();
                notificationObj.put("title", "Emergency Alert");
                notificationObj.put("body", "Emergency situation detected. Please take necessary actions.");
                notificationObj.put("icon", "ambulance");
                notificationObj.put("color", "#FA1818");
                notificationObj.put("click_action", "com.ka.hospitalsos.CLICK_ACTION");

                notificationObj.put("android_channel_id","45");
                notificationObj.put("notification_priority","PRIORITY_HIGH");

                notificationObj.put("notification_count",2);
                notificationObj.put(" default_vibrate_timings", false);

                dataObj.put("emergency", true); // Add your custom data here
                dataObj.put("location", "Hospital XYZ"); // Add additional data fields as needed


                JSONArray vibrateTimingsArray = new JSONArray();
                vibrateTimingsArray.put("0.0s");
                vibrateTimingsArray.put("0.2s");
                vibrateTimingsArray.put("0.1s");
                vibrateTimingsArray.put("0.2s");
                notificationObj.put("vibrate_timings", vibrateTimingsArray);

                jsonObject.put("data", dataObj);
                jsonObject.put("notification", notificationObj);
                jsonObject.put("to", token);
                callApi(jsonObject);
            } catch (Exception e) {

            }
            Log.d("Notification", "Sending notification to token: " + token);
        }

        // Show a toast indicating that notifications have been sent
        Toast.makeText(context, "Accepted is sent to all hospitals", Toast.LENGTH_SHORT).show();


    }
    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json");

        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAACo0WmAc:APA91bGaE8nY5xOm5DPocnYXeWqAXSJZ5-ZZCjLrDqF5KPt79NUs4aVh67mHKBGLvfjdwW6vOZxfd5_wrL0hFSPt1drfO-OqXix4UT36PL2HhcO-rFdbWVeISqJJZJMV1p41nKHUXFmP")
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

}
