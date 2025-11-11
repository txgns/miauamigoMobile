package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.miaumigo.app.models.Product;

import java.util.UUID;

public class ProductManagementActivity extends AppCompatActivity {

    private EditText editTextProductName;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private EditText editTextQuantity;
    private CheckBox checkBoxInStock;
    private CheckBox checkBoxVisibleToCustomers;
    private Button buttonSave;
    private Button buttonDelete;
    private ProgressBar progressBar;
    
    private FirebaseUser currentUser;
    private DatabaseReference productReference;
    private Product currentProduct;
    private boolean isEditMode;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        initViews();
        setupToolbar();
        getIntentData();
        setupButtons();
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (isEditMode) {
            loadProductData();
        } else {
            buttonDelete.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        checkBoxInStock = findViewById(R.id.checkBoxInStock);
        checkBoxVisibleToCustomers = findViewById(R.id.checkBoxVisibleToCustomers);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Editar Produto" : "Novo Produto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getIntentData() {
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        productId = getIntent().getStringExtra("product_id");
    }

    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveProduct());
        buttonDelete.setOnClickListener(v -> deleteProduct());
    }

    private void loadProductData() {
        if (productId == null) {
            Toast.makeText(this, "Erro: ID do produto não fornecido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        productReference = FirebaseDatabase.getInstance().getReference("products").child(productId);
        
        productReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                
                if (snapshot.exists()) {
                    currentProduct = snapshot.getValue(Product.class);
                    if (currentProduct != null) {
                        displayProductData(currentProduct);
                    }
                } else {
                    Toast.makeText(ProductManagementActivity.this, 
                        "Produto não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(ProductManagementActivity.this, 
                    "Erro ao carregar produto: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductData(Product product) {
        editTextProductName.setText(product.getName());
        editTextDescription.setText(product.getDescription());
        editTextPrice.setText(String.valueOf(product.getPrice()));
        editTextQuantity.setText(String.valueOf(product.getQuantity()));
        checkBoxInStock.setChecked(product.isInStock());
        checkBoxVisibleToCustomers.setChecked(product.isVisibleToCustomers());
    }

    private void saveProduct() {
        String productName = editTextProductName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();
        
        if (TextUtils.isEmpty(productName)) {
            editTextProductName.setError("Nome do produto é obrigatório");
            return;
        }
        
        if (TextUtils.isEmpty(priceStr)) {
            editTextPrice.setError("Preço é obrigatório");
            return;
        }
        
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            editTextPrice.setError("Preço inválido");
            return;
        }
        
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityStr)) {
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                editTextQuantity.setError("Quantidade inválida");
                return;
            }
        }
        
        if (currentUser == null) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        if (currentProduct == null) {
            // Cria novo produto
            currentProduct = new Product();
            currentProduct.setId(UUID.randomUUID().toString());
            currentProduct.setVendorId(currentUser.getUid());
        }
        
        currentProduct.setName(productName);
        currentProduct.setDescription(description);
        currentProduct.setPrice(price);
        currentProduct.setQuantity(quantity);
        currentProduct.setInStock(checkBoxInStock.isChecked());
        currentProduct.setVisibleToCustomers(checkBoxVisibleToCustomers.isChecked());
        currentProduct.setUpdatedAt(System.currentTimeMillis());
        
        if (currentProduct.getCreatedAt() == 0) {
            currentProduct.setCreatedAt(System.currentTimeMillis());
        }
        
        productReference = FirebaseDatabase.getInstance().getReference("products")
            .child(currentProduct.getId());
        productReference.setValue(currentProduct)
            .addOnCompleteListener(task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    Toast.makeText(ProductManagementActivity.this, 
                        "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductManagementActivity.this, 
                        "Erro ao salvar produto", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void deleteProduct() {
        if (productId == null || currentProduct == null) {
            return;
        }
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Deseja realmente excluir este produto?")
            .setPositiveButton("Excluir", (dialog, which) -> {
                showLoading(true);
                productReference = FirebaseDatabase.getInstance().getReference("products")
                    .child(productId);
                productReference.removeValue()
                    .addOnCompleteListener(task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(ProductManagementActivity.this, 
                                "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ProductManagementActivity.this, 
                                "Erro ao excluir produto", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSave.setEnabled(!show);
        buttonDelete.setEnabled(!show);
    }
}

