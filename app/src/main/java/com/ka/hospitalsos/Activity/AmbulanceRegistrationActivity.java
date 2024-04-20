package com.ka.hospitalsos.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ka.hospitalsos.AmbulanceRegistration;
import com.ka.hospitalsos.R;

public class AmbulanceRegistrationActivity extends AppCompatActivity {
    EditText editTextServiceName, editTextContact, editTextAddress, editTextNumAmbulances;
    Button buttonRegister;

    private FirebaseFirestore db;
    private ImageView addPreference;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ambulance_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editTextServiceName = findViewById(R.id.editTextServiceName);
        editTextContact = findViewById(R.id.editTextContact);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextNumAmbulances = findViewById(R.id.editTextNumAmbulances);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle registration logic here
                registerAmbulance();
            }
        });
    }

    private void registerAmbulance() {
        String serviceName = editTextServiceName.getText().toString().trim();
        String contact = editTextContact.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        int numAmbulances = Integer.parseInt(editTextNumAmbulances.getText().toString().trim());

        // Get the current FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String fcmToken) {
                        // Create a new ambulance registration object
                        AmbulanceRegistration ambulanceRegistration = new AmbulanceRegistration(serviceName, contact, address, numAmbulances, fcmToken);

                        // Add the ambulance registration data to Firestore
                        db.collection("ambulanceRegistrations")
                                .add(ambulanceRegistration)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(AmbulanceRegistrationActivity.this, "Ambulance Registered!", Toast.LENGTH_SHORT).show();
                                        editTextServiceName.setText("");
                                        editTextContact.setText("");
                                        editTextAddress.setText("");
                                        editTextNumAmbulances.setText("");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AmbulanceRegistrationActivity.this, "Error registering ambulance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AmbulanceRegistrationActivity.this, "Error getting FCM token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}