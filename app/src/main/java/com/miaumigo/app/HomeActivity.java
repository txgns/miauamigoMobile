package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.fragments.CartFragment;
import com.miaumigo.app.fragments.HomeFragment;
import com.miaumigo.app.fragments.OrdersFragment;
import com.miaumigo.app.fragments.ProductsFragment;
import com.miaumigo.app.fragments.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initFirebase();
        initViews();
        checkUserAuthentication();
        setupBottomNavigation();
        loadHomeFragment();
    }

    private void initFirebase() {
        FirebaseApp app = FirebaseApp.initializeApp(this);
        if (app == null && FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                loadHomeFragment();
                return true;
            } else if (itemId == R.id.navigation_products) {
                loadProductsFragment();
                return true;
            } else if (itemId == R.id.navigation_cart) {
                loadCartFragment();
                return true;
            } else if (itemId == R.id.navigation_orders) {
                loadOrdersFragment();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                loadProfileFragment();
                return true;
            }
            
            return false;
        });
    }

    private void loadHomeFragment() {
        Fragment fragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void loadProductsFragment() {
        Fragment fragment = new ProductsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void loadCartFragment() {
        Fragment fragment = new CartFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void loadOrdersFragment() {
        Fragment fragment = new OrdersFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void loadProfileFragment() {
        Fragment fragment = new ProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Usuário não está logado, volta para a tela principal
            openMainActivity();
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        showLoading(true);
        firebaseAuth.signOut();
        showLoading(false);
        openMainActivity();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verifica se o usuário ainda está logado
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            openMainActivity();
        }
    }
}
