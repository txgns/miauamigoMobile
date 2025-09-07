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
    private View viewBackgroundShape;
    private FirebaseAuthService authService;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new FirebaseAuthService(this);
        // Default to 'client' if no extra is passed
        userType = getIntent().getStringExtra("USER_TYPE");
        if (userType == null) {
            userType = "client";
        }

        initializeViews();
        setupStyling();
        setupClickListeners();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewLoginSubtitle = findViewById(R.id.textViewLoginSubtitle);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);
        viewBackgroundShape = findViewById(R.id.viewBackgroundShape);
    }

    private void setupStyling() {
        if ("client".equals(userType)) {
            buttonLogin.setBackground(ContextCompat.getDrawable(this, R.drawable.button_client_background));
            viewBackgroundShape.setBackground(ContextCompat.getDrawable(this, R.drawable.client_background_shape));
            textViewLoginSubtitle.setText("Como Cliente");
        } else { // vendor
            buttonLogin.setBackground(ContextCompat.getDrawable(this, R.drawable.button_vendor_background));
            viewBackgroundShape.setBackground(ContextCompat.getDrawable(this, R.drawable.vendor_background_shape));
            textViewLoginSubtitle.setText("Como Vendedor");
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
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
