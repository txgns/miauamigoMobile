package com.miaumigo.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView welcomeText;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_home);

        welcomeText = findViewById(R.id.welcome_text);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    welcomeText.setText("Bem-vindo ao MiauMigo!");
                    getSupportActionBar().setTitle(R.string.title_home);
                    return true;
                } else if (itemId == R.id.navigation_dashboard) {
                    welcomeText.setText("Painel de Controle");
                    getSupportActionBar().setTitle(R.string.title_dashboard);
                    return true;
                } else if (itemId == R.id.navigation_notifications) {
                    welcomeText.setText("Notificações");
                    getSupportActionBar().setTitle(R.string.title_notifications);
                    return true;
                }
                return false;
            }
        });
    }
}
