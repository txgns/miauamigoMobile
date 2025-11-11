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

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private TextView textViewLoginSubtitle;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private boolean isVendorLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Verifica se é login de vendedor
        Intent intent = getIntent();
        if (intent != null) {
            isVendorLogin = intent.getBooleanExtra("is_vendor", false);
        }

        initFirebase();
        initViews();
        updateUIForUserType();
    }

    private void initFirebase() {
        try {
            // Inicialização simples
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference();
            
            Toast.makeText(this, "Firebase inicializado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Continuar mesmo com erro
        }
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewLoginSubtitle = findViewById(R.id.textViewLoginSubtitle);
        progressBar = findViewById(R.id.progressBar);

        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(v -> openRegisterActivity());
    }
    
    private void updateUIForUserType() {
        if (isVendorLogin) {
            textViewLoginSubtitle.setText("Como Vendedor");
            textViewLoginSubtitle.setTextColor(getColor(R.color.primary_vendor));
        } else {
            textViewLoginSubtitle.setText("Como Cliente");
            textViewLoginSubtitle.setTextColor(getColor(R.color.primary_client));
        }
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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

        showLoading(true);

        // Teste de conexão primeiro
        testFirebaseConnection(email, password);
    }
    
    private void testFirebaseConnection(String email, String password) {
        // Teste simples de conectividade
        databaseReference.child("test").setValue("connection_test")
            .addOnSuccessListener(aVoid -> {
                // Conexão OK, prosseguir com login
                performLogin(email, password);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Erro de conexão Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void performLogin(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Verifica se o usuário já existe no banco de dados
                                checkUserExists(user);
                            }
                        } else {
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : getString(R.string.error_login_failed);
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkUserExists(FirebaseUser firebaseUser) {
        databaseReference.child("users").child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Usuário não existe no banco, cria um novo registro
                            createUserInDatabase(firebaseUser);
                        } else {
                            // Usuário existe, verifica o role
                            User userData = snapshot.getValue(User.class);
                            if (userData != null) {
                                // Atualiza o displayName se necessário
                                if (userData.getName() != null && !userData.getName().isEmpty()) {
                                    updateFirebaseAuthProfile(firebaseUser, userData.getName());
                                }
                                
                                // Verifica se o tipo de usuário corresponde ao tipo de login
                                boolean userIsVendor = "vendor".equals(userData.getRole());
                                if (isVendorLogin && !userIsVendor) {
                                    Toast.makeText(LoginActivity.this, 
                                        "Esta conta não é de vendedor. Use o login de cliente.", 
                                        Toast.LENGTH_LONG).show();
                                    firebaseAuth.signOut();
                                    return;
                                } else if (!isVendorLogin && userIsVendor) {
                                    Toast.makeText(LoginActivity.this, 
                                        "Esta conta é de vendedor. Use o login de vendedor.", 
                                        Toast.LENGTH_LONG).show();
                                    firebaseAuth.signOut();
                                    return;
                                }
                                
                                // Redireciona para a activity apropriada
                                if (userIsVendor) {
                                    openVendorHomeActivity();
                                } else {
                                    openHomeActivity();
                                }
                            } else {
                                createUserInDatabase(firebaseUser);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void updateFirebaseAuthProfile(FirebaseUser firebaseUser, String name) {
        if (firebaseUser.getDisplayName() == null || firebaseUser.getDisplayName().isEmpty()) {
            com.google.firebase.auth.UserProfileChangeRequest profileUpdates = 
                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            
            firebaseUser.updateProfile(profileUpdates);
        }
    }

    private void createUserInDatabase(FirebaseUser firebaseUser) {
        String displayName = firebaseUser.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = firebaseUser.getEmail() != null ? 
                firebaseUser.getEmail().split("@")[0] : "Usuário";
        }
        
        // Define o role baseado no tipo de login
        String role = isVendorLogin ? "vendor" : "customer";
        
        User user = new User(
                firebaseUser.getUid(),
                displayName,
                firebaseUser.getEmail(),
                firebaseUser.getPhoneNumber(),
                role
        );
        user.setUpdatedAt(System.currentTimeMillis());

        databaseReference.child("users").child(firebaseUser.getUid())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Redireciona para a activity apropriada
                            if (isVendorLogin) {
                                openVendorHomeActivity();
                            } else {
                                openHomeActivity();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("is_vendor", isVendorLogin);
        startActivity(intent);
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
        buttonLogin.setEnabled(!show);
        textViewRegister.setEnabled(!show);
    }
}
