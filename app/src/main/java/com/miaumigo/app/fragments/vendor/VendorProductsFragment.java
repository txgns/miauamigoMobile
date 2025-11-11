package com.miaumigo.app.fragments.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.ProductManagementActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ProductAdapter;
import com.miaumigo.app.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VendorProductsFragment extends Fragment {

    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddProduct;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_products, container, false);
        
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        setupRecyclerView();
        setupFab();
        loadProducts();
        
        return view;
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(productList, product -> {
            // Abre tela de edição do produto
            Intent intent = new Intent(getContext(), ProductManagementActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("edit_mode", true);
            startActivity(intent);
        });
        
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupFab() {
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProductManagementActivity.class);
            intent.putExtra("edit_mode", false);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        
        com.google.firebase.database.Query productsQuery = FirebaseDatabase.getInstance().getReference("products")
            .orderByChild("vendorId").equalTo(currentUser.getUid());
        
        productsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                
                // Ordena por data de criação (mais recente primeiro)
                Collections.sort(productList, (p1, p2) -> {
                    long time1 = p1.getCreatedAt() > 0 ? p1.getCreatedAt() : 0;
                    long time2 = p2.getCreatedAt() > 0 ? p2.getCreatedAt() : 0;
                    return Long.compare(time2, time1);
                });
                
                productAdapter.notifyDataSetChanged();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(getContext(), "Erro ao carregar produtos: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Query listeners are automatically removed when fragment is destroyed
    }
}

