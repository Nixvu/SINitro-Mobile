package com.febriana.sinitro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;  // Pastikan Anda mengimpor Log

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // waktu splash screen (dalam milidetik)
    private static final String PREF_NAME = "onboarding_pref";
    private static final String KEY_FIRST_TIME = "is_first_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Ganti dengan layout splash.xml Anda

        // Menunggu selama SPLASH_TIME_OUT, lalu beralih ke halaman yang sesuai
        new Handler().postDelayed(() -> {
            boolean isFirstTime = isFirstTimeLaunch();  // Mendapatkan nilai dari fungsi
            Log.d("SplashActivity", "isFirstTimeLaunch: " + isFirstTime); // Menambahkan log yang benar

            if (isFirstTime) {
                // Tampilkan onboarding jika pertama kali
                Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                startActivity(intent);
            } else {
                // Arahkan ke login jika sudah selesai onboarding
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            finish(); // Menutup SplashActivity
        }, SPLASH_TIME_OUT);
    }

    // Memeriksa apakah ini pertama kali aplikasi diluncurkan
    private boolean isFirstTimeLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_FIRST_TIME, true); // Mengembalikan nilai boolean
    }
}
