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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private SwipeRefreshLayout swipeRefreshLayout;
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
        try {
            View view = inflater.inflate(R.layout.fragment_vendor_products, container, false);
            
            recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
            progressBar = view.findViewById(R.id.progressBar);
            fabAddProduct = view.findViewById(R.id.fabAddProduct);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
            
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
            }
            
            setupRecyclerView();
            setupFab();
            loadProducts();
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("VendorProductsFragment", "Erro ao criar view", e);
            e.printStackTrace();
            return new View(getContext());
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(productList, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onProductClick(Product product) {
                // Abre tela de edição do produto
                Intent intent = new Intent(getContext(), ProductManagementActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("edit_mode", true);
                startActivity(intent);
            }

            @Override
            public void onAddToCart(Product product) {
                // Não aplicável para vendedores
            }
        }, true); // true = esconde botão de adicionar ao carrinho
        
        // Usa GridLayoutManager para melhor visualização
        int spanCount = calculateSpanCount();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recyclerViewProducts.setLayoutManager(layoutManager);
        
        // Adiciona espaçamento entre cards
        try {
            int spacing = getResources().getDimensionPixelSize(com.miaumigo.app.R.dimen.product_grid_spacing);
            recyclerViewProducts.addItemDecoration(new com.miaumigo.app.utils.GridSpacingItemDecoration(spanCount, spacing, true));
        } catch (Exception e) {
            int spacing = (int) (8 * getResources().getDisplayMetrics().density);
            recyclerViewProducts.addItemDecoration(new com.miaumigo.app.utils.GridSpacingItemDecoration(spanCount, spacing, true));
        }
        
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private int calculateSpanCount() {
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        float screenWidthDp = metrics.widthPixels / metrics.density;
        int span = (int) Math.floor(screenWidthDp / 160f);
        return Math.max(2, Math.min(span, 4));
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
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
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
        if (swipeRefreshLayout != null && !show) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    
    }
}

