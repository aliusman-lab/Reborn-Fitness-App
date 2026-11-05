package com.alilab.reborn;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class DashboardFragment extends Fragment {

    private static final String PREF_NAME = "REBORN_PREFS";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AGE = "user_age";
    private static final String KEY_USER_HEIGHT = "user_height";
    private static final String KEY_USER_WEIGHT = "user_weight";
    private static final String KEY_USER_GOAL = "user_goal";

    private TextView tvWelcome;
    private TextView tvGoalBadge;
    private TextView tvTargetCaloriesVal;
    private ProgressBar pbCaloriesProgress;
    private TextView tvConsumedCalories;
    private TextView tvRemainingCalories;
    private TextView tvMacroProtein;
    private TextView tvMacroCarbs;
    private TextView tvMacroFats;
    private TextView tvBmiScore;
    private TextView tvBmiStatusBadge;
    private TextView tvBmiDetails;
    private TextView tvBmiDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);
        loadUserDashboardData();

        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_dash_welcome);
        tvGoalBadge = view.findViewById(R.id.tv_dash_goal_badge);
        tvTargetCaloriesVal = view.findViewById(R.id.tv_target_calories_val);
        pbCaloriesProgress = view.findViewById(R.id.pb_calories_progress);
        tvConsumedCalories = view.findViewById(R.id.tv_consumed_calories);
        tvRemainingCalories = view.findViewById(R.id.tv_remaining_calories);
        tvMacroProtein = view.findViewById(R.id.tv_macro_protein);
        tvMacroCarbs = view.findViewById(R.id.tv_macro_carbs);
        tvMacroFats = view.findViewById(R.id.tv_macro_fats);
        tvBmiScore = view.findViewById(R.id.tv_bmi_score);
        tvBmiStatusBadge = view.findViewById(R.id.tv_bmi_status_badge);
        tvBmiDetails = view.findViewById(R.id.tv_bmi_details);
        tvBmiDescription = view.findViewById(R.id.tv_bmi_description);
    }

    private void loadUserDashboardData() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userName = prefs.getString(KEY_USER_NAME, "Athlete");
        int age = prefs.getInt(KEY_USER_AGE, 25);
        float height = prefs.getFloat(KEY_USER_HEIGHT, 175.0f); // in cm
        float weight = prefs.getFloat(KEY_USER_WEIGHT, 70.0f); // in kg
        String goal = prefs.getString(KEY_USER_GOAL, "Weight Loss");

        // 1. Header & Goal Badge
        tvWelcome.setText(String.format("Welcome back, %s! 👋", userName));
        tvGoalBadge.setText(goal);

        // 2. BMI Calculation
        float heightInM = height / 100.0f;
        float bmi = (heightInM > 0) ? (weight / (heightInM * heightInM)) : 0f;

        String bmiStatus;
        String bmiDesc;

        if (bmi < 18.5) {
            bmiStatus = "Underweight";
            bmiDesc = "Your BMI indicates you are below the recommended healthy weight range.";
        } else if (bmi < 25.0) {
            bmiStatus = "Normal / Fit";
            bmiDesc = "Your BMI is in a healthy range for your body metrics.";
        } else if (bmi < 30.0) {
            bmiStatus = "Overweight";
            bmiDesc = "Your BMI indicates you are slightly above the average healthy range.";
        } else {
            bmiStatus = "Obese";
            bmiDesc = "Your BMI indicates an elevated body mass range.";
        }

        tvBmiScore.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        tvBmiStatusBadge.setText(bmiStatus);
        tvBmiDetails.setText(String.format(Locale.getDefault(), "Height: %.0f cm  •  Weight: %.1f kg", height, weight));
        tvBmiDescription.setText(bmiDesc);

        // 3. Daily Calorie Target (Mifflin-St Jeor Formula: 10 * W + 6.25 * H - 5 * A + 5)
        double bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0;
        int targetCalories;

        if ("Muscle Gain".equalsIgnoreCase(goal)) {
            targetCalories = (int) Math.round(bmr + 300);
        } else if ("Weight Loss".equalsIgnoreCase(goal)) {
            targetCalories = (int) Math.round(bmr - 400);
        } else {
            targetCalories = (int) Math.round(bmr);
        }

        // Safety fallback check
        if (targetCalories < 1200) {
            targetCalories = 1200;
        }

        tvTargetCaloriesVal.setText(String.format(Locale.getDefault(), "%,d kcal", targetCalories));

        // Simulated daily intake / remaining calories
        int consumedCalories = (int) Math.round(targetCalories * 0.45);
        int remainingCalories = targetCalories - consumedCalories;
        int progressPercent = (int) Math.round((consumedCalories * 100.0) / targetCalories);

        pbCaloriesProgress.setProgress(progressPercent);
        tvConsumedCalories.setText(String.format(Locale.getDefault(), "Consumed: %,d kcal", consumedCalories));
        tvRemainingCalories.setText(String.format(Locale.getDefault(), "%,d kcal remaining", remainingCalories));

        // 4. Macro Targets Calculation (Protein 30%, Carbs 50%, Fats 20%)
        // Protein: 4 kcal/g, Carbs: 4 kcal/g, Fats: 9 kcal/g
        int proteinGrams = (int) Math.round((targetCalories * 0.30) / 4.0);
        int carbsGrams = (int) Math.round((targetCalories * 0.50) / 4.0);
        int fatsGrams = (int) Math.round((targetCalories * 0.20) / 9.0);

        tvMacroProtein.setText(String.format(Locale.getDefault(), "%d g", proteinGrams));
        tvMacroCarbs.setText(String.format(Locale.getDefault(), "%d g", carbsGrams));
        tvMacroFats.setText(String.format(Locale.getDefault(), "%d g", fatsGrams));
    }
}
