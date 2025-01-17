package com.febriana.sinitro;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView userNameTextView, userRoleTextView, incomeTextView, motorCountTextView, carCountTextView, staffCountTextView;
    private TableLayout tableLayout;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admindashboard);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current user session
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize UI components
        userNameTextView = findViewById(R.id.user_name);
        userRoleTextView = findViewById(R.id.user_role);
        incomeTextView = findViewById(R.id.income_count);
        motorCountTextView = findViewById(R.id.motor_total);
        carCountTextView = findViewById(R.id.car_total);
        staffCountTextView = findViewById(R.id.staff_count);
        tableLayout = findViewById(R.id.table_layout);

        // Menambahkan kode untuk tombol "Petugas"
        Button buttonPetugas = findViewById(R.id.button_petugas);
        buttonPetugas.setOnClickListener(v -> {
            // Mengarahkan ke ListPetugasActivity
            Intent intent = new Intent(AdminDashboardActivity.this, ListPetugasActivity.class);
            startActivity(intent);
        });

        // Fetch admin info, transaction data, and petugas count
        fetchAdminInfo();
        fetchTransactionData();
        fetchPetugasCount();
    }

    protected void onResume() {
        super.onResume();
        fetchPetugasCount();
        fetchTransactionData();
    }

    // Existing method to fetch admin info
    private void fetchAdminInfo() {
        // Check if the current user is authenticated
        if (currentUser != null) {
            String userId = currentUser.getUid();  // Get the current user's ID

            // Fetch the admin data from the 'users' node based on user session
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.child("name").getValue(String.class);
                        String userRole = dataSnapshot.child("role").getValue(String.class);  // Update key name to 'role'

                        // Display the admin name and role on the UI
                        if (userName != null) {
                            userNameTextView.setText("Hi, " + userName);
                        }
                        if (userRole != null) {
                            userRoleTextView.setText(userRole);
                        }
                    } else {
                        Toast.makeText(AdminDashboardActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching admin info: " + databaseError.getMessage());
                }
            });
        } else {
            Toast.makeText(AdminDashboardActivity.this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }

    // New method to fetch the count of petugas
    private void fetchPetugasCount() {
        mDatabase.child("users").orderByChild("role").equalTo("petugas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Count the number of petugas (users with role 'petugas')
                int petugasCount = (int) dataSnapshot.getChildrenCount();
                // Update the staff count UI
                staffCountTextView.setText(String.valueOf(petugasCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching petugas count: " + databaseError.getMessage());
            }
        });
    }

    // Existing method to fetch transaction data
    private void fetchTransactionData() {
        mDatabase.child("transaksi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int motorCount = 0;
                int carCount = 0;
                double totalIncome = 0;

                // Clear TableLayout before adding new data
                tableLayout.removeAllViews();

                // Add table header row
                TableRow headerRow = new TableRow(AdminDashboardActivity.this);
                String[] headers = {"No", "Shift", "Qty", "Biaya", "Kendaraan", "Tanggal"};
                for (String header : headers) {
                    TextView textView = new TextView(AdminDashboardActivity.this);
                    textView.setText(header);
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(8, 8, 8, 8);
                    textView.setTextColor(Color.parseColor("#272B3C"));
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setBackgroundResource(R.drawable.hdtable);

                    TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                    textView.setLayoutParams(params);
                    headerRow.addView(textView);
                }
                tableLayout.addView(headerRow);

                // Display transaction data
                int index = 1;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaksi transaksi = snapshot.getValue(Transaksi.class);
                    if (transaksi != null) {
                        TableRow tableRow = new TableRow(AdminDashboardActivity.this);
                        String[] rowData = {
                                String.valueOf(index++),
                                transaksi.getShift() != null ? transaksi.getShift() : "-",
                                transaksi.getKuantitas(),
                                transaksi.getHarga(),
                                transaksi.getKendaraan(),
                                transaksi.getTanggal()
                        };

                        for (String cellData : rowData) {
                            TextView textView = new TextView(AdminDashboardActivity.this);
                            textView.setText(cellData);
                            textView.setGravity(Gravity.CENTER);
                            textView.setPadding(8, 8, 8, 8);
                            textView.setTextColor(Color.BLACK);

                            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            textView.setLayoutParams(params);
                            tableRow.addView(textView);
                        }

                        tableLayout.addView(tableRow);

                        // Count motor and car transactions
                        if ("Motor".equalsIgnoreCase(transaksi.getKendaraan())) {
                            motorCount++;
                        } else if ("Mobil".equalsIgnoreCase(transaksi.getKendaraan())) {
                            carCount++;
                        }

                        // Calculate total income
                        try {
                            String hargaStr = transaksi.getHarga().replace("Rp ", "").replace(",", "");
                            totalIncome += Double.parseDouble(hargaStr);
                        } catch (NumberFormatException e) {
                            Log.e("Firebase", "Error parsing harga: " + e.getMessage());
                        }
                    }
                }

                motorCountTextView.setText(String.valueOf(motorCount));
                carCountTextView.setText(String.valueOf(carCount));
                incomeTextView.setText("Rp " + NumberFormat.getInstance().format(totalIncome));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });
    }
}
