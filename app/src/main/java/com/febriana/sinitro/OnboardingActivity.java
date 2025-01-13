package com.febriana.sinitro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabIndicator;
    private Button btnNext;
    private static final String PREF_NAME = "onboarding_pref";
    private static final String KEY_FIRST_TIME = "is_first_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Periksa apakah pengguna sudah melihat onboarding sebelumnya
        if (!isFirstTimeLaunch()) {
            navigateToLogin();
            return; // Tidak perlu melanjutkan ke onboarding
        }

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        tabIndicator = findViewById(R.id.tabIndicator);
        btnNext = findViewById(R.id.btnNext);

        // Data Onboarding
        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(R.drawable.sp1, "Mencatat Dan Mengelola Transaksi Di SINITRO", "Cara termudah dalam mengelola dan pencatatan transaksi usaha anda tanpa menggunakan cara manual menulis buku lagi"));
        onboardingItems.add(new OnboardingItem(R.drawable.sp2, "Fiture Aplikasi Lengkap Dan Keamanan Terjamin", "Fiture yang ada diaplikasi lengkap baik export data, management user tampilan user friendly, Keamanan dan masih banyak lagi."));
        onboardingItems.add(new OnboardingItem(R.drawable.sp3, "Semua Hanya Dalam Satu Genggaman Anda", "Tunggu apalagi mari mulai sekarang juga cukup ikuti langkah berikutnya dengan mengklik login atau daftar"));

        OnboardingAdapter adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);

        // Tab Indicator
        new TabLayoutMediator(tabIndicator, viewPager, (tab, position) -> {}).attach();

        // Tombol Next
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < onboardingItems.size()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                // Tandai onboarding sudah selesai
                setFirstTimeLaunch(false);


                // Pindah ke halaman login
                navigateToLogin();
            }
        });
    }

    // Fungsi untuk memeriksa apakah ini pertama kali aplikasi diluncurkan
    private boolean isFirstTimeLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_FIRST_TIME, true);
    }

    private void setFirstTimeLaunch(boolean isFirstTime) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_FIRST_TIME, isFirstTime);
        editor.apply();  // Pastikan diterapkan
    }

    // Fungsi untuk berpindah ke halaman Login
    private void navigateToLogin() {
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
