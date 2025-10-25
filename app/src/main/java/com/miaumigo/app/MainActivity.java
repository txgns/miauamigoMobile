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
    private Button buttonLogin;
    private Button buttonRegister;

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
        buttonLogin = findViewById(R.id.buttonClient);
        buttonRegister = findViewById(R.id.buttonVendor);

        buttonLogin.setOnClickListener(v -> openLoginActivity());
        buttonRegister.setOnClickListener(v -> openRegisterActivity());
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

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!show);
        buttonRegister.setEnabled(!show);
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
