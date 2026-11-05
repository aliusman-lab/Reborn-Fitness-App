package com.alilab.reborn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String PREF_NAME = "REBORN_PREFS";
    private static final String KEY_IS_ONBOARDED = "is_onboarded";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AGE = "user_age";
    private static final String KEY_USER_HEIGHT = "user_height";
    private static final String KEY_USER_WEIGHT = "user_weight";
    private static final String KEY_USER_GOAL = "user_goal";

    private TextView tvName;
    private TextView tvGoal;
    private TextView tvAge;
    private TextView tvHeight;
    private TextView tvWeight;
    private TextView tvBmi;
    private Button btnUpdateDetails;
    private Button btnResetData;

    private String selectedGoalDialog = "Weight Loss";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        loadProfileData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tv_profile_name);
        tvGoal = view.findViewById(R.id.tv_profile_goal);
        tvAge = view.findViewById(R.id.tv_profile_age);
        tvHeight = view.findViewById(R.id.tv_profile_height);
        tvWeight = view.findViewById(R.id.tv_profile_weight);
        tvBmi = view.findViewById(R.id.tv_profile_bmi);
        btnUpdateDetails = view.findViewById(R.id.btn_update_details);
        btnResetData = view.findViewById(R.id.btn_reset_data);
    }

    private void loadProfileData() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "Athlete");
        String goal = prefs.getString(KEY_USER_GOAL, "Weight Loss");
        int age = prefs.getInt(KEY_USER_AGE, 25);
        float height = prefs.getFloat(KEY_USER_HEIGHT, 175.0f);
        float weight = prefs.getFloat(KEY_USER_WEIGHT, 70.0f);

        tvName.setText(name);
        tvGoal.setText(goal);
        tvAge.setText(String.format(Locale.getDefault(), "%d yrs", age));
        tvHeight.setText(String.format(Locale.getDefault(), "%.1f cm", height));
        tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weight));

        // Calculate BMI
        if (height > 0) {
            float heightInM = height / 100.0f;
            float bmi = weight / (heightInM * heightInM);
            String bmiCategory;
            if (bmi < 18.5) {
                bmiCategory = "Underweight";
            } else if (bmi < 25.0) {
                bmiCategory = "Normal";
            } else if (bmi < 30.0) {
                bmiCategory = "Overweight";
            } else {
                bmiCategory = "Obese";
            }
            tvBmi.setText(String.format(Locale.getDefault(), "%.1f (%s)", bmi, bmiCategory));
        }
    }

    private void setupClickListeners() {
        btnUpdateDetails.setOnClickListener(v -> showUpdateDetailsDialog());
        btnResetData.setOnClickListener(v -> confirmResetData());
    }

    private void showUpdateDetailsDialog() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        float currentWeight = prefs.getFloat(KEY_USER_WEIGHT, 70.0f);
        String currentGoal = prefs.getString(KEY_USER_GOAL, "Weight Loss");
        selectedGoalDialog = currentGoal;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_profile, null);

        EditText etWeight = dialogView.findViewById(R.id.et_dialog_weight);
        TextView btnGoalWeightLoss = dialogView.findViewById(R.id.btn_dialog_goal_weight_loss);
        TextView btnGoalMuscleGain = dialogView.findViewById(R.id.btn_dialog_goal_muscle_gain);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        etWeight.setText(String.format(Locale.getDefault(), "%.1f", currentWeight));

        // Preselect Goal
        if ("Muscle Gain".equalsIgnoreCase(currentGoal)) {
            setGoalChipSelected(btnGoalMuscleGain, btnGoalWeightLoss);
        } else {
            setGoalChipSelected(btnGoalWeightLoss, btnGoalMuscleGain);
        }

        btnGoalWeightLoss.setOnClickListener(v -> {
            selectedGoalDialog = "Weight Loss";
            setGoalChipSelected(btnGoalWeightLoss, btnGoalMuscleGain);
        });

        btnGoalMuscleGain.setOnClickListener(v -> {
            selectedGoalDialog = "Muscle Gain";
            setGoalChipSelected(btnGoalMuscleGain, btnGoalWeightLoss);
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String weightStr = etWeight.getText().toString().trim();
            if (TextUtils.isEmpty(weightStr)) {
                etWeight.setError("Please enter weight");
                return;
            }

            try {
                float newWeight = Float.parseFloat(weightStr);
                if (newWeight <= 0) {
                    etWeight.setError("Weight must be greater than 0");
                    return;
                }

                // Save updated values
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(KEY_USER_WEIGHT, newWeight);
                editor.putString(KEY_USER_GOAL, selectedGoalDialog);
                editor.apply();

                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                // Refresh UI
                loadProfileData();

            } catch (NumberFormatException e) {
                etWeight.setError("Invalid number format");
            }
        });

        dialog.show();
    }

    private void setGoalChipSelected(TextView selected, TextView unselected) {
        if (getContext() == null) return;
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(getContext().getColor(R.color.bg_dark));

        unselected.setBackgroundResource(R.drawable.bg_chip_unselected);
        unselected.setTextColor(getContext().getColor(R.color.text_white));
    }

    private void confirmResetData() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Reset App Data")
                .setMessage("Are you sure you want to reset all onboarding data and start fresh?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Toast.makeText(getContext(), "App data cleared", Toast.LENGTH_SHORT).show();

                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), OnboardingActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
