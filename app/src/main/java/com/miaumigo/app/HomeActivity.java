package com.miaumigo.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
    private ExtendedFloatingActionButton chatbotFab;
    private FrameLayout chatbotAnchor;
    private View fragmentContainerHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initFirebase();
        initViews();
        checkUserAuthentication();
        setupBottomNavigation();
        
        // Verificar se há um fragment específico para carregar
        String fragmentToLoad = getIntent().getStringExtra("fragment");
        if ("orders".equals(fragmentToLoad)) {
            loadOrdersFragment();
            bottomNavigationView.setSelectedItemId(R.id.navigation_orders);
        } else {
            loadHomeFragment();
        }
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
        chatbotFab = findViewById(R.id.fabChatbot);
        chatbotAnchor = findViewById(R.id.chatbotAnchor);
        fragmentContainerHost = findViewById(R.id.fragmentContainer);

        if (chatbotFab != null) {
            chatbotFab.shrink();
            chatbotFab.setOnClickListener(v -> openChatbotActivity());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                int insetBottom = insets.getInsets(android.view.WindowInsets.Type.systemBars()).bottom;
                adjustChatbotAnchor(insetBottom);
                return v.onApplyWindowInsets(insets);
            });
        } else {
            fragmentContainerHost.setOnApplyWindowInsetsListener((v, insets) -> {
                int insetBottom = insets.getSystemWindowInsetBottom();
                adjustChatbotAnchor(insetBottom);
                return v.onApplyWindowInsets(insets);
            });
        }
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
        try {
            Fragment fragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContent, fragment)
                    .commitNowAllowingStateLoss();
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erro ao carregar HomeFragment", e);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar página inicial", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductsFragment() {
        try {
            Fragment fragment = new ProductsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContent, fragment)
                    .commitNowAllowingStateLoss();
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erro ao carregar ProductsFragment", e);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar produtos", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void navigateToProducts() {
        loadProductsFragment();
        // Atualiza o item selecionado na navegação inferior
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_products);
        }
    }

    private void loadCartFragment() {
        try {
            Fragment fragment = new CartFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContent, fragment)
                    .commitNowAllowingStateLoss();
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erro ao carregar CartFragment", e);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar carrinho", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOrdersFragment() {
        try {
            Fragment fragment = new OrdersFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContent, fragment)
                    .commitNowAllowingStateLoss();
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erro ao carregar OrdersFragment", e);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileFragment() {
        try {
            Fragment fragment = new ProfileFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContent, fragment)
                    .commitNowAllowingStateLoss();
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erro ao carregar ProfileFragment", e);
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Usuário não está logado, volta para a tela principal
            openMainActivity();
            return;
        }
        
        // Verifica se o usuário é vendedor e redireciona
        com.google.firebase.database.DatabaseReference userRef = 
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users").child(currentUser.getUid());
        
        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    com.miaumigo.app.models.User user = snapshot.getValue(com.miaumigo.app.models.User.class);
                    if (user != null && "vendor".equals(user.getRole())) {
                        // É vendedor, redireciona para VendorHomeActivity
                        openVendorHomeActivity();
                    }
                    // Se for cliente, continua na HomeActivity
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                // Em caso de erro, continua na HomeActivity
            }
        });
    }

    private void openVendorHomeActivity() {
        Intent intent = new Intent(this, VendorHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    private void openChatbotActivity() {
        Intent intent = new Intent(this, ChatbotActivity.class);
        startActivity(intent);
    }

    private void adjustChatbotAnchor(int systemInsetBottom) {
        if (chatbotAnchor == null || bottomNavigationView == null) {
            return;
        }
        int bottomOffset = bottomNavigationView.getHeight() + systemInsetBottom;
        chatbotAnchor.setPadding(0, 0, 0, bottomOffset);
    }

    private void updateChatbotOffset() {
        int insetBottom = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.view.WindowInsets insets = chatbotAnchor.getRootWindowInsets();
            if (insets != null) {
                insetBottom = insets.getStableInsetBottom();
            }
        }
        adjustChatbotAnchor(insetBottom);
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

    @Override
    protected void onResume() {
        super.onResume();
        updateChatbotOffset();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateChatbotOffset();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateChatbotOffset();
    }
}
