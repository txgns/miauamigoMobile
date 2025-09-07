package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.fragments.HomeFragment;
import com.miaumigo.app.fragments.ProductsFragment;
import com.miaumigo.app.fragments.CartFragment;
import com.miaumigo.app.fragments.OrdersFragment;
import com.miaumigo.app.fragments.ProfileFragment;
import com.miaumigo.app.services.FirebaseAuthService;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuthService authService;
    private TextView textViewWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        authService = new FirebaseAuthService(this);
        initializeViews();
        setupBottomNavigation();
        loadUserInfo();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        textViewWelcome = findViewById(R.id.textViewWelcome);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_products) {
                    selectedFragment = new ProductsFragment();
                } else if (itemId == R.id.nav_cart) {
                    selectedFragment = new CartFragment();
                } else if (itemId == R.id.nav_orders) {
                    selectedFragment = new OrdersFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            textViewWelcome.setText("Olá, " + currentUser.getDisplayName() + "!");
        }
    }

    @Override
    public void onBackPressed() {
        // Exit app when back is pressed from home
        finishAffinity();
    }
}

