package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private Button buttonClient;
    private Button buttonVendor;
    private android.widget.TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFirebase();
        initViews();
        checkUserAuthentication();
    }

    private void initFirebase() {
        try {
            // Inicialização mais simples e robusta
            firebaseAuth = FirebaseAuth.getInstance();
            
            // Executar diagnóstico completo
            FirebaseConnectionTester.runFullDiagnostic(this);
            
        } catch (Exception e) {
            Toast.makeText(this, "Erro Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Não fechar o app, apenas mostrar erro
        }
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        buttonClient = findViewById(R.id.buttonClient);
        buttonVendor = findViewById(R.id.buttonVendor);
        textViewLogin = findViewById(R.id.textViewLogin);

        // Botões para CRIAR CONTA (Registro)
        buttonClient.setOnClickListener(v -> openClientRegisterActivity());
        buttonVendor.setOnClickListener(v -> openVendorRegisterActivity());
        
        // Link para fazer LOGIN
        textViewLogin.setOnClickListener(v -> showLoginOptions());
    }

    private void checkUserAuthentication() {
        showLoading(true);
        
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Usuário já está logado, vai para a tela principal
            openHomeActivity();
        } else {
            // Usuário não está logado, mostra as opções de login/registro
            showLoading(false);
        }
    }

    // Métodos para CRIAR CONTA (Registro)
    private void openClientRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("is_vendor", false);
        startActivity(intent);
    }

    private void openVendorRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("is_vendor", true);
        startActivity(intent);
    }
    
    // Métodos para fazer LOGIN (para quem já tem conta)
    private void showLoginOptions() {
        // Criar diálogo para escolher tipo de login
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Como você deseja entrar?");
        builder.setMessage("Escolha o tipo de conta:");
        
        builder.setPositiveButton("Cliente", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("is_vendor", false);
            startActivity(intent);
        });
        
        builder.setNegativeButton("Vendedor", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("is_vendor", true);
            startActivity(intent);
        });
        
        builder.setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }

    private void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonClient.setEnabled(!show);
        buttonVendor.setEnabled(!show);
        textViewLogin.setEnabled(!show);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verifica se o usuário está logado quando a atividade é iniciada
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            openHomeActivity();
        }
    }
}
