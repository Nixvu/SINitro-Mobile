package com.febriana.sinitro;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button registerPetugasButton;
    private TableLayout tableLayout;
    private DatabaseReference mDatabase;
    private Map<String, Integer> servicePrices = new HashMap<>();
    private List<String> serviceTypes;
    private TextView userNameTextView, userRoleTextView, incomeTextView, motorCountTextView, carCountTextView, staffCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admindashboard);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        userNameTextView = findViewById(R.id.user_name);
        userRoleTextView = findViewById(R.id.user_role);
        incomeTextView = findViewById(R.id.income_count);
        motorCountTextView = findViewById(R.id.motor_total);
        carCountTextView = findViewById(R.id.car_total);
        staffCountTextView = findViewById(R.id.staff_count);

        tableLayout = findViewById(R.id.table_layout);

        // Initialize service types and prices
        serviceTypes = new ArrayList<>();
        serviceTypes.add("Isi Tambah");
        serviceTypes.add("Isi Awal");
        serviceTypes.add("Tambal Ban");

        servicePrices.put("Motor Isi Tambah", 3000);
        servicePrices.put("Motor Isi Awal", 5000);
        servicePrices.put("Motor Tambal Ban", 8000);
        servicePrices.put("Mobil Isi Tambah", 5000);
        servicePrices.put("Mobil Isi Awal", 7000);
        servicePrices.put("Mobil Tambal Ban", 10000);

        // Inisialisasi button lainnya
        Button buttonBuatTransaksi = findViewById(R.id.button_buattransaksi);
        buttonBuatTransaksi.setOnClickListener(v -> {
            showTransactionForm(null);  // null untuk transaksi baru
        });

        // Menambahkan kode untuk tombol "Petugas"
        Button buttonPetugas = findViewById(R.id.button_petugas);
        buttonPetugas.setOnClickListener(v -> {
            // Mengarahkan ke ListPetugasActivity
            Intent intent = new Intent(AdminDashboardActivity.this, ListPetugasActivity.class);
            startActivity(intent);
        });

        // Fetch admin info and transaction data
        fetchAdminInfo();
        fetchTransactionData();
    }

    private void showTransactionForm(String transaksiId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_form_transaksi, null);

        // Initialize Views
        EditText etPlateNumber = dialogView.findViewById(R.id.etPlateNumber);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        Spinner spinnerServiceType = dialogView.findViewById(R.id.spinnerServiceType);
        RadioGroup radioGroupVehicleType = dialogView.findViewById(R.id.radioGroupVehicleType);
        TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        // Initialize ArrayAdapter for Spinner
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, serviceTypes);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceType.setAdapter(serviceAdapter);

        // Set up date picker
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AdminDashboardActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String formattedDate = String.format("%02d-%02d-%04d", dayOfMonth, monthOfYear + 1, year1);
                        etDate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Build and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set up submit button
        btnSubmit.setOnClickListener(v -> {
            String plateNumber = etPlateNumber.getText().toString();

            // Cek apakah ada RadioButton yang dipilih
            int selectedVehicleId = radioGroupVehicleType.getCheckedRadioButtonId();
            if (selectedVehicleId == -1) {
                Toast.makeText(AdminDashboardActivity.this, "Jenis kendaraan harus dipilih", Toast.LENGTH_SHORT).show();
                return; // Jika tidak ada yang dipilih, hentikan eksekusi lebih lanjut
            }

            // Ambil teks dari RadioButton yang dipilih
            RadioButton selectedVehicleType = dialogView.findViewById(selectedVehicleId);
            String vehicleType = selectedVehicleType != null ? selectedVehicleType.getText().toString() : "";

            String serviceType = spinnerServiceType.getSelectedItem().toString();
            String quantityStr = etQuantity.getText().toString();
            String date = etDate.getText().toString();

            if (validateInputs(plateNumber, quantityStr, date)) {
                processTransaction(plateNumber, vehicleType, serviceType, quantityStr, date, tvPrice, dialog);
            }
        });

        dialog.show();
    }

    private void processTransaction(String plateNumber, String vehicleType, String serviceType, String quantityStr, String date, TextView tvPrice, AlertDialog dialog) {
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah harus berupa angka.", Toast.LENGTH_SHORT).show();
            return;
        }

        String serviceKey = vehicleType + " " + serviceType;
        Integer pricePerUnit = servicePrices.get(serviceKey);

        if (pricePerUnit == null) {
            Toast.makeText(this, "Layanan tidak ditemukan untuk kendaraan ini.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPrice = pricePerUnit * quantity;
        String formattedPrice = NumberFormat.getInstance().format(totalPrice);
        tvPrice.setText("Rp " + formattedPrice);

        // Create the transaction object
        Transaksi transaction = new Transaksi(vehicleType, date, String.valueOf(quantity), serviceType, plateNumber, "Rp " + formattedPrice, "Shift Pagi");

        // Save the transaction to Firebase
        mDatabase.child("transaksi").push().setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Transaksi berhasil untuk kendaraan: " + transaction.getKendaraan(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // Close the dialog after successful transaction
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Terjadi kesalahan saat menyimpan transaksi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String plateNumber, String quantityStr, String date) {
        if (TextUtils.isEmpty(plateNumber)) {
            Toast.makeText(this, "Nomor plat kendaraan harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Jumlah layanan harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Tanggal harus diisi", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

                    // Menampilkan data admin di UI
                    if (userNameTextView != null) userNameTextView.setText("Hi, " + userName);
                    if (userRoleTextView != null) userRoleTextView.setText(userRole);
                    if (incomeTextView != null) incomeTextView.setText(income);
                    if (motorCountTextView != null) motorCountTextView.setText(motorCount);
                    if (carCountTextView != null) carCountTextView.setText(carCount);
                    if (staffCountTextView != null) staffCountTextView.setText(staffCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchTransactionData() {
        mDatabase.child("transaksi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int motorCount = 0;
                int carCount = 0;
                double totalIncome = 0;

                // Bersihkan TableLayout sebelum menambahkan data baru
                tableLayout.removeAllViews();

                // Menambahkan header tabel
                TableRow headerRow = new TableRow(AdminDashboardActivity.this);
                TextView tvHeader1 = new TextView(AdminDashboardActivity.this);
                tvHeader1.setText("Nomor Plat");
                tvHeader1.setGravity(Gravity.CENTER);
                headerRow.addView(tvHeader1);

                TextView tvHeader2 = new TextView(AdminDashboardActivity.this);
                tvHeader2.setText("Jenis Layanan");
                tvHeader2.setGravity(Gravity.CENTER);
                headerRow.addView(tvHeader2);

                TextView tvHeader3 = new TextView(AdminDashboardActivity.this);
                tvHeader3.setText("Harga");
                tvHeader3.setGravity(Gravity.CENTER);
                headerRow.addView(tvHeader3);

                tableLayout.addView(headerRow);

                // Menampilkan data transaksi dalam TableLayout dan menghitung data untuk Card
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaksi transaksi = snapshot.getValue(Transaksi.class);
                    if (transaksi != null) {
                        TableRow tableRow = new TableRow(AdminDashboardActivity.this);

                        // Nomor plat kendaraan
                        TextView tvPlateNumber = new TextView(AdminDashboardActivity.this);
                        tvPlateNumber.setText(transaksi.getNomorPlat());
                        tvPlateNumber.setGravity(Gravity.CENTER);
                        tableRow.addView(tvPlateNumber);

                        // Jenis layanan
                        TextView tvServiceType = new TextView(AdminDashboardActivity.this);
                        tvServiceType.setText(transaksi.getJenisLayanan());
                        tvServiceType.setGravity(Gravity.CENTER);
                        tableRow.addView(tvServiceType);

                        // Harga
                        TextView tvPrice = new TextView(AdminDashboardActivity.this);
                        tvPrice.setText(transaksi.getHarga());
                        tvPrice.setGravity(Gravity.CENTER);
                        tableRow.addView(tvPrice);

                        // Menambahkan baris ke dalam TableLayout
                        tableLayout.addView(tableRow);

                        // Hitung jumlah motor dan mobil
                        if (transaksi.getKendaraan().equalsIgnoreCase("Motor")) {
                            motorCount++;
                        } else if (transaksi.getKendaraan().equalsIgnoreCase("Mobil")) {
                            carCount++;
                        }

                        // Hitung total pemasukan (hapus simbol "Rp " dan tanda koma, kemudian parsing angka)
                        try {
                            String hargaStr = transaksi.getHarga().replace("Rp ", "").replace(",", "");
                            totalIncome += Double.parseDouble(hargaStr);
                        } catch (NumberFormatException e) {
                            Log.e("Firebase", "Error parsing harga: " + e.getMessage());
                        }
                    }
                }

                // Update UI dengan data yang dihitung untuk Card
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
