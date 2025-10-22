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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference;
    private FirebaseUser firebaseUser;
    private UserProfile currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initFirebase();
        initViews();
        loadUserData();
    }

    private void initFirebase() {
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
                        currentUser = snapshot.getValue(UserProfile.class);
                        if (currentUser == null) {
                            currentUser = new UserProfile();
                            currentUser.uid = firebaseUser.getUid();
                            currentUser.email = firebaseUser.getEmail();
                            currentUser.name = !TextUtils.isEmpty(firebaseUser.getDisplayName())
                                    ? firebaseUser.getDisplayName() : "";
                            currentUser.phone = firebaseUser.getPhoneNumber();
                        } else {
                            if (TextUtils.isEmpty(currentUser.uid)) {
                                currentUser.uid = firebaseUser.getUid();
                            }
                            if (TextUtils.isEmpty(currentUser.email)) {
                                currentUser.email = firebaseUser.getEmail();
                            }
                        }

                        editTextName.setText(currentUser.name != null ? currentUser.name : "");
                        editTextEmail.setText(currentUser.email != null ? currentUser.email : "");
                        editTextPhone.setText(currentUser.phone != null ? currentUser.phone : "");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        currentUser = new UserProfile();
                        currentUser.uid = firebaseUser.getUid();
                        currentUser.email = firebaseUser.getEmail();
                        currentUser.name = !TextUtils.isEmpty(firebaseUser.getDisplayName())
                                ? firebaseUser.getDisplayName() : "";
                        currentUser.phone = firebaseUser.getPhoneNumber();
                        editTextName.setText(currentUser.name);
                        editTextEmail.setText(currentUser.email);
                        editTextPhone.setText(currentUser.phone != null ? currentUser.phone : "");
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
        currentUser.name = name;
        currentUser.phone = TextUtils.isEmpty(phone) ? null : phone;
        currentUser.email = firebaseUser.getEmail();
        currentUser.updatedAt = System.currentTimeMillis();

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

    private static class UserProfile {
        public String uid;
        public String name;
        public String email;
        public String phone;
        public long updatedAt;

        public UserProfile() {
        }
    }
}
