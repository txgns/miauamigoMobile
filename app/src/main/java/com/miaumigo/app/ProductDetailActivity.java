package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.models.User;
import com.miaumigo.app.utils.CartManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewProductDescription;
    private TextView textViewProductPrice;
    private MaterialButton buttonAddToCart;
    private ProgressBar progressBar;
    private MaterialCardView cardStore;
    private ImageView imageViewStoreLogo;
    private TextView textViewStoreName;
    private MaterialButton buttonViewStore;

    private FirebaseAuth firebaseAuth;
    private String productId;
    private String productName;
    private String productDescription;
    private String productPrice;
    private String productImageUrl;
    private String vendorId;
    private String vendorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initFirebase();
        initViews();
        loadProductData();
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.product_details);
        }

        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductDescription = findViewById(R.id.textViewProductDescription);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        buttonAddToCart = findViewById(R.id.buttonAddToCart);
        progressBar = findViewById(R.id.progressBar);
        cardStore = findViewById(R.id.cardStore);
        imageViewStoreLogo = findViewById(R.id.imageViewStoreLogo);
        textViewStoreName = findViewById(R.id.textViewStoreName);
        buttonViewStore = findViewById(R.id.buttonViewStore);

        buttonAddToCart.setOnClickListener(v -> addToCart());
        buttonViewStore.setOnClickListener(v -> openStorePage());
    }

    private void loadProductData() {
        // Recupera os dados do produto passados via Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            productId = extras.getString("product_id");
            productName = extras.getString("product_name");
            productDescription = extras.getString("product_description");
            
            // Recebe o preço como double e formata
            double price = extras.getDouble("product_price", 0.0);
            productPrice = String.format("%.2f", price);
            
            productImageUrl = extras.getString("product_image");
            vendorId = extras.getString("vendor_id");
            vendorName = extras.getString("vendor_name");

            displayProductData();
            loadStoreInfo();
        } else {
            Toast.makeText(this, R.string.error_product_not_found, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void displayProductData() {
        if (productName != null) {
            textViewProductName.setText(productName);
        }
        
        if (productDescription != null) {
            textViewProductDescription.setText(productDescription);
        }
        
        if (productPrice != null) {
            // Formata o preço para exibição
            String formattedPrice = "R$ " + productPrice.replace(".", ",");
            textViewProductPrice.setText(formattedPrice);
        }
        
        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(productImageUrl)
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into(imageViewProduct);
        } else {
            imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
        }
    }

    private void addToCart() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_user_not_authenticated, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading(true);
        
        try {
            // Adicionar produto ao carrinho usando CartManager
            CartManager cartManager = CartManager.getInstance(this);
            
            // Converte o preço de forma segura
            double price = 0.0;
            if (productPrice != null && !productPrice.isEmpty()) {
                // Remove formatação brasileira (R$, pontos de milhar e substitui vírgula por ponto)
                String cleanPrice = productPrice
                    .replace("R$", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();
                price = Double.parseDouble(cleanPrice);
            }
            
            CartItem cartItem = new CartItem(
                productId,
                productName,
                price,
                1,
                productImageUrl != null ? productImageUrl : ""
            );
            
            cartManager.addToCart(cartItem);
            
            buttonAddToCart.postDelayed(() -> {
                showLoading(false);
                Toast.makeText(ProductDetailActivity.this, R.string.message_product_added_to_cart, Toast.LENGTH_SHORT).show();
            }, 500);
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Erro ao adicionar produto ao carrinho: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadStoreInfo() {
        if (vendorId == null || vendorId.isEmpty()) {
            if (cardStore != null) {
                cardStore.setVisibility(View.GONE);
            }
            return;
        }

        if (textViewStoreName != null && vendorName != null && !vendorName.isEmpty()) {
            textViewStoreName.setText(vendorName);
        } else {
            // Busca nome da loja do Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(vendorId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && textViewStoreName != null) {
                            String name = user.getName();
                            if (name != null && !name.isEmpty()) {
                                textViewStoreName.setText(name);
                            }
                        }
                        if (user != null && user.getAvatarUrl() != null && 
                            !user.getAvatarUrl().isEmpty() && imageViewStoreLogo != null) {
                            Glide.with(ProductDetailActivity.this)
                                .load(user.getAvatarUrl())
                                .placeholder(R.drawable.ic_product_placeholder)
                                .into(imageViewStoreLogo);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Ignora erro silenciosamente
                }
            });
        }
    }

    private void openStorePage() {
        if (vendorId == null || vendorId.isEmpty()) {
            Toast.makeText(this, "Informações da loja não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, StoreActivity.class);
        intent.putExtra("vendor_id", vendorId);
        intent.putExtra("vendor_name", vendorName);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonAddToCart.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
