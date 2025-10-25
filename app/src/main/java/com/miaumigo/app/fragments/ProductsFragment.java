package com.miaumigo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.material.textfield.TextInputEditText;
import com.miaumigo.app.ProductDetailActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ProductAdapter;
import com.miaumigo.app.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerViewProducts;
    private TextInputEditText editTextSearch;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredProductList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadProducts();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
        
        productAdapter = new ProductAdapter(filteredProductList, this);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProducts() {
        showLoading(true);
        
        // Simular carregamento de produtos
        // Em uma implementação real, isso viria do Firebase ou API
        productList.clear();
        productList.add(new Product("1", "Ração Premium para Cães", "Ração de alta qualidade para cães adultos", 89.90, "https://example.com/racao.jpg", 4.5, true));
        productList.add(new Product("2", "Brinquedo para Gatos", "Brinquedo interativo para gatos", 25.50, "https://example.com/brinquedo.jpg", 4.2, true));
        productList.add(new Product("3", "Coleira Antipulgas", "Coleira repelente de pulgas e carrapatos", 45.00, "https://example.com/coleira.jpg", 4.7, true));
        productList.add(new Product("4", "Areia Sanitária", "Areia sanitária para gatos", 35.90, "https://example.com/areia.jpg", 4.0, true));
        productList.add(new Product("5", "Petisco para Cães", "Petisco natural para cães", 15.80, "https://example.com/petisco.jpg", 4.3, true));
        productList.add(new Product("6", "Bebedouro Automático", "Bebedouro com sensor automático", 120.00, "https://example.com/bebedouro.jpg", 4.6, true));
        
        filteredProductList.clear();
        filteredProductList.addAll(productList);
        productAdapter.notifyDataSetChanged();
        
        showLoading(false);
        updateEmptyState();
    }

    private void filterProducts(String query) {
        filteredProductList.clear();
        
        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredProductList.add(product);
                }
            }
        }
        
        productAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredProductList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewProducts.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_price", product.getPrice());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_image", product.getImageUrl());
        startActivity(intent);
    }
}
