package com.miaumigo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.ProductDetailActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ProductAdapter;
import com.miaumigo.app.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerViewProducts;
    private TextInputEditText editTextSearch;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private DatabaseReference productsReference;
    private ValueEventListener productsListener;
    
    // Para debounce na busca
    private Handler searchHandler;
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 300; // 300ms de delay
    
    // Mapa de sinônimos para busca mais inteligente
    private Map<String, List<String>> synonymsMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        
        initViews(view);
        initSearchHandler();
        initSynonymsMap();
        setupRecyclerView();
        setupSearch();
        loadProducts();
        
        return view;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpa callbacks pendentes
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (productsReference != null && productsListener != null) {
            productsReference.removeEventListener(productsListener);
        }
    }

    private void initViews(View view) {
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    private void initSearchHandler() {
        searchHandler = new Handler(Looper.getMainLooper());
    }
    
    private void initSynonymsMap() {
        synonymsMap = new HashMap<>();
        
        // Adiciona sinônimos comuns para produtos pet
        synonymsMap.put("cachorro", List.of("cão", "cães", "dog", "canino"));
        synonymsMap.put("gato", List.of("felino", "cat", "gatinho"));
        synonymsMap.put("comida", List.of("ração", "alimento", "feed"));
        synonymsMap.put("brinquedo", List.of("toy", "brincadeira", "diversão"));
        synonymsMap.put("remedio", List.of("remédio", "medicamento", "medicina"));
        synonymsMap.put("higiene", List.of("banho", "limpeza", "shampoo"));
        synonymsMap.put("caminha", List.of("cama", "colchão", "almofada"));
        synonymsMap.put("camisa", List.of("camiseta", "blusa", "roupa"));
        synonymsMap.put("passear", List.of("passeio", "coleira", "guia"));
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
                // Cancela a busca anterior se ainda estiver pendente
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Cria uma nova busca com delay (debounce)
                searchRunnable = () -> filterProducts(s.toString());
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

        productsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null && product.isVisibleToCustomers() && product.isInStock()) {
                        productList.add(product);
                    }
                }

                productList.sort((p1, p2) -> {
                    long time1 = p1.getUpdatedAt() > 0 ? p1.getUpdatedAt() : p1.getCreatedAt();
                    long time2 = p2.getUpdatedAt() > 0 ? p2.getUpdatedAt() : p2.getCreatedAt();
                    return Long.compare(time2, time1);
                });

                String currentQuery = editTextSearch.getText() != null ? editTextSearch.getText().toString() : "";
                if (currentQuery.isEmpty()) {
                    filteredProductList.clear();
                    filteredProductList.addAll(productList);
                    productAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    showLoading(false);
                } else {
                    filterProducts(currentQuery);
                    showLoading(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(getContext(), "Erro ao carregar produtos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                filteredProductList.clear();
                productAdapter.notifyDataSetChanged();
                updateEmptyState();
            }
        };

        productsReference.addValueEventListener(productsListener);
    }

    private void filterProducts(String query) {
        filteredProductList.clear();
        
        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            
            // Busca expandida com sinônimos
            List<String> searchTerms = new ArrayList<>();
            searchTerms.add(lowerQuery);
            
            // Adiciona sinônimos se existirem
            for (Map.Entry<String, List<String>> entry : synonymsMap.entrySet()) {
                if (lowerQuery.contains(entry.getKey())) {
                    searchTerms.addAll(entry.getValue());
                }
                for (String synonym : entry.getValue()) {
                    if (lowerQuery.contains(synonym)) {
                        searchTerms.add(entry.getKey());
                        searchTerms.addAll(entry.getValue());
                        break;
                    }
                }
            }
            
            // Filtra produtos usando todos os termos de busca
            for (Product product : productList) {
                String productName = product.getName() != null ? product.getName().toLowerCase() : "";
                String productDesc = product.getDescription() != null ? product.getDescription().toLowerCase() : "";
                
                boolean matches = false;
                for (String term : searchTerms) {
                    if (productName.contains(term) || productDesc.contains(term)) {
                        matches = true;
                        break;
                    }
                }
                
                if (matches && !filteredProductList.contains(product)) {
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
