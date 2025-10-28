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
                String productName = product.getName().toLowerCase();
                String productDesc = product.getDescription().toLowerCase();
                
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
