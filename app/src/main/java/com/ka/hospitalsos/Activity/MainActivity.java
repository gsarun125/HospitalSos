package com.ka.hospitalsos.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ka.hospitalsos.Agency;
import com.ka.hospitalsos.AgencySelectionDialog;
import com.ka.hospitalsos.NotificationCheckService;
import com.ka.hospitalsos.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

    private static final String CHANNEL_ID = "10";
    private static final CharSequence CHANNEL_NAME = "FCM_Channel";
    private ImageView addPreference;
    private  static  final String TAG="MainActivity";
    private FusedLocationProviderClient fusedLocationClient;

    private SharedPreferences sharedPreferences;
    double latitude;
    double longitude;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private static final int REQUEST_LOCATION_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton menuButton = findViewById(R.id.menuButton);
        db = FirebaseFirestore.getInstance();
        sos = findViewById(R.id.sosButton);
        createNotificationChannel(this);
        createNotificationChannel2(this);
        sharedPreferences = getSharedPreferences("SelectedAgencies", Context.MODE_PRIVATE);
        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        startService(new Intent(this, NotificationCheckService.class));
        boolean locationEnabled = isLocationEnabled();
        if (locationEnabled) {
            // Location services are enabled, proceed with registration
            requestLocationUpdates();
        } else {
            checkLocationSettings();

            // Location services are not enabled, notify the user or take appropriate action
            // For example, you can show a message or prompt the user to enable location services
        }
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sos.setBackgroundColor(Color.RED);
                showConfirmationDialogWithTimer();
            }
        });
        addPreference = findViewById(R.id.addPreference);

        // Initialize SharedPreferences

        addPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgencySelectionDialog();
            }
        });
    }
    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create());

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The device has location services enabled.
                    requestLocationUpdates();
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. Show the user a dialog to upgrade location settings
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the settings.
                            Toast.makeText(MainActivity.this, "Location settings are inadequate, and cannot be fixed here. Please enable location services manually.", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        });
    }
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Got last known location
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        // Call the method to register hospital with location
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle failure to get last known location
                    Log.e("Location", "Failed to get last known location: " + e.getMessage());
                    // You can show a toast message or perform any other action here
                    Toast.makeText(MainActivity.this, "Failed to get last known location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, attempt to get location again
                requestLocationUpdates();
            } else {
                // Permission denied, show a toast message or perform any other action
                Toast.makeText(this, "Location permissions are required to register hospital.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gpsEnabled || networkEnabled;
    }

    private void showAgencySelectionDialog() {
        fetchAgencyData(new FirestoreCallback() {
            @Override
            public void onCallback(List<Agency> agencies) {
                if (agencies != null) {
                    AgencySelectionDialog dialog = new AgencySelectionDialog(MainActivity.this, agencies,
                            new AgencySelectionDialog.Callback() {
                                @Override
                                public void onConfirm(List<String> selectedAgencies) {
                                    saveSelectedAgencies(selectedAgencies);
                                }
                            });
                    dialog.show();
                } else {
                    // Handle error
                    Log.e(TAG, "Failed to fetch agency data.");
                }
            }
        });
    }
    private void fetchAgencyData(final FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ambulanceRegistrations") // Replace "your_collection_name" with your actual collection name
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Agency> agencies = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String serviceName = document.getString("serviceName");
                                String contact = document.getString("contact");
                                String address = document.getString("address");
                                long numAmbulances = document.getLong("numAmbulances");

                                System.out.println("id :"+ id +" serviceName: "+serviceName);
                                Agency agency = new Agency(id, serviceName, contact, address, numAmbulances);
                                agencies.add(agency);
                            }
                            callback.onCallback(agencies);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            callback.onCallback(null);
                        }
                    }
                });
    }

    private void saveSelectedAgencies(List<String> selectedAgencies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder sb = new StringBuilder();
        for (String agencyID : selectedAgencies) {
            sb.append(agencyID).append(",");
        }
        System.out.println("345"+sb.toString());
        editor.putString("selectedAgencies", sb.toString());
        editor.apply();
    }

    private interface FirestoreCallback {
        void onCallback(List<Agency> agencies);
    }
    private interface FirestoreCallback2 {
        void onCallback( HashSet<String> agencies);
    }
    private void showConfirmationDialogWithTimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to send an SOS message?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Change the background color to indicate the SOS button press
                sos.setBackgroundColor(Color.RED);
                // Call a method to send the SOS message
                sendSOSMessage();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog if Cancel is clicked
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Set up the countdown timer
        final CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the dialog message with the countdown
                dialog.setMessage("Are you sure you want to send an SOS message?\n" +
                        "Time left: " + millisUntilFinished / 1000 + " seconds");
            }

            @Override
            public void onFinish() {
                // Cancel the dialog if the user doesn't press "OK" within 30 seconds
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "SOS action canceled.", Toast.LENGTH_SHORT).show();
            }
        };

        // Start the countdown timer
        countDownTimer.start();

        // Set the dialog dismiss listener to cancel the countdown timer if the dialog is dismissed manually
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                countDownTimer.cancel();
            }
        });
    }

    private void sendSOSMessage() {
        String selectedAgenciesString = sharedPreferences.getString("selectedAgencies", "");

        if (selectedAgenciesString != null && !selectedAgenciesString.isEmpty()) {
            // Split the string into individual agency IDs
            String[] agencyIds = selectedAgenciesString.split(",");

            // Now you have an array of agency IDs
            for (String agencyId : agencyIds) {
                Log.d("Selected Agency ID", agencyId);
            }

            getDataForMultipleIds(Arrays.asList(agencyIds), new FirestoreCallback2() {
                @Override
                public void onCallback( HashSet<String> fcmTokens) {
                    if (fcmTokens != null) {
                        sendNotification( fcmTokens);
                    } else {
                        // Error occurred or no FCM tokens found
                        Log.d(TAG, "FCM Tokens not found or error occurred");
                    }
                }
            });
        } else {
            System.out.println("else");
            getAllFCMTokens();
        }

    }

    private void getDataForMultipleIds(List<String> documentIds, final FirestoreCallback2 callback) {
        db.collection("ambulanceRegistrations")
                .whereIn(FieldPath.documentId(), documentIds)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            HashSet<String> fcmTokens = new HashSet<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String fcmToken = document.getString("fcmToken");
                                if (fcmToken != null && !fcmToken.isEmpty()) {
                                    fcmTokens.add(fcmToken);
                                }
                            }
                            // Pass the list of FCM tokens to the callback
                            callback.onCallback(fcmTokens);
                        } else {
                            // Error occurred
                            Log.e(TAG, "Error getting documents", task.getException());
                            callback.onCallback(null);
                        }
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
                            HashSet<String> tokens = new HashSet<>();
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
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define the notification channel ID and name
            String channelId = CHANNEL_ID;
            CharSequence channelName = CHANNEL_NAME;

            // Set the importance level for the notification channel
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Create the notification channel
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            // Optionally, configure additional settings for the channel, such as description, sound, and vibration
            channel.setDescription("Channel for emergency alerts");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000}); // Vibrate pattern if needed

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void createNotificationChannel2(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define the notification channel ID and name
            String channelId = "45";
            CharSequence channelName = "chanel2";

            // Set the importance level for the notification channel
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Create the notification channel
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            // Optionally, configure additional settings for the channel, such as description, sound, and vibration
            channel.setDescription("Channel for emergency alerts");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000}); // Vibrate pattern if needed

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(HashSet<String> tokens) {
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

                notificationObj.put("android_channel_id","10");
                notificationObj.put("notification_priority","PRIORITY_HIGH");

                notificationObj.put("notification_count",2);
                notificationObj.put(" default_vibrate_timings", false);
                dataObj.put("latitude", latitude); // Add latitude to the payload
                dataObj.put("longitude", longitude);
                dataObj.put("emergency", true); // Add your custom data here


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
        Toast.makeText(MainActivity.this, "Notifications sent to all hospitals", Toast.LENGTH_SHORT).show();


    }

   private void callApi(JSONObject jsonObject) {
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
                } else if (id ==R.id.menu_item2) {
                    Intent i = new Intent(MainActivity.this, AmbulanceRegistrationActivity.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}