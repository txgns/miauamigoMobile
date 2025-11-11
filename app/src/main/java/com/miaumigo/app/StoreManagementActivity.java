package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.R;
import com.miaumigo.app.models.Store;
import com.miaumigo.app.utils.StoreManager;

import java.util.UUID;

public class StoreManagementActivity extends AppCompatActivity {

    private EditText editTextStoreName;
    private EditText editTextStoreDescription;
    private Button buttonSave;
    private ProgressBar progressBar;
    
    private FirebaseUser currentUser;
    private StoreManager storeManager;
    private Store currentStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_management);

        initViews();
        setupToolbar();
        setupSaveButton();
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storeManager = StoreManager.getInstance();
        
        loadStoreData();
    }

    private void initViews() {
        editTextStoreName = findViewById(R.id.editTextStoreName);
        editTextStoreDescription = findViewById(R.id.editTextStoreDescription);
        buttonSave = findViewById(R.id.buttonSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gerenciar Loja");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSaveButton() {
        buttonSave.setOnClickListener(v -> saveStore());
    }

    private void loadStoreData() {
        if (currentUser == null) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
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
                    currentStore = storeSnapshot.getValue(Store.class);
                    
                    if (currentStore != null) {
                        displayStoreData(currentStore);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(StoreManagementActivity.this, 
                    "Erro ao carregar loja: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStoreData(Store store) {
        editTextStoreName.setText(store.getName());
        editTextStoreDescription.setText(store.getDescription());
    }

    private void saveStore() {
        String storeName = editTextStoreName.getText().toString().trim();
        String storeDescription = editTextStoreDescription.getText().toString().trim();
        
        if (TextUtils.isEmpty(storeName)) {
            editTextStoreName.setError("Nome da loja é obrigatório");
            return;
        }
        
        if (currentUser == null) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        if (currentStore == null) {
            // Cria nova loja
            currentStore = new Store();
            currentStore.setId(UUID.randomUUID().toString());
            currentStore.setVendorId(currentUser.getUid());
        }
        
        currentStore.setName(storeName);
        currentStore.setDescription(storeDescription);
        
        storeManager.createOrUpdateStore(currentStore);
        
        showLoading(false);
        Toast.makeText(this, "Loja salva com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSave.setEnabled(!show);
    }
}

