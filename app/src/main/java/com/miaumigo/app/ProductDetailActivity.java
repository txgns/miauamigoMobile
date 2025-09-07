package com.miaumigo.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.models.Product;
import com.miaumigo.app.services.FirebaseAuthService;
import com.miaumigo.app.services.FirebaseDatabaseService;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewProduct;
    private TextView textViewName;
    private TextView textViewPrice;
    private TextView textViewDescription;
    private TextView textViewRating;
    private TextView textViewStock;
    private Button buttonAddToCart;
    private FloatingActionButton fabBack;
    private ProgressBar progressBar;
    
    private FirebaseDatabaseService databaseService;
    private FirebaseAuthService authService;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initializeViews();
        setupClickListeners();
        loadProductDetails();
    }

    private void initializeViews() {
        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewName = findViewById(R.id.textViewName);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewRating = findViewById(R.id.textViewRating);
        textViewStock = findViewById(R.id.textViewStock);
        buttonAddToCart = findViewById(R.id.buttonAddToCart);
        fabBack = findViewById(R.id.fabBack);
        progressBar = findViewById(R.id.progressBar);
        
        databaseService = new FirebaseDatabaseService(this);
        authService = new FirebaseAuthService(this);
    }

    private void setupClickListeners() {
        fabBack.setOnClickListener(v -> finish());
        
        buttonAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProductDetails() {
        String productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            Toast.makeText(this, "Produto não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        databaseService.getProduct(productId, new FirebaseDatabaseService.DataCallback<Product>() {
            @Override
            public void onSuccess(Product productData) {
                showLoading(false);
                product = productData;
                displayProductDetails();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayProductDetails() {
        if (product == null) return;

        textViewName.setText(product.getName());
        textViewPrice.setText(product.getFormattedPrice());
        textViewDescription.setText(product.getDescription());
        textViewRating.setText(String.format("%.1f ⭐ (%d avaliações)", product.getRating(), product.getReviewCount()));

        // Stock status
        if (product.isInStock()) {
            textViewStock.setText("Em estoque");
            textViewStock.setTextColor(getColor(R.color.success));
            buttonAddToCart.setEnabled(true);
        } else {
            textViewStock.setText("Fora de estoque");
            textViewStock.setTextColor(getColor(R.color.error));
            buttonAddToCart.setEnabled(false);
        }

        // Load product image
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(this)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into(imageViewProduct);
        } else {
            imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
        }
    }

    private void addToCart() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Faça login para adicionar produtos ao carrinho", Toast.LENGTH_SHORT).show();
            return;
        }

        if (product == null || !product.isInStock()) {
            Toast.makeText(this, "Produto não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        String productImage = (product.getImages() != null && !product.getImages().isEmpty()) 
            ? product.getImages().get(0) : null;
        
        CartItem cartItem = new CartItem(
            product.getId(),
            product.getName(),
            productImage,
            product.getPrice(),
            1
        );

        databaseService.addToCart(currentUser.getUid(), cartItem, new FirebaseDatabaseService.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(ProductDetailActivity.this, getString(R.string.add_to_cart_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        imageViewProduct.setVisibility(show ? View.GONE : View.VISIBLE);
        textViewName.setVisibility(show ? View.GONE : View.VISIBLE);
        textViewPrice.setVisibility(show ? View.GONE : View.VISIBLE);
        textViewDescription.setVisibility(show ? View.GONE : View.VISIBLE);
        textViewRating.setVisibility(show ? View.GONE : View.VISIBLE);
        textViewStock.setVisibility(show ? View.GONE : View.VISIBLE);
        buttonAddToCart.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}

