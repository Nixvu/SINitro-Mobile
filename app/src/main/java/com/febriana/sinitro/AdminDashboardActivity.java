package com.febriana.sinitro;

import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admindashboard);

        tableLayout = findViewById(R.id.table);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Mengambil data adminInfo
        fetchAdminInfo();

        // Mengambil data transaksi dari Firebase Realtime Database
        fetchTransactionData();
    }

    private void fetchAdminInfo() {
        mDatabase.child("adminData").child("adminInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("userName").getValue(String.class);
                    String userRole = dataSnapshot.child("userRole").getValue(String.class);
                    String income = dataSnapshot.child("income").getValue(String.class);
                    String motorCount = dataSnapshot.child("motorCount").getValue(String.class);
                    String carCount = dataSnapshot.child("carCount").getValue(String.class);
                    String staffCount = dataSnapshot.child("staffCount").getValue(String.class);

                    // Menampilkan data admin di UI (opsional, tergantung tampilan)
                    // userNameTextView.setText(userName);
                    // userRoleTextView.setText(userRole);
                    // incomeTextView.setText(income);
                    // motorCountTextView.setText(motorCount);
                    // carCountTextView.setText(carCount);
                    // staffCountTextView.setText(staffCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminDashboard", "Error getting admin data", databaseError.toException());
            }
        });
    }

    private void fetchTransactionData() {
        mDatabase.child("transaksi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Transaksi> transaksiList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaksi transaksi = snapshot.getValue(Transaksi.class);
                    transaksiList.add(transaksi);
                }
                // Menampilkan data transaksi pada TableLayout
                displayDataInTable(transaksiList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminDashboard", "Error getting transaksi data", databaseError.toException());
            }
        });
    }

    private void displayDataInTable(List<Transaksi> transaksiList) {
        // Clear table sebelum menampilkan data baru
        tableLayout.removeAllViews();

        // Menampilkan data pada TableLayout
        for (Transaksi transaksi : transaksiList) {
            TableRow tableRow = new TableRow(this);

            TextView shiftText = new TextView(this);
            shiftText.setText(transaksi.getShift());
            shiftText.setPadding(8, 8, 8, 8);
            tableRow.addView(shiftText);

            TextView tanggalText = new TextView(this);
            tanggalText.setText(transaksi.getTanggal());
            tanggalText.setPadding(8, 8, 8, 8);
            tableRow.addView(tanggalText);

            TextView qtyText = new TextView(this);
            qtyText.setText(transaksi.getQty());
            qtyText.setPadding(8, 8, 8, 8);
            tableRow.addView(qtyText);

            TextView biayaText = new TextView(this);
            biayaText.setText(transaksi.getBiaya());
            biayaText.setPadding(8, 8, 8, 8);
            tableRow.addView(biayaText);

            TextView kendaraanText = new TextView(this);
            kendaraanText.setText(transaksi.getKendaraan());
            kendaraanText.setPadding(8, 8, 8, 8);
            tableRow.addView(kendaraanText);

            // Tambahkan row ke TableLayout
            tableLayout.addView(tableRow);
        }
    }
}
