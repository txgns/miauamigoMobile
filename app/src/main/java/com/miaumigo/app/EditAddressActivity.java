package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.models.Address;
import com.miaumigo.app.utils.EncryptionManager;

import java.util.ArrayList;
import java.util.List;

public class EditAddressActivity extends AppCompatActivity {

    private EditText editTextStreet;
    private EditText editTextNumber;
    private EditText editTextComplement;
    private EditText editTextNeighborhood;
    private EditText editTextCity;
    private EditText editTextState;
    private EditText editTextZip;
    private CheckBox checkBoxDefault;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private Address currentAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        initFirebase();
        if (firebaseUser == null) {
            return;
        }
        initViews();
        loadAddress();
    }

    private void initFirebase() {
        FirebaseApp app = FirebaseApp.initializeApp(this);
        if (app == null && FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, R.string.error_user_not_authenticated, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void initViews() {
        editTextStreet = findViewById(R.id.editTextStreet);
        editTextNumber = findViewById(R.id.editTextNumber);
        editTextComplement = findViewById(R.id.editTextComplement);
        editTextNeighborhood = findViewById(R.id.editTextNeighborhood);
        editTextCity = findViewById(R.id.editTextCity);
        editTextState = findViewById(R.id.editTextState);
        editTextZip = findViewById(R.id.editTextZip);
        checkBoxDefault = findViewById(R.id.checkboxDefaultAddress);
        progressBar = findViewById(R.id.progressBar);

        Button buttonSave = findViewById(R.id.buttonSaveAddress);
        buttonSave.setOnClickListener(v -> saveAddress());

        Button buttonBack = findViewById(R.id.buttonBackAddress);
        buttonBack.setOnClickListener(v -> finish());
    }

    private void loadAddress() {
        if (firebaseUser == null) {
            return;
        }

        showLoading(true);
        Query query = databaseReference.child("addresses")
                .orderByChild("userId")
                .equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                List<Address> addresses = new ArrayList<>();
                EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    Address address = addressSnapshot.getValue(Address.class);
                    if (address != null) {
                        address.setId(addressSnapshot.getKey());
                        decryptAddressFields(address, encryptionManager);
                        addresses.add(address);
                    }
                }
                if (!addresses.isEmpty()) {
                    currentAddress = getDefaultAddress(addresses);
                    populateFields(currentAddress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                if (!TextUtils.isEmpty(error.getMessage())) {
                    Toast.makeText(EditAddressActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private Address getDefaultAddress(List<Address> addresses) {
        for (Address address : addresses) {
            if (address != null && address.isDefault()) {
                return address;
            }
        }
        return addresses.get(0);
    }

    private void populateFields(Address address) {
        if (address == null) {
            return;
        }

        editTextStreet.setText(address.getStreet());
        editTextNumber.setText(address.getNumber());
        editTextComplement.setText(address.getComplement());
        editTextNeighborhood.setText(address.getNeighborhood());
        editTextCity.setText(address.getCity());
        editTextState.setText(address.getState());
        editTextZip.setText(address.getZipCode());
        checkBoxDefault.setChecked(address.isDefault());
    }

    private void decryptAddressFields(Address address, EncryptionManager encryptionManager) {
        address.setStreet(encryptionManager.decrypt(address.getStreet()));
        address.setNumber(encryptionManager.decrypt(address.getNumber()));
        address.setComplement(encryptionManager.decrypt(address.getComplement()));
        address.setNeighborhood(encryptionManager.decrypt(address.getNeighborhood()));
        address.setCity(encryptionManager.decrypt(address.getCity()));
        address.setState(encryptionManager.decrypt(address.getState()));
        address.setZipCode(encryptionManager.decrypt(address.getZipCode()));
    }

    private void saveAddress() {
        if (firebaseUser == null) {
            return;
        }

        String street = editTextStreet.getText().toString().trim();
        String number = editTextNumber.getText().toString().trim();
        String neighborhood = editTextNeighborhood.getText().toString().trim();
        String city = editTextCity.getText().toString().trim();
        String state = editTextState.getText().toString().trim();
        String zip = editTextZip.getText().toString().trim();

        if (TextUtils.isEmpty(street) || TextUtils.isEmpty(number) ||
                TextUtils.isEmpty(neighborhood) || TextUtils.isEmpty(city) ||
                TextUtils.isEmpty(state) || TextUtils.isEmpty(zip)) {
            Toast.makeText(this, R.string.error_address_required_fields, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading(true);

        if (currentAddress == null) {
            currentAddress = new Address();
            currentAddress.setUserId(firebaseUser.getUid());
        }

        String complement = editTextComplement.getText().toString().trim();
        EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());

        Address addressToSave = new Address();
        addressToSave.setId(currentAddress.getId());
        addressToSave.setUserId(currentAddress.getUserId());
        addressToSave.setStreet(encryptionManager.encrypt(street));
        addressToSave.setNumber(encryptionManager.encrypt(number));
        addressToSave.setComplement(encryptionManager.encrypt(complement));
        addressToSave.setNeighborhood(encryptionManager.encrypt(neighborhood));
        addressToSave.setCity(encryptionManager.encrypt(city));
        addressToSave.setState(encryptionManager.encrypt(state));
        addressToSave.setZipCode(encryptionManager.encrypt(zip));
        addressToSave.setDefault(checkBoxDefault.isChecked());

        currentAddress.setStreet(street);
        currentAddress.setNumber(number);
        currentAddress.setComplement(complement);
        currentAddress.setNeighborhood(neighborhood);
        currentAddress.setCity(city);
        currentAddress.setState(state);
        currentAddress.setZipCode(zip);
        currentAddress.setDefault(checkBoxDefault.isChecked());

        DatabaseReference addressesRef = databaseReference.child("addresses");
        boolean isNewAddress = TextUtils.isEmpty(currentAddress.getId());
        if (isNewAddress) {
            String addressId = addressesRef.push().getKey();
            if (TextUtils.isEmpty(addressId)) {
                showLoading(false);
                Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
                return;
            }
            currentAddress.setId(addressId);
            addressToSave.setId(addressId);
        }
        currentAddress.setUpdatedAt(System.currentTimeMillis());
        addressToSave.setUpdatedAt(currentAddress.getUpdatedAt());

        addressesRef.child(currentAddress.getId()).setValue(addressToSave)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (currentAddress.isDefault()) {
                                setDefaultAddress(currentAddress.getId());
                            } else {
                                handleAddressSaved();
                            }
                        } else {
                            showLoading(false);
                            Toast.makeText(EditAddressActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void handleAddressSaved() {
        showLoading(false);
        Toast.makeText(EditAddressActivity.this, R.string.message_address_saved, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void setDefaultAddress(String addressId) {
        Query query = databaseReference.child("addresses")
                .orderByChild("userId")
                .equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Task<Void>> tasks = new ArrayList<>();
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    boolean isTarget = addressSnapshot.getKey() != null && addressSnapshot.getKey().equals(addressId);
                    tasks.add(addressSnapshot.getRef().child("isDefault").setValue(isTarget));
                }

                if (tasks.isEmpty()) {
                    handleAddressSaved();
                    return;
                }

                Task<Void> aggregate = Tasks.whenAll(tasks);
                aggregate.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        handleAddressSaved();
                    } else {
                        showLoading(false);
                        Toast.makeText(EditAddressActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(EditAddressActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
