package com.miaumigo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.EditProfileActivity;
import com.miaumigo.app.EditAddressActivity;
import com.miaumigo.app.HomeActivity;
import com.miaumigo.app.ProductDetailActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ProductAdapter;
import com.miaumigo.app.models.Product;
import com.miaumigo.app.models.User;
import com.miaumigo.app.utils.CartManager;
import com.miaumigo.app.utils.EncryptionManager;
import com.miaumigo.app.utils.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductActionListener {

    private static final String TAG = "HomeFragment";
    private static final int MAX_FEATURED_PRODUCTS = 6;

    private TextView textViewWelcome;
    private Button buttonEditProfile;
    private Button buttonEditAddress;
    private Button buttonViewAllProducts;
    private RecyclerView recyclerViewFeaturedProducts;
    private ProgressBar progressBarFeatured;
    private TextView textViewEmptyFeatured;
    
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference productsReference;
    
    private ProductAdapter featuredProductsAdapter;
    private List<Product> featuredProducts;
    private CartManager cartManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_home, container, false);
            
            initViews(view);
            initFirebase();
            setupClickListeners();
            setupRecyclerView();
            loadUserData();
            loadFeaturedProducts();
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("HomeFragment", "Erro ao criar view", e);
            e.printStackTrace();
            // Retorna uma view vazia em caso de erro
            return new View(getContext());
        }
    }

    private void initViews(View view) {
        textViewWelcome = view.findViewById(R.id.textViewWelcome);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonEditAddress = view.findViewById(R.id.buttonEditAddress);
        buttonViewAllProducts = view.findViewById(R.id.buttonViewAllProducts);
        recyclerViewFeaturedProducts = view.findViewById(R.id.recyclerViewFeaturedProducts);
        progressBarFeatured = view.findViewById(R.id.progressBarFeatured);
        textViewEmptyFeatured = view.findViewById(R.id.textViewEmptyFeatured);
        
        cartManager = CartManager.getInstance(getContext());
        featuredProducts = new ArrayList<>();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        productsReference = FirebaseDatabase.getInstance().getReference("products");
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> openEditProfile());
        buttonEditAddress.setOnClickListener(v -> openEditAddress());
        if (buttonViewAllProducts != null) {
            buttonViewAllProducts.setOnClickListener(v -> navigateToProducts());
        }
    }
    
    private void setupRecyclerView() {
        if (recyclerViewFeaturedProducts == null) {
            return;
        }
        
        // Usa LinearLayoutManager horizontal para mostrar produtos em scroll horizontal
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewFeaturedProducts.setLayoutManager(layoutManager);
        
        // Adapter para produtos em destaque (mostra botão de adicionar ao carrinho)
        featuredProductsAdapter = new ProductAdapter(featuredProducts, this, false);
        recyclerViewFeaturedProducts.setAdapter(featuredProductsAdapter);
        
        // Adiciona espaçamento entre itens
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_8);
        recyclerViewFeaturedProducts.addItemDecoration(new GridSpacingItemDecoration(1, spacing, false));
    }
    
    private void loadFeaturedProducts() {
        if (productsReference == null) {
            return;
        }
        
        showLoading(true);
        
        productsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                featuredProducts.clear();
                
                List<Product> allProducts = new ArrayList<>();
                
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    try {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null) {
                            product.setId(productSnapshot.getKey());
                            
                            // Filtra apenas produtos visíveis para clientes e em estoque
                            if (product.isVisibleToCustomers() && product.isInStock() && product.getQuantity() > 0) {
                                allProducts.add(product);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar produto: " + productSnapshot.getKey(), e);
                    }
                }
                
                // Ordena por rating (maior primeiro) e depois por quantidade de vendas
                Collections.sort(allProducts, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        // Primeiro por rating
                        int ratingCompare = Double.compare(p2.getRating(), p1.getRating());
                        if (ratingCompare != 0) {
                            return ratingCompare;
                        }
                        // Depois por vendas
                        return Long.compare(p2.getSalesCount(), p1.getSalesCount());
                    }
                });
                
                // Pega os primeiros MAX_FEATURED_PRODUCTS produtos
                int count = Math.min(MAX_FEATURED_PRODUCTS, allProducts.size());
                for (int i = 0; i < count; i++) {
                    featuredProducts.add(allProducts.get(i));
                }
                
                // Atualiza o adapter
                if (featuredProductsAdapter != null) {
                    featuredProductsAdapter.notifyDataSetChanged();
                }
                
                // Atualiza estado vazio
                updateEmptyState();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro ao carregar produtos em destaque: " + error.getMessage());
                showLoading(false);
                updateEmptyState();
                Toast.makeText(getContext(), "Erro ao carregar produtos em destaque", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBarFeatured != null) {
            progressBarFeatured.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void updateEmptyState() {
        if (textViewEmptyFeatured != null && featuredProductsAdapter != null) {
            boolean isEmpty = featuredProducts.isEmpty();
            textViewEmptyFeatured.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (recyclerViewFeaturedProducts != null) {
                recyclerViewFeaturedProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        }
    }
    
    private void navigateToProducts() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).navigateToProducts();
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        databaseReference.child("users").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                EncryptionManager encryptionManager = EncryptionManager.getInstance(requireContext());
                                user.setName(encryptionManager.decrypt(user.getName()));
                                if (user.getName() != null && !user.getName().isEmpty()) {
                                    String welcomeText = "Bem-vindo, " + user.getName() + "!";
                                    textViewWelcome.setText(welcomeText);
                                    return;
                                }
                            }
                        }
                        setDefaultWelcome(currentUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        setDefaultWelcome(currentUser);
                    }
                });
    }
    
    private void setDefaultWelcome(FirebaseUser currentUser) {
        String userName = "Usuário";
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            userName = currentUser.getDisplayName();
        }
        String welcomeText = "Bem-vindo, " + userName + "!";
        textViewWelcome.setText(welcomeText);
    }

    private void openEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void openEditAddress() {
        Intent intent = new Intent(getActivity(), EditAddressActivity.class);
        startActivity(intent);
    }
    
    @Override
    public void onProductClick(Product product) {
        if (product == null || getActivity() == null) {
            return;
        }
        
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_price", product.getPrice());
        intent.putExtra("product_image", product.getImageUrl());
        intent.putExtra("vendor_id", product.getVendorId());
        intent.putExtra("vendor_name", product.getVendorName());
        startActivity(intent);
    }
    
    @Override
    public void onAddToCart(Product product) {
        if (product == null || cartManager == null) {
            return;
        }
        
        try {
            com.miaumigo.app.models.CartItem cartItem = new com.miaumigo.app.models.CartItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                1,
                product.getImageUrl()
            );
            cartManager.addToCart(cartItem);
            
            if (getContext() != null) {
                Toast.makeText(getContext(), product.getName() + " adicionado ao carrinho", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao adicionar ao carrinho", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erro ao adicionar produto ao carrinho", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listeners para evitar vazamentos de memória
        if (productsReference != null) {
            productsReference.removeEventListener((ValueEventListener) null);
        }
    }
}
