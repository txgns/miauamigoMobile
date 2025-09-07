package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private Button buttonClient, buttonVendor;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        buttonClient = findViewById(R.id.buttonClient);
        buttonVendor = findViewById(R.id.buttonVendor);
        textViewLogin = findViewById(R.id.textViewLogin);
    }

    private void setupClickListeners() {
        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Passar um extra para a RegisterActivity saber que é um cliente
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("USER_TYPE", "client");
                startActivity(intent);
            }
        });

        buttonVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Passar um extra para a RegisterActivity saber que é um vendedor
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("USER_TYPE", "vendor");
                startActivity(intent);
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
