package com.febriana.sinitro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText, phoneEditText;
    private Button registerButton;
    private TextView loginButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase; // Realtime Database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Initialize FirebaseAuth and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.konfirmasi_password);
        nameEditText = findViewById(R.id.nama_pemilik);
        phoneEditText = findViewById(R.id.no_handphone);
        registerButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login_button);

        // Register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Login button click listener (navigate to login activity)
        loginButton.setOnClickListener(v -> {
            // Navigate to login activity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the RegisterActivity so the user can't navigate back to it
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            return;
        }

        // Register user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("RegisterActivity", "User registered successfully with UID: " + user.getUid());
                        // Save additional user data to Realtime Database
                        saveUserData(user.getUid(), name, phone);

                    } else {
                        // If registration fails, display a message to the user
                        Log.d("RegisterActivity", "User registration failed, no user data found.");
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(String userId, String name, String phone) {
        // Get current time as timestamp
        long timestamp = System.currentTimeMillis();

        // Tentukan role pengguna, misalnya admin atau petugas
        String role = "admin"; // Atau Anda bisa set berdasarkan input pengguna atau kebutuhan aplikasi

        // Buat objek User dengan data tambahan role dan timestamp
        User user = new User(name, phone, role, timestamp);

        // Simpan ke Realtime Database pada path "users/{userId}"
        mDatabase.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    // Data pengguna berhasil disimpan
                    Log.d("RegisterActivity", "User registered successfully");
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                    // Arahkan ke halaman login setelah berhasil mendaftar
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Tutup RegisterActivity agar pengguna tidak dapat kembali ke halaman ini
                })
                .addOnFailureListener(e -> {
                    // Jika gagal menyimpan data
                    Log.e("RegisterActivity", "Error saving user to Realtime Database", e);
                    Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // User class to hold user data and role
    public static class User {
        String name;
        String phone;
        String role;
        long timestamp;

        // Konstruktor kosong untuk Realtime Database
        public User() {
        }

        public User(String name, String phone, String role, long timestamp) {
            this.name = name;
            this.phone = phone;
            this.role = role;
            this.timestamp = timestamp;
        }

        // Getter dan Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
