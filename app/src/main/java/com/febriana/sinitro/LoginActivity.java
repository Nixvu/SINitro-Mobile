package com.febriana.sinitro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView createAccountTextView, forgotPasswordTextView;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Pastikan layout file bernama 'login.xml'

        // Initialize Firebase Auth dan Realtime Database
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Menghubungkan komponen UI
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        createAccountTextView = findViewById(R.id.create_account);
        forgotPasswordTextView = findViewById(R.id.forgot_password);

        // Cek jika sudah login
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            checkUserRole(user.getUid());
        }

        // Fungsi login ketika tombol ditekan
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Email atau Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });

        // Fungsi untuk membuat akun baru
        createAccountTextView.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Fungsi reset password
        forgotPasswordTextView.setOnClickListener(v -> resetPassword());
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String uid) {
        // Akses Realtime Database untuk mengambil role pengguna
        DatabaseReference usersRef = database.getReference("users").child(uid);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if ("admin".equals(role)) {
                        navigateToAdminDashboard();
                    } else if ("petugas".equals(role)) {
                        navigateToPetugasDashboard();
                    } else {
                        Toast.makeText(LoginActivity.this, "Role tidak dikenal!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Dokumen pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Gagal mengambil data pengguna: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToAdminDashboard() {
        startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
        finish();
    }

    private void navigateToPetugasDashboard() {
        startActivity(new Intent(LoginActivity.this, PetugasDashboardActivity.class));
        finish();
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Masukkan email terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Instruksi reset password telah dikirim ke email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Gagal mengirim email reset password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
