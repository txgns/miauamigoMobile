package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.services.FirebaseAuthService;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuthService authService;
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authService = new FirebaseAuthService(this);

        // Show splash screen for 2 seconds then redirect
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserAuthentication();
            }
        }, SPLASH_DELAY);
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = authService.getCurrentUser();
        
        if (currentUser != null) {
            // User is logged in, go to Home
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            // User is not logged in, go to Login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        
        finish(); // Close splash screen
    }
}

