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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView createAccountTextView, forgotPasswordTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Ganti dengan nama layout Anda jika berbeda

        // Inisialisasi Firebase Auth dan Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Menyambungkan komponen UI
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        createAccountTextView = findViewById(R.id.create_account);
        forgotPasswordTextView = findViewById(R.id.forgot_password);

        // Cek jika pengguna sudah login
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Jika sudah login, langsung arahkan ke halaman utama
            checkUserRole(user.getUid());
        }

        // Fungsi tombol login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Email atau Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(email, password);
            }
        });

        // Fungsi untuk buat akun baru
        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Arahkan ke halaman registrasi
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Fungsi untuk lupa password
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Arahkan ke halaman reset password (implementasi bisa ditambahkan)
                resetPassword();
            }
        });
    }

    // Fungsi login pengguna
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login sukses, cek role pengguna
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        }
                    } else {
                        // Jika gagal login
                        Toast.makeText(LoginActivity.this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Fungsi untuk mengecek role pengguna dari Firestore
    private void checkUserRole(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Dapatkan role pengguna
                        String role = documentSnapshot.getString("role");

                        // Arahkan pengguna ke dashboard berdasarkan role
                        if ("admin".equals(role)) {
                            navigateToAdminDashboard();
                        } else if ("petugas".equals(role)) {
                            navigateToPetugasDashboard();
                        } else {
                            Toast.makeText(LoginActivity.this, "Role tidak dikenal!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Data pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengambil data pengguna!", Toast.LENGTH_SHORT).show();
                });
    }

    // Fungsi untuk mengarahkan ke dashboard admin
    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    // Fungsi untuk mengarahkan ke dashboard petugas
    private void navigateToPetugasDashboard() {
        Intent intent = new Intent(LoginActivity.this, PetugasDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    // Fungsi reset password (opsional)
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
