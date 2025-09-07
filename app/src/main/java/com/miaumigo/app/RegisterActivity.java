package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.miaumigo.app.services.FirebaseAuthService;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPhone, editTextPassword;
    private Button buttonRegister, buttonBack;
    private View viewBackgroundShape;
    private TextView textViewRegisterTitle;
    private FirebaseAuthService authService;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new FirebaseAuthService(this);
        userType = getIntent().getStringExtra("USER_TYPE");

        initializeViews();
        setupStyling();
        setupClickListeners();
    }

    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBack = findViewById(R.id.buttonBack);
        viewBackgroundShape = findViewById(R.id.viewBackgroundShape);
        textViewRegisterTitle = findViewById(R.id.textViewRegisterTitle);
    }

    private void setupStyling() {
        if ("client".equals(userType)) {
            buttonRegister.setBackground(ContextCompat.getDrawable(this, R.drawable.button_client_background));
            viewBackgroundShape.setBackground(ContextCompat.getDrawable(this, R.drawable.client_background_shape));
            textViewRegisterTitle.setText("Criar Conta Cliente");
        } else { // vendor
            buttonRegister.setBackground(ContextCompat.getDrawable(this, R.drawable.button_vendor_background));
            viewBackgroundShape.setBackground(ContextCompat.getDrawable(this, R.drawable.vendor_background_shape));
            textViewRegisterTitle.setText("Criar Conta Vendedor");
        }
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> registerUser());
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("USER_TYPE", userType);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("A senha deve ter pelo menos 6 caracteres");
            editTextPassword.requestFocus();
            return;
        }

        authService.registerUser(email, password, name, phone, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(RegisterActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
