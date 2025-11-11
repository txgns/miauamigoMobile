package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.models.User;
import com.miaumigo.app.utils.EncryptionManager;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference;
    private FirebaseUser firebaseUser;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initFirebase();
        initViews();
        loadUserData();
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
        usersReference = FirebaseDatabase.getInstance().getReference().child("users");
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
        if (firebaseUser == null || usersReference == null) {
            return;
        }
        showLoading(true);
        usersReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showLoading(false);
                        currentUser = snapshot.getValue(User.class);
                        if (currentUser == null) {
                            currentUser = new User(
                                    firebaseUser.getUid(),
                                    !TextUtils.isEmpty(firebaseUser.getDisplayName())
                                            ? firebaseUser.getDisplayName() : "",
                                    firebaseUser.getEmail(),
                                    firebaseUser.getPhoneNumber()
                            );
                        } else {
                            if (TextUtils.isEmpty(currentUser.getUid())) {
                                currentUser.setUid(firebaseUser.getUid());
                            }
                            if (TextUtils.isEmpty(currentUser.getEmail())) {
                                currentUser.setEmail(firebaseUser.getEmail());
                            }
                        }

                        EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());
                        currentUser.setName(encryptionManager.decrypt(currentUser.getName()));
                        currentUser.setEmail(encryptionManager.decrypt(currentUser.getEmail()));
                        currentUser.setPhone(encryptionManager.decrypt(currentUser.getPhone()));

                        editTextName.setText(currentUser.getName() != null ? currentUser.getName() : "");
                        editTextEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
                        editTextPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        currentUser = new User(
                                firebaseUser.getUid(),
                                !TextUtils.isEmpty(firebaseUser.getDisplayName())
                                        ? firebaseUser.getDisplayName() : "",
                                firebaseUser.getEmail(),
                                firebaseUser.getPhoneNumber()
                        );
                        editTextName.setText(currentUser.getName());
                        editTextEmail.setText(currentUser.getEmail());
                        editTextPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
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
        if (firebaseUser == null || usersReference == null) {
            showLoading(false);
            Toast.makeText(this, R.string.error_user_not_authenticated, Toast.LENGTH_LONG).show();
            return;
        }
        EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());
        currentUser.setName(encryptionManager.encrypt(name));
        currentUser.setPhone(TextUtils.isEmpty(phone) ? null : encryptionManager.encrypt(phone));
        currentUser.setEmail(encryptionManager.encrypt(firebaseUser.getEmail()));
        currentUser.setUpdatedAt(System.currentTimeMillis());
        if (TextUtils.isEmpty(currentUser.getUid())) {
            currentUser.setUid(firebaseUser.getUid());
        }

        usersReference.child(firebaseUser.getUid())
                .setValue(currentUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, R.string.message_profile_updated, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
