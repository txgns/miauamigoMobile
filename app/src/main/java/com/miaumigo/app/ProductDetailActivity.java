package com.miaumigo.app;

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
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.utils.CartManager;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewProductDescription;
    private TextView textViewProductPrice;
    private Button buttonAddToCart;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private String productId;
    private String productName;
    private String productDescription;
    private String productPrice;
    private String productImageUrl;

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

        buttonAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProductData() {
        // Recupera os dados do produto passados via Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            productId = extras.getString("product_id");
            productName = extras.getString("product_name");
            productDescription = extras.getString("product_description");
            productPrice = extras.getString("product_price");
            productImageUrl = extras.getString("product_image");

            displayProductData();
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
            textViewProductPrice.setText(productPrice);
        }
        
        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(productImageUrl)
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into(imageViewProduct);
        }
    }

    private void addToCart() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_user_not_authenticated, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading(true);
        
        // Adicionar produto ao carrinho usando CartManager
        CartManager cartManager = CartManager.getInstance(this);
        CartItem cartItem = new CartItem(
            productId,
            productName,
            Double.parseDouble(productPrice.replace("R$ ", "").replace(",", ".")),
            1,
            productImageUrl
        );
        
        cartManager.addToCart(cartItem);
        
        buttonAddToCart.postDelayed(() -> {
            showLoading(false);
            Toast.makeText(ProductDetailActivity.this, R.string.message_product_added_to_cart, Toast.LENGTH_SHORT).show();
        }, 1000);
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
