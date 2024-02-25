package com.ka.hospitalsos;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class RegisterHospital extends AppCompatActivity {

    private EditText editTextHospitalName, editTextHospitalAddress, editTextHospitalContact;
    private Button buttonRegister;

    // Firebase Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_hosplital);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        editTextHospitalName = findViewById(R.id.editTextHospitalName);
        editTextHospitalAddress = findViewById(R.id.editTextHospitalAddress);
        editTextHospitalContact = findViewById(R.id.editTextHospitalContact);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Button click listener to register hospital
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerHospital();
            }
        });
    }

    // Method to register hospital to Firestore
    private void registerHospital() {
        // Get data from EditText fields
        String hospitalName = editTextHospitalName.getText().toString().trim();
        String hospitalAddress = editTextHospitalAddress.getText().toString().trim();
        String hospitalContact = editTextHospitalContact.getText().toString().trim();

        // Retrieve FCM token asynchronously
        retrieveFCMToken(new FCMTokenCallback() {
            @Override
            public void onTokenReceived(String fcmToken) {
                // Create a new hospital object with the data
                Map<String, Object> hospital = new HashMap<>();
                hospital.put("name", hospitalName);
                hospital.put("address", hospitalAddress);
                hospital.put("contact", hospitalContact);
                hospital.put("fcmToken", fcmToken);
                System.out.println(fcmToken+"jkhgfdxghj");
                // Add hospital to Firestore
                db.collection("hospitals")
                        .add(hospital)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterHospital.this, "Hospital Registered Successfully", Toast.LENGTH_SHORT).show();
                                    // Clear EditText fields after successful registration
                                    editTextHospitalName.setText("");
                                    editTextHospitalAddress.setText("");
                                    editTextHospitalContact.setText("");
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

    // Define a callback interface for FCM token retrieval
    interface FCMTokenCallback {
        void onTokenReceived(String fcmToken);
        void onFailure(Exception e);
    }
}
