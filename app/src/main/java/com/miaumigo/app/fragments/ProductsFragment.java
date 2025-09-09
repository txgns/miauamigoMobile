package com.miaumigo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ProductAdapter;
import com.miaumigo.app.models.Product;
import com.miaumigo.app.services.FirebaseDatabaseService;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerViewProducts;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private ProductAdapter productAdapter;
    private FirebaseDatabaseService databaseService;
    private List<Product> products;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadProducts();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        
        databaseService = new FirebaseDatabaseService(getContext());
        products = new ArrayList<>();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(products, this);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void loadProducts() {
        showLoading(true);
        
        try {
            databaseService.getProducts(new FirebaseDatabaseService.ListCallback<Product>() {
                @Override
                public void onSuccess(List<Product> productList) {
                    showLoading(false);
                    products.clear();
                    if (productList != null) {
                        products.addAll(productList);
                    }
                    productAdapter.notifyDataSetChanged();
                    
                    if (products.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    textViewEmpty.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            showLoading(false);
            textViewEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to product detail
        Intent intent = new Intent(getContext(), com.miaumigo.app.ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}

