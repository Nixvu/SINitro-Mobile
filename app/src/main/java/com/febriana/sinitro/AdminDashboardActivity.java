package com.febriana.sinitro;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView userNameTextView, userRoleTextView, incomeTextView, motorCountTextView,
            carCountTextView, staffCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admindashboard);

        // Menghubungkan variabel dengan komponen XML
        userNameTextView = findViewById(R.id.user_name);
        userRoleTextView = findViewById(R.id.user_role);
        incomeTextView = findViewById(R.id.income);
        motorCountTextView = findViewById(R.id.motor_count);
        carCountTextView = findViewById(R.id.car_count);
        staffCountTextView = findViewById(R.id.staff_count);

        // Dummy data untuk testing
        String userName = "Febriana";
        String userRole = "Super Admin";
        String income = "Rp 10.260.000,-";
        String motorCount = "2908";
        String carCount = "2908";
        String staffCount = "4";

        // Menampilkan data pada tampilan
        userNameTextView.setText("Halo, " + userName);
        userRoleTextView.setText(userRole);
        incomeTextView.setText(income);
        motorCountTextView.setText(motorCount);
        carCountTextView.setText(carCount);
        staffCountTextView.setText(staffCount);
    }
}
