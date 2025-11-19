package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ProductAdapter;
import com.miaumigo.app.models.Product;
import com.miaumigo.app.models.User;
import com.miaumigo.app.utils.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoreActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private RecyclerView recyclerViewProducts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private ImageView imageViewStoreLogo;
    private TextView textViewStoreName;
    private TextView textViewStoreDescription;

    private ProductAdapter productAdapter;
    private List<Product> productList;
    private String vendorId;
    private String vendorName;
    private DatabaseReference productsReference;
    private ValueEventListener productsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        vendorId = getIntent().getStringExtra("vendor_id");
        vendorName = getIntent().getStringExtra("vendor_name");

        if (vendorId == null || vendorId.isEmpty()) {
            Toast.makeText(this, "Loja não encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadStoreInfo();
        loadProducts();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(vendorName != null ? vendorName : "Loja");
        }

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        imageViewStoreLogo = findViewById(R.id.imageViewStoreLogo);
        textViewStoreName = findViewById(R.id.textViewStoreName);
        textViewStoreDescription = findViewById(R.id.textViewStoreDescription);

        productList = new ArrayList<>();

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(vendorName != null ? vendorName : "Loja");
        }
    }

    private void setupRecyclerView() {
        int spanCount = calculateSpanCount();
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        productAdapter = new ProductAdapter(productList, this);
        recyclerViewProducts.setLayoutManager(layoutManager);
        
        try {
            int spacing = getResources().getDimensionPixelSize(R.dimen.product_grid_spacing);
            recyclerViewProducts.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        } catch (Exception e) {
            int spacing = (int) (8 * getResources().getDisplayMetrics().density);
            recyclerViewProducts.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        }
        
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private int calculateSpanCount() {
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        float screenWidthDp = metrics.widthPixels / metrics.density;
        int span = (int) Math.floor(screenWidthDp / 160f);
        return Math.max(2, Math.min(span, 4));
    }

    private void loadStoreInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users").child(vendorId);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        if (textViewStoreName != null) {
                            String name = user.getName();
                            if (name != null && !name.isEmpty()) {
                                textViewStoreName.setText(name);
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setTitle(name);
                                }
                            }
                        }
                        
                        if (imageViewStoreLogo != null && user.getAvatarUrl() != null && 
                            !user.getAvatarUrl().isEmpty()) {
                            Glide.with(StoreActivity.this)
                                .load(user.getAvatarUrl())
                                .placeholder(R.drawable.ic_product_placeholder)
                                .circleCrop()
                                .into(imageViewStoreLogo);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ignora erro silenciosamente
            }
        });
    }

    private void loadProducts() {
        showLoading(true);
        
        if (productsReference == null) {
            productsReference = FirebaseDatabase.getInstance().getReference("products");
        }
        
        if (productsListener != null) {
            productsReference.removeEventListener(productsListener);
        }

        Query productsQuery = productsReference
            .orderByChild("vendorId")
            .equalTo(vendorId);

        productsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null && product.isVisibleToCustomers()) {
                        productList.add(product);
                    }
                }
                
                // Ordena por mais recente
                Collections.sort(productList, (p1, p2) -> 
                    Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                
                productAdapter.notifyDataSetChanged();
                updateEmptyState();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(StoreActivity.this, 
                    "Erro ao carregar produtos: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };

        productsQuery.addValueEventListener(productsListener);
    }

    private void updateEmptyState() {
        boolean isEmpty = productList.isEmpty();
        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewProducts != null) {
            recyclerViewProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
        }
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_price", product.getPrice());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_image", product.getImageUrl());
        intent.putExtra("vendor_id", product.getVendorId());
        intent.putExtra("vendor_name", product.getVendorName());
        startActivity(intent);
    }

    @Override
    public void onAddToCart(Product product) {
        if (!product.isInStock()) {
            Toast.makeText(this, R.string.product_out_of_stock, Toast.LENGTH_SHORT).show();
            return;
        }

        com.miaumigo.app.models.CartItem cartItem = new com.miaumigo.app.models.CartItem(
            product.getId(),
            product.getName(),
            product.getPrice(),
            1,
            product.getImageUrl() != null ? product.getImageUrl() : ""
        );
        com.miaumigo.app.utils.CartManager.getInstance(this).addToCart(cartItem);
        Toast.makeText(this, R.string.message_product_added_to_cart, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (productsReference != null && productsListener != null) {
            productsReference.removeEventListener(productsListener);
        }
    }
}

