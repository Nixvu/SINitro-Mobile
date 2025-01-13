package com.febriana.sinitro;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PetugasDashboardActivity extends AppCompatActivity {

    private TextView userName, userRole, income, motorCount, carCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petugasdashboard);

        // Initialize views
        userName = findViewById(R.id.user_name);
        userRole = findViewById(R.id.user_role);
        income = findViewById(R.id.income);
        motorCount = findViewById(R.id.motor_count);
        carCount = findViewById(R.id.car_count);

        // Set sample data
        userName.setText("Halo, Febriana");
        userRole.setText("Petugas Shift 1");
        income.setText("Rp 10.260.000,-");
        motorCount.setText("2908");
        carCount.setText("1290");
    }
}
