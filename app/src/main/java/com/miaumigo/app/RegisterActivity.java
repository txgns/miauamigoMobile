package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miaumigo.app.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private Button buttonBack;
    private TextView textViewRegisterTitle;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private boolean isVendorRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Verifica se é registro de vendedor
        Intent intent = getIntent();
        if (intent != null) {
            isVendorRegister = intent.getBooleanExtra("is_vendor", false);
        }

        initFirebase();
        initViews();
    }

    private void initFirebase() {
        FirebaseApp app = FirebaseApp.initializeApp(this);
        if (app == null && FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void initViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBack = findViewById(R.id.buttonBack);
        textViewRegisterTitle = findViewById(R.id.textViewRegisterTitle);
        progressBar = findViewById(R.id.progressBar);

        buttonRegister.setOnClickListener(v -> registerUser());
        buttonBack.setOnClickListener(v -> finish());
        
        // Atualiza o título baseado no tipo de registro
        updateUIForUserType();
    }
    
    private void updateUIForUserType() {
        if (isVendorRegister) {
            textViewRegisterTitle.setText("Criar Conta - Vendedor");
        } else {
            textViewRegisterTitle.setText("Criar Conta - Cliente");
        }
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError(getString(R.string.error_name_required));
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.error_email_required));
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.error_password_required));
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError(getString(R.string.error_password_min_length));
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.error_passwords_do_not_match));
            editTextConfirmPassword.requestFocus();
            return;
        }

        showLoading(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Cria o perfil do usuário no banco de dados
                                createUserProfile(user, name, phone);
                            }
                        } else {
                            showLoading(false);
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : getString(R.string.error_registration_failed);
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void createUserProfile(FirebaseUser firebaseUser, String name, String phone) {
        // Atualiza o displayName no FirebaseAuth
        com.google.firebase.auth.UserProfileChangeRequest profileUpdates = 
            new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        
        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(profileTask -> {
                    // Independente do resultado, salva no banco de dados
                    saveUserToDatabase(firebaseUser, name, phone);
                });
    }
    
    private void saveUserToDatabase(FirebaseUser firebaseUser, String name, String phone) {
        // Define o role baseado no tipo de registro
        String role = isVendorRegister ? "vendor" : "customer";
        
        User user = new User(
                firebaseUser.getUid(),
                name,
                firebaseUser.getEmail(),
                phone.isEmpty() ? null : phone,
                role
        );
        user.setUpdatedAt(System.currentTimeMillis());

        databaseReference.child("users").child(firebaseUser.getUid())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            String successMessage = isVendorRegister ? 
                                "Cadastro de vendedor realizado com sucesso!" : 
                                getString(R.string.message_registration_successful);
                            Toast.makeText(RegisterActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                            
                            // Redireciona para a activity apropriada
                            if (isVendorRegister) {
                                openVendorHomeActivity();
                            } else {
                                openHomeActivity();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openVendorHomeActivity() {
        Intent intent = new Intent(this, VendorHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonRegister.setEnabled(!show);
        buttonBack.setEnabled(!show);
        editTextName.setEnabled(!show);
        editTextEmail.setEnabled(!show);
        editTextPhone.setEnabled(!show);
        editTextPassword.setEnabled(!show);
        editTextConfirmPassword.setEnabled(!show);
    }
}
