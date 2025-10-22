package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.models.Address;
import com.miaumigo.app.services.FirebaseAuthService;
import com.miaumigo.app.services.FirebaseDatabaseService;

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

    private FirebaseAuthService authService;
    private FirebaseDatabaseService databaseService;
    private FirebaseUser firebaseUser;
    private Address currentAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        initServices();
        if (firebaseUser == null) {
            return;
        }
        initViews();
        loadAddress();
    }

    private void initServices() {
        authService = new FirebaseAuthService(this);
        databaseService = new FirebaseDatabaseService(this);
        firebaseUser = authService.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, R.string.error_user_not_authenticated, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
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
        databaseService.getUserAddresses(firebaseUser.getUid(), new FirebaseDatabaseService.ListCallback<Address>() {
            @Override
            public void onSuccess(List<Address> data) {
                showLoading(false);
                if (data != null && !data.isEmpty()) {
                    currentAddress = getDefaultAddress(data);
                    populateFields(currentAddress);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                if (!TextUtils.isEmpty(error)) {
                    Toast.makeText(EditAddressActivity.this, error, Toast.LENGTH_LONG).show();
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

        currentAddress.setStreet(street);
        currentAddress.setNumber(number);
        currentAddress.setComplement(editTextComplement.getText().toString().trim());
        currentAddress.setNeighborhood(neighborhood);
        currentAddress.setCity(city);
        currentAddress.setState(state);
        currentAddress.setZipCode(zip);
        currentAddress.setDefault(checkBoxDefault.isChecked());

        FirebaseDatabaseService.DataCallback<Void> callback = new FirebaseDatabaseService.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (currentAddress.isDefault()) {
                    databaseService.setDefaultAddress(firebaseUser.getUid(), currentAddress.getId(), new FirebaseDatabaseService.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            handleAddressSaved();
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            Toast.makeText(EditAddressActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handleAddressSaved();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(EditAddressActivity.this, error, Toast.LENGTH_LONG).show();
            }
        };

        if (TextUtils.isEmpty(currentAddress.getId())) {
            databaseService.createAddress(currentAddress, callback);
        } else {
            databaseService.updateAddress(currentAddress, callback);
        }
    }

    private void handleAddressSaved() {
        showLoading(false);
        Toast.makeText(EditAddressActivity.this, R.string.message_address_saved, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
