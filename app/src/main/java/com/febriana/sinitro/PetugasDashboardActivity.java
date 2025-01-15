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

    }
}
