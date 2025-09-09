package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button buttonClient, buttonVendor;
    private TextView textViewLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            mAuth = FirebaseAuth.getInstance();

            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is already logged in, redirect to HomeActivity
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                // TODO: You might need to pass the USER_TYPE here if HomeActivity depends on it.
                // You would typically fetch this from your database based on the currentUser.getUid()
                startActivity(intent);
                finish(); // Finish MainActivity so the user can't go back to it
                return; // Return to prevent the rest of the onCreate from running
            }

            initializeViews();
            setupClickListeners();
        } catch (Exception e) {
            // If there's an error, show a simple message and finish
            Toast.makeText(this, "Erro ao inicializar o app", Toast.LENGTH_LONG).show();
            finish();
        }
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
                // Start LoginActivity with a hint for client
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("USER_TYPE", "client");
                startActivity(intent);
            }
        });

        buttonVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity with a hint for vendor
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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
