package com.febriana.sinitro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // splash screen duration (in milliseconds)
    private static final String PREF_NAME = "user_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ROLE = "user_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Wait for SPLASH_TIME_OUT, then navigate based on login status and role
        new Handler().postDelayed(() -> {
            if (isUserLoggedIn()) {
                String role = getUserRole();
                if ("admin".equals(role)) {
                    navigateToAdminDashboard();
                } else if ("petugas".equals(role)) {
                    navigateToPetugasDashboard();
                } else {
                    navigateToLogin();
                }
            } else {
                navigateToLogin();
            }
            finish(); // Close SplashActivity
        }, SPLASH_TIME_OUT);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private String getUserRole() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ROLE, "");
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
    }

    private void navigateToPetugasDashboard() {
        Intent intent = new Intent(SplashActivity.this, PetugasDashboardActivity.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}