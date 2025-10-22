package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.models.User;
import com.miaumigo.app.services.FirebaseAuthService;
import com.miaumigo.app.services.FirebaseDatabaseService;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private ProgressBar progressBar;
    private FirebaseAuthService authService;
    private FirebaseDatabaseService databaseService;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initServices();
        initViews();
        loadUserData();
    }

    private void initServices() {
        authService = new FirebaseAuthService(this);
        databaseService = new FirebaseDatabaseService(this);
    }

    private void initViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        progressBar = findViewById(R.id.progressBar);
        Button buttonSave = findViewById(R.id.buttonSaveProfile);

        editTextEmail.setEnabled(false);

        buttonSave.setOnClickListener(v -> saveProfile());

        Button buttonBack = findViewById(R.id.buttonBackProfile);
        buttonBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = authService.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, R.string.error_user_not_authenticated, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showLoading(true);
        databaseService.getUser(firebaseUser.getUid(), new FirebaseDatabaseService.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                showLoading(false);
                currentUser = user;
                if (currentUser != null) {
                    editTextName.setText(currentUser.getName());
                    editTextEmail.setText(currentUser.getEmail());
                    editTextPhone.setText(currentUser.getPhone());
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
                currentUser = new User(firebaseUser.getUid(),
                        !TextUtils.isEmpty(firebaseUser.getDisplayName()) ? firebaseUser.getDisplayName() : "",
                        firebaseUser.getEmail(),
                        firebaseUser.getPhoneNumber());
                editTextEmail.setText(firebaseUser.getEmail());
                editTextName.setText(currentUser.getName());
                editTextPhone.setText(currentUser.getPhone());
            }
        });
    }

    private void saveProfile() {
        if (currentUser == null) {
            return;
        }

        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError(getString(R.string.error_name_required));
            editTextName.requestFocus();
            return;
        }

        showLoading(true);
        currentUser.setName(name);
        currentUser.setPhone(TextUtils.isEmpty(phone) ? null : phone);

        databaseService.updateUser(currentUser, new FirebaseDatabaseService.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, R.string.message_profile_updated, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
