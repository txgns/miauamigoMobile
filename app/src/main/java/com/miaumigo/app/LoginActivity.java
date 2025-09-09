package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.miaumigo.app.services.FirebaseAuthService;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewLoginSubtitle, textViewRegisterLink;
    private FirebaseAuthService authService;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            authService = new FirebaseAuthService(this);
            // Default to 'client' if no extra is passed
            userType = getIntent().getStringExtra("USER_TYPE");
            if (userType == null) {
                userType = "client";
            }

            initializeViews();
            setupStyling();
            setupClickListeners();
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            // If there's an error, show a simple message and finish
            Toast.makeText(this, "Erro ao inicializar o app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            buttonLogin = findViewById(R.id.buttonLogin);
            textViewLoginSubtitle = findViewById(R.id.textViewLoginSubtitle);
            textViewRegisterLink = findViewById(R.id.textViewRegisterLink);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inicializar views: " + e.getMessage());
        }
    }

    private void setupStyling() {
        try {
            if ("client".equals(userType)) {
                buttonLogin.setBackground(ContextCompat.getDrawable(this, R.drawable.button_client_background));
                textViewLoginSubtitle.setText("Como Cliente");
            } else { // vendor
                buttonLogin.setBackground(ContextCompat.getDrawable(this, R.drawable.button_vendor_background));
                textViewLoginSubtitle.setText("Como Vendedor");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao configurar estilo: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(v -> loginUser());

        textViewRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtra("USER_TYPE", userType);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "E-mail e senha são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            authService.loginUser(email, password, new FirebaseAuthService.AuthCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("USER_TYPE", userType);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(LoginActivity.this, "Erro ao fazer login. Tente novamente.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Erro de conexão. Verifique sua internet.", Toast.LENGTH_LONG).show();
        }
    }
}
