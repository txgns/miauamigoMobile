package com.miaumigo.app;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miaumigo.app.fragments.CartFragment;
import com.miaumigo.app.fragments.HomeFragment;
import com.miaumigo.app.fragments.OrdersFragment;
import com.miaumigo.app.fragments.ProductsFragment;
import com.miaumigo.app.fragments.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String title = getString(R.string.app_name);

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    title = getString(R.string.title_home);
                } else if (itemId == R.id.nav_products) {
                    selectedFragment = new ProductsFragment();
                    title = getString(R.string.products);
                } else if (itemId == R.id.nav_cart) {
                    selectedFragment = new CartFragment();
                    title = getString(R.string.cart);
                } else if (itemId == R.id.nav_orders) {
                    selectedFragment = new OrdersFragment();
                    title = getString(R.string.orders);
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    title = getString(R.string.profile);
                }

                if (selectedFragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, selectedFragment);
                    transaction.commit();
                    getSupportActionBar().setTitle(title);
                }
                return true;
            }
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
}
