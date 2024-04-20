package com.ka.hospitalsos.Activity;

import static androidx.core.location.LocationManagerCompat.isLocationEnabled;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ka.hospitalsos.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterHospital extends AppCompatActivity {

    private EditText editTextHospitalName, editTextHospitalAddress, editTextHospitalContact;
    private Button buttonRegister;

    private EditText editTextHospitalWebsite;
    private EditText editTextHospitalEmail;
    private EditText editTextHospitalDescription;
    private CheckBox checkBoxEmergencyCare;
    private CheckBox checkBoxSurgery;
    private FirebaseFirestore db;
    private static final int REQUEST_LOCATION_PERMISSIONS = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    double latitude;
    double longitude;
    private static final int REQUEST_CHECK_SETTINGS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_hosplital);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Views
        editTextHospitalName = findViewById(R.id.editTextHospitalName);
        editTextHospitalAddress = findViewById(R.id.editTextHospitalAddress);
        editTextHospitalContact = findViewById(R.id.editTextHospitalContact);

        editTextHospitalWebsite = findViewById(R.id.editTextHospitalWebsite);
        editTextHospitalEmail = findViewById(R.id.editTextHospitalEmail);
        editTextHospitalDescription = findViewById(R.id.editTextHospitalDescription);
        checkBoxEmergencyCare = findViewById(R.id.checkBoxEmergencyCare);
        checkBoxSurgery = findViewById(R.id.checkBoxSurgery);

        buttonRegister = findViewById(R.id.buttonRegister);

        // Button click listener to register hospital
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAllHospitalFields()) {
                    boolean locationEnabled = isLocationEnabled();
                    if (locationEnabled) {
                        // Location services are enabled, proceed with registration
                        requestLocationUpdates();
                    } else {
                        checkLocationSettings();

                        // Location services are not enabled, notify the user or take appropriate action
                        // For example, you can show a message or prompt the user to enable location services
                    }
                }
            }
        });
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

    private boolean checkAllHospitalFields() {
        String hospitalName = editTextHospitalName.getText().toString().trim();
        String hospitalAddress = editTextHospitalAddress.getText().toString().trim();
        String hospitalContact = editTextHospitalContact.getText().toString().trim();
        String hospitalEmail = editTextHospitalEmail.getText().toString().trim();
        String hospitalDescription = editTextHospitalDescription.getText().toString().trim();

        if (hospitalName.isEmpty() || hospitalAddress.isEmpty() || hospitalContact.isEmpty() || hospitalEmail.isEmpty() || hospitalDescription.isEmpty()) {
            if (hospitalName.isEmpty()) {
                editTextHospitalName.setError("Hospital Name is required");
                editTextHospitalName.requestFocus();
            }
            if (hospitalAddress.isEmpty()) {
                editTextHospitalAddress.setError("Hospital Address is required");
                editTextHospitalAddress.requestFocus();
            }
            if (hospitalContact.isEmpty()) {
                editTextHospitalContact.setError("Contact Number is required");
                editTextHospitalContact.requestFocus();
            }
            if (hospitalEmail.isEmpty()) {
                editTextHospitalEmail.setError("Hospital Email is required");
                editTextHospitalEmail.requestFocus();
            }
            if (hospitalDescription.isEmpty()) {
                editTextHospitalDescription.setError("Hospital Description is required");
                editTextHospitalDescription.requestFocus();
            }
            return false;
        }
        return true;
    }

    // Method to register hospital to Firestore
    private void registerHospital(double latitude, double longitude) {
        // Get data from EditText fields
        String hospitalName = editTextHospitalName.getText().toString().trim();
        String hospitalAddress = editTextHospitalAddress.getText().toString().trim();
        String hospitalContact = editTextHospitalContact.getText().toString().trim();
        String hospitalWebsite = editTextHospitalWebsite.getText().toString().trim();
        String hospitalEmail = editTextHospitalEmail.getText().toString().trim();
        String hospitalDescription = editTextHospitalDescription.getText().toString().trim();
        boolean hasEmergencyCare = checkBoxEmergencyCare.isChecked();
        boolean hasSurgery = checkBoxSurgery.isChecked();

        // Retrieve FCM token asynchronously
        retrieveFCMToken(new FCMTokenCallback() {
            @Override
            public void onTokenReceived(String fcmToken) {
                // Create a new hospital object with the data
                Map<String, Object> hospital = new HashMap<>();
                hospital.put("name", hospitalName);
                hospital.put("address", hospitalAddress);
                hospital.put("contact", hospitalContact);
                hospital.put("Hospital Email", hospitalEmail);
                hospital.put("Hospital Website", hospitalWebsite);
                hospital.put("Hospital Description", hospitalDescription);
                hospital.put("Has EmergencyCare", hasEmergencyCare);
                hospital.put("Has Surgery", hasSurgery);
                hospital.put("latitude", latitude);
                hospital.put("longitude", longitude);
                hospital.put("fcmToken", fcmToken);

                // Add hospital to Firestore
                db.collection("hospitals")
                        .add(hospital)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterHospital.this, "Hospital Registered Successfully", Toast.LENGTH_SHORT).show();
                                    // Clear EditText fields after successful registration
                                    Intent i = new Intent(RegisterHospital.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterHospital.this, "Failed to register hospital", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RegisterHospital.this, "Failed to retrieve FCM token", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveFCMToken(FCMTokenCallback callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Get the FCM token
                        String token = task.getResult();
                        Log.d("FCM Token", "Current token: " + token);
                        // Pass the token to the callback
                        callback.onTokenReceived(token);
                    } else {
                        Log.w("FCM Token", "getToken failed", task.getException());
                        // Notify the callback of failure
                        callback.onFailure(task.getException());
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
                        registerHospital(latitude, longitude);
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle failure to get last known location
                    Log.e("Location", "Failed to get last known location: " + e.getMessage());
                    // You can show a toast message or perform any other action here
                    Toast.makeText(RegisterHospital.this, "Failed to get last known location", Toast.LENGTH_SHORT).show();
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
                                resolvable.startResolutionForResult(RegisterHospital.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the settings.
                            Toast.makeText(RegisterHospital.this, "Location settings are inadequate, and cannot be fixed here. Please enable location services manually.", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // User enabled location services
                requestLocationUpdates();
            } else {
                // User did not enable location services
                Toast.makeText(RegisterHospital.this, "Location services not enabled. Please enable location services to proceed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Define a callback interface for FCM token retrieval
    interface FCMTokenCallback {
        void onTokenReceived(String fcmToken);

        void onFailure(Exception e);
    }
}
