package com.miaumigo.app.fragments.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.R;
import com.miaumigo.app.StoreManagementActivity;
import com.miaumigo.app.models.Store;

public class VendorStoreFragment extends Fragment {

    private TextView textViewStoreName;
    private TextView textViewStoreDescription;
    private TextView textViewStoreRating;
    private TextView textViewTotalSales;
    private TextView textViewTotalTrades;
    private ImageView imageViewStoreLogo;
    private ImageView imageViewStoreBanner;
    private Button buttonManageStore;
    private ProgressBar progressBar;
    
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_store, container, false);
        
        initViews(view);
        setupClickListeners();
        loadStoreData();
        
        return view;
    }

    private void initViews(View view) {
        textViewStoreName = view.findViewById(R.id.textViewStoreName);
        textViewStoreDescription = view.findViewById(R.id.textViewStoreDescription);
        textViewStoreRating = view.findViewById(R.id.textViewStoreRating);
        textViewTotalSales = view.findViewById(R.id.textViewTotalSales);
        textViewTotalTrades = view.findViewById(R.id.textViewTotalTrades);
        imageViewStoreLogo = view.findViewById(R.id.imageViewStoreLogo);
        imageViewStoreBanner = view.findViewById(R.id.imageViewStoreBanner);
        buttonManageStore = view.findViewById(R.id.buttonManageStore);
        progressBar = view.findViewById(R.id.progressBar);
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupClickListeners() {
        buttonManageStore.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StoreManagementActivity.class);
            startActivity(intent);
        });
    }

    private void loadStoreData() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        
        com.google.firebase.database.Query storeQuery = FirebaseDatabase.getInstance().getReference("stores")
            .orderByChild("vendorId").equalTo(currentUser.getUid())
            .limitToFirst(1);
        
        storeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                
                if (snapshot.exists() && snapshot.getChildren().iterator().hasNext()) {
                    DataSnapshot storeSnapshot = snapshot.getChildren().iterator().next();
                    Store store = storeSnapshot.getValue(Store.class);
                    
                    if (store != null) {
                        displayStoreData(store);
                    } else {
                        showCreateStorePrompt();
                    }
                } else {
                    showCreateStorePrompt();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(getContext(), "Erro ao carregar loja: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStoreData(Store store) {
        textViewStoreName.setText(store.getName() != null ? store.getName() : "Minha Loja");
        textViewStoreDescription.setText(store.getDescription() != null ? 
            store.getDescription() : "Sem descrição");
        textViewStoreRating.setText(String.format("⭐ %.1f", store.getRating()));
        textViewTotalSales.setText("Vendas: " + store.getTotalSales());
        textViewTotalTrades.setText("Trocas: " + store.getTotalTrades());
        
        if (store.getLogoUrl() != null && !store.getLogoUrl().isEmpty()) {
            Glide.with(this).load(store.getLogoUrl()).into(imageViewStoreLogo);
        }
        
        if (store.getBannerUrl() != null && !store.getBannerUrl().isEmpty()) {
            Glide.with(this).load(store.getBannerUrl()).into(imageViewStoreBanner);
        }
    }

    private void showCreateStorePrompt() {
        textViewStoreName.setText("Loja não configurada");
        textViewStoreDescription.setText("Clique em 'Gerenciar Loja' para criar sua loja");
        buttonManageStore.setText("Criar Loja");
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

