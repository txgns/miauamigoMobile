package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.miaumigo.app.models.Announcement;
import com.miaumigo.app.models.User;
import com.miaumigo.app.utils.AnnouncementManager;
import com.miaumigo.app.utils.EncryptionManager;

public class CreateAnnouncementActivity extends AppCompatActivity {

    private EditText editTextProductName;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private Spinner spinnerType;
    private Spinner spinnerCondition;
    private Button buttonCreate;
    private ProgressBar progressBar;
    
    private FirebaseUser currentUser;
    private AnnouncementManager announcementManager;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        initViews();
        setupToolbar();
        setupSpinners();
        setupCreateButton();
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        announcementManager = AnnouncementManager.getInstance();
    }

    private void initViews() {
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        buttonCreate = findViewById(R.id.buttonCreate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Criar Anúncio");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        // Tipo de anúncio
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
            R.array.announcement_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        
        // Condição
        ArrayAdapter<CharSequence> conditionAdapter = ArrayAdapter.createFromResource(this,
            R.array.product_conditions, android.R.layout.simple_spinner_item);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupCreateButton() {
        buttonCreate.setOnClickListener(v -> createAnnouncement());
    }

    private void createAnnouncement() {
        String productName = editTextProductName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        
        if (TextUtils.isEmpty(productName)) {
            editTextProductName.setError("Nome do produto é obrigatório");
            return;
        }
        
        if (TextUtils.isEmpty(description)) {
            editTextDescription.setError("Descrição é obrigatória");
            return;
        }
        
        double price = 0.0;
        if (!TextUtils.isEmpty(priceStr)) {
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                editTextPrice.setError("Preço inválido");
                return;
            }
        }
        
        if (currentUser == null) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        final double finalPrice = price;
        final String finalProductName = productName;
        final String finalDescription = description;
        
        // Busca dados do vendedor
        userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    showLoading(false);
                    Toast.makeText(CreateAnnouncementActivity.this, 
                        "Erro ao buscar dados do usuário", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Cria anúncio
                EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());
                user.setName(encryptionManager.decrypt(user.getName()));
                user.setPhone(encryptionManager.decrypt(user.getPhone()));

                Announcement announcement = new Announcement();
                announcement.setVendorId(currentUser.getUid());
                announcement.setVendorName(user.getName());
                announcement.setVendorAvatar(user.getAvatarUrl());
                announcement.setProductName(finalProductName);
                announcement.setDescription(finalDescription);
                announcement.setSuggestedPrice(finalPrice);
                announcement.setCondition(spinnerCondition.getSelectedItem().toString());
                
                // Tipo
                String typeStr = spinnerType.getSelectedItem().toString();
                if (typeStr.equals("Venda")) {
                    announcement.setType(Announcement.AnnouncementType.SALE);
                } else if (typeStr.equals("Troca")) {
                    announcement.setType(Announcement.AnnouncementType.TRADE);
                } else {
                    announcement.setType(Announcement.AnnouncementType.REQUEST);
                }
                
                announcementManager.createAnnouncement(announcement);
                
                showLoading(false);
                Toast.makeText(CreateAnnouncementActivity.this, 
                    "Anúncio criado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(CreateAnnouncementActivity.this, 
                    "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonCreate.setEnabled(!show);
    }
}

