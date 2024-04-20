package com.ka.hospitalsos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AgencySelectionDialog extends Dialog {

    private List<Agency> agencies;
    private Callback callback;
    private int maxHeight; // Maximum height for the dialog

    public interface Callback {
        void onConfirm(List<String> selectedAgencies);
    }

    public AgencySelectionDialog(@NonNull Context context, List<Agency> agencies, Callback callback) {
        super(context);
        this.agencies = agencies;
        this.callback = callback;
        // Calculate max height as 80% of screen height
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        maxHeight = (int) (wm.getDefaultDisplay().getHeight() * 0.8);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_agency_selection);

        // Set the initial height of the dialog to WRAP_CONTENT
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Get the LinearLayout containing the checkbox views
        final LinearLayout layout = findViewById(R.id.agencyLayout);

        // Use ViewTreeObserver to measure the actual height of the dialog
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // If the measured height exceeds the maximum height, set it to the maximum
                if (layout.getHeight() > maxHeight) {
                    getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, maxHeight);
                }
            }
        });

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (final Agency agency : agencies) {
            View checkBoxView = inflater.inflate(R.layout.checkbox_agency, null);
            CheckBox checkBox = checkBoxView.findViewById(R.id.checkBox);
            TextView textAddress = checkBoxView.findViewById(R.id.textAddress);
            TextView textContact = checkBoxView.findViewById(R.id.textContact);
            TextView textNumAmbulances = checkBoxView.findViewById(R.id.textNumAmbulances);

            checkBox.setText(agency.getServiceName());
            checkBox.setTag(agency.getId());
            textAddress.setText("Address: " + agency.getAddress());
            textContact.setText("Contact: " + agency.getContact());
            textNumAmbulances.setText("Number of Ambulances: " + agency.getNumAmbulances());

            layout.addView(checkBoxView);
        }

        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedAgencies = getCheckedTags();
                // Pass the list of selected agencies to the callback
                for (String id : selectedAgencies) {

                    Log.d("Selected Agency ID", id);
                }
                Log.d("Selected Agency ID", "dddid");
               callback.onConfirm(selectedAgencies);
                // Dismiss the dialog
                dismiss();
            }
        });
    }

    private List<String> getCheckedTags() {
        List<String> checkedTags = new ArrayList<>();
        LinearLayout layout = findViewById(R.id.agencyLayout);
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View view = layout.getChildAt(i);
                if (view instanceof ViewGroup) {
                    ViewGroup checkBoxView = (ViewGroup) view;
                    CheckBox checkBox = checkBoxView.findViewById(R.id.checkBox);
                    Log.d("getCheckedTags", "CheckBox text: " + checkBox.getText());
                    if (checkBox.isChecked()) {
                        Object tag = checkBox.getTag();
                        Log.d("getCheckedTags", "CheckBox is checked, Tag: " + tag);
                        if (tag instanceof String) {
                            checkedTags.add((String) tag);
                            Log.d("getCheckedTags", "Tag added to checkedTags: " + tag);
                        }
                    }
                }
            }
        } else {
            Log.w("getCheckedTags", "Layout is null");
        }
        return checkedTags;
    }





}
