package com.alilab.reborn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String PREF_NAME = "REBORN_PREFS";
    private static final String KEY_IS_ONBOARDED = "is_onboarded";
    private static final int SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkOnboardingAndNavigate, SPLASH_DELAY_MS);
    }

    private void checkOnboardingAndNavigate() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isOnboarded = sharedPreferences.getBoolean(KEY_IS_ONBOARDED, false);

        Intent intent;
        if (isOnboarded) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
