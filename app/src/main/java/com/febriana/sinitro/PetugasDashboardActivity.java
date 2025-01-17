package com.febriana.sinitro;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
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

public class PetugasDashboardActivity extends AppCompatActivity {

    private Button buttonBuatTransaksi, buttonAbsensi, buttonDaftarTransaksi, buttonLaporan;
    private TableLayout tableLayout;
    private DatabaseReference mDatabase;
    private Map<String, Integer> servicePrices = new HashMap<>();
    private List<String> serviceTypes;
    private TextView userNameTextView, userShiftTextView, incomeTextView, motorCountTextView, carCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petugasdashboard); // Ensure this matches your XML filename

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        userNameTextView = findViewById(R.id.user_name);
        userShiftTextView = findViewById(R.id.user_shift);
        incomeTextView = findViewById(R.id.income_count);
        motorCountTextView = findViewById(R.id.motor_total);
        carCountTextView = findViewById(R.id.car_total);
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

        // Initialize buttons
        buttonBuatTransaksi = findViewById(R.id.button_buattransaksi);
        buttonBuatTransaksi.setOnClickListener(v -> {
            showTransactionForm(null);  // null for new transaction
        });

        buttonAbsensi = findViewById(R.id.button_absensi);
        buttonAbsensi.setOnClickListener(v -> {
            // Handle Absensi button click
        });

        buttonDaftarTransaksi = findViewById(R.id.button_daftartransaksi);
        buttonDaftarTransaksi.setOnClickListener(v -> {
            // Handle Daftar Transaksi button click
        });

        buttonLaporan = findViewById(R.id.button_laporan);
        buttonLaporan.setOnClickListener(v -> {
            // Handle Laporan button click
        });

        // Fetch petugas info and transaction data
        fetchPetugasInfo();
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
                    PetugasDashboardActivity.this,
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

            // Check if a RadioButton is selected
            int selectedVehicleId = radioGroupVehicleType.getCheckedRadioButtonId();
            if (selectedVehicleId == -1) {
                Toast.makeText(PetugasDashboardActivity.this, "Jenis kendaraan harus dipilih", Toast.LENGTH_SHORT).show();
                return; // Stop further execution if none is selected
            }

            // Get text from the selected RadioButton
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

    private void fetchPetugasInfo() {
        mDatabase.child("adminData").child("adminInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("userName").getValue(String.class);
                    String userRole = dataSnapshot.child("userRole").getValue(String.class);
                    String income = dataSnapshot.child("income").getValue(String.class);
                    String motorCount = dataSnapshot.child("motorCount").getValue(String.class);
                    String carCount = dataSnapshot.child("carCount").getValue(String.class);

                    // Display admin data in UI
                    if (userNameTextView != null) userNameTextView.setText("Halo, " + userName);
                    if (userShiftTextView != null) userShiftTextView.setText(userRole);
                    if (incomeTextView != null) incomeTextView.setText(income);
                    if (motorCountTextView != null) motorCountTextView.setText(motorCount);
                    if (carCountTextView != null) carCountTextView.setText(carCount);
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

                // Clear TableLayout before adding new data
                tableLayout.removeAllViews();

                // Add table header row
                TableRow headerRow = new TableRow(PetugasDashboardActivity.this);
                String[] headers = {"No", "Nopol", "Qty", "Biaya", "Kendaraan", "Tanggal", "Aksi"};
                for (String header : headers) {
                    TextView textView = createStyledTextView(header, true);
                    headerRow.addView(textView);
                }
                tableLayout.addView(headerRow);

                // Display transaction data
                int index = 1;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaksi transaksi = snapshot.getValue(Transaksi.class);
                    if (transaksi != null) {
                        TableRow tableRow = new TableRow(PetugasDashboardActivity.this);

                        // Add transaction data columns
                        tableRow.addView(createStyledTextView(String.valueOf(index++), false));
                        tableRow.addView(createStyledTextView(transaksi.getNomorPlat(), false));
                        tableRow.addView(createStyledTextView(transaksi.getKuantitas(), false));
                        tableRow.addView(createStyledTextView(transaksi.getHarga(), false));
                        tableRow.addView(createStyledTextView(transaksi.getKendaraan(), false));
                        tableRow.addView(createStyledTextView(transaksi.getTanggal(), false));

                        // Add action buttons (Edit, Delete)
                        tableRow.addView(createActionButton("Edit", Color.BLUE, v -> showTransactionForm(snapshot.getKey())));
                        tableRow.addView(createActionButton("Delete", Color.RED, v -> deleteTransaction(snapshot.getKey())));

                        // Add the row to the table
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

                // Update UI with calculated data
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

    private TextView createStyledTextView(String text, boolean isHeader) {
        TextView textView = new TextView(PetugasDashboardActivity.this);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextColor(isHeader ? Color.parseColor("#272B3C") : Color.BLACK);
        textView.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        textView.setBackgroundResource(isHeader ? R.drawable.hdtable : 0);

        // Set layout parameters
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createActionButton(String label, int color, View.OnClickListener listener) {
        TextView actionButton = new TextView(PetugasDashboardActivity.this);
        actionButton.setText(label);
        actionButton.setTextColor(color);
        actionButton.setGravity(Gravity.CENTER);
        actionButton.setPadding(8, 8, 8, 8);
        actionButton.setOnClickListener(listener);
        return actionButton;
    }

    private void deleteTransaction(String transactionId) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                .setPositiveButton("Hapus", (dialog, which) -> mDatabase.child("transaksi").child(transactionId).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus transaksi: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Batal", null)
                .show();
    }
}