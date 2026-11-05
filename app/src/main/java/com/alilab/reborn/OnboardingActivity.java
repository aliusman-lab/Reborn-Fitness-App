package com.alilab.reborn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREF_NAME = "REBORN_PREFS";
    private static final String KEY_IS_ONBOARDED = "is_onboarded";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AGE = "user_age";
    private static final String KEY_USER_HEIGHT = "user_height";
    private static final String KEY_USER_WEIGHT = "user_weight";
    private static final String KEY_USER_GOAL = "user_goal";

    private EditText etName;
    private EditText etAge;
    private EditText etHeight;
    private EditText etWeight;
    private TextView btnGoalWeightLoss;
    private TextView btnGoalMuscleGain;
    private Button btnStartJourney;

    private String selectedGoal = "Weight Loss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initViews();
        setupGoalToggle();
        setupSubmitButton();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        btnGoalWeightLoss = findViewById(R.id.btn_goal_weight_loss);
        btnGoalMuscleGain = findViewById(R.id.btn_goal_muscle_gain);
        btnStartJourney = findViewById(R.id.btn_start_journey);
    }

    private void setupGoalToggle() {
        btnGoalWeightLoss.setOnClickListener(v -> {
            selectedGoal = "Weight Loss";
            btnGoalWeightLoss.setBackgroundResource(R.drawable.bg_chip_selected);
            btnGoalWeightLoss.setTextColor(getColor(R.color.bg_dark));

            btnGoalMuscleGain.setBackgroundResource(R.drawable.bg_chip_unselected);
            btnGoalMuscleGain.setTextColor(getColor(R.color.text_white));
        });

        btnGoalMuscleGain.setOnClickListener(v -> {
            selectedGoal = "Muscle Gain";
            btnGoalMuscleGain.setBackgroundResource(R.drawable.bg_chip_selected);
            btnGoalMuscleGain.setTextColor(getColor(R.color.bg_dark));

            btnGoalWeightLoss.setBackgroundResource(R.drawable.bg_chip_unselected);
            btnGoalWeightLoss.setTextColor(getColor(R.color.text_white));
        });
    }

    private void setupSubmitButton() {
        btnStartJourney.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Please enter your name");
                etName.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(ageStr)) {
                etAge.setError("Please enter your age");
                etAge.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(heightStr)) {
                etHeight.setError("Please enter height");
                etHeight.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(weightStr)) {
                etWeight.setError("Please enter weight");
                etWeight.requestFocus();
                return;
            }

            int age;
            float height;
            float weight;

            try {
                age = Integer.parseInt(ageStr);
                height = Float.parseFloat(heightStr);
                weight = Float.parseFloat(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save details in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USER_NAME, name);
            editor.putInt(KEY_USER_AGE, age);
            editor.putFloat(KEY_USER_HEIGHT, height);
            editor.putFloat(KEY_USER_WEIGHT, weight);
            editor.putString(KEY_USER_GOAL, selectedGoal);
            editor.putBoolean(KEY_IS_ONBOARDED, true);
            editor.apply();

            Toast.makeText(this, "Profile Saved! Welcome to REBORN", Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
