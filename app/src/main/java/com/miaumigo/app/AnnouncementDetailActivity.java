package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.R;
import com.miaumigo.app.models.Announcement;
import com.miaumigo.app.utils.AnnouncementManager;

import java.text.NumberFormat;
import java.util.Locale;

public class AnnouncementDetailActivity extends AppCompatActivity {

    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewDescription;
    private TextView textViewVendorName;
    private TextView textViewPrice;
    private TextView textViewType;
    private TextView textViewCondition;
    private TextView textViewStatus;
    private MaterialButton buttonContact;
    private MaterialButton buttonEdit;
    private MaterialButton buttonDelete;
    private MaterialButton buttonMarkSold;
    private MaterialButton buttonMarkReserved;
    private ProgressBar progressBar;

    private String announcementId;
    private boolean isOwner;
    private Announcement currentAnnouncement;
    private FirebaseUser currentUser;
    private AnnouncementManager announcementManager;
    private DatabaseReference announcementReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);

        announcementId = getIntent().getStringExtra("announcement_id");
        isOwner = getIntent().getBooleanExtra("is_owner", false);

        if (announcementId == null || announcementId.isEmpty()) {
            Toast.makeText(this, "Anúncio não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        announcementManager = AnnouncementManager.getInstance();

        initViews();
        setupToolbar();
        loadAnnouncement();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalhes do Anúncio");
        }

        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewVendorName = findViewById(R.id.textViewVendorName);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewType = findViewById(R.id.textViewType);
        textViewCondition = findViewById(R.id.textViewCondition);
        textViewStatus = findViewById(R.id.textViewStatus);
        buttonContact = findViewById(R.id.buttonContact);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonMarkSold = findViewById(R.id.buttonMarkSold);
        buttonMarkReserved = findViewById(R.id.buttonMarkReserved);
        progressBar = findViewById(R.id.progressBar);

        // Configura botões conforme o tipo de usuário
        if (isOwner) {
            buttonContact.setVisibility(View.GONE);
            buttonEdit.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonMarkSold.setVisibility(View.VISIBLE);
            buttonMarkReserved.setVisibility(View.VISIBLE);
        } else {
            buttonContact.setVisibility(View.VISIBLE);
            buttonEdit.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
            buttonMarkSold.setVisibility(View.GONE);
            buttonMarkReserved.setVisibility(View.GONE);
        }

        buttonContact.setOnClickListener(v -> contactVendor());
        buttonEdit.setOnClickListener(v -> editAnnouncement());
        buttonDelete.setOnClickListener(v -> deleteAnnouncement());
        buttonMarkSold.setOnClickListener(v -> markAsSold());
        buttonMarkReserved.setOnClickListener(v -> markAsReserved());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalhes do Anúncio");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAnnouncement() {
        showLoading(true);

        announcementReference = FirebaseDatabase.getInstance()
            .getReference("announcements")
            .child(announcementId);

        announcementReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                if (snapshot.exists()) {
                    currentAnnouncement = snapshot.getValue(Announcement.class);
                    if (currentAnnouncement != null) {
                        displayAnnouncement(currentAnnouncement);
                    }
                } else {
                    Toast.makeText(AnnouncementDetailActivity.this,
                        "Anúncio não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AnnouncementDetailActivity.this,
                    "Erro ao carregar anúncio: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAnnouncement(Announcement announcement) {
        if (textViewProductName != null) {
            textViewProductName.setText(announcement.getProductName() != null ?
                announcement.getProductName() : "Produto");
        }

        if (textViewDescription != null) {
            textViewDescription.setText(announcement.getDescription() != null ?
                announcement.getDescription() : "");
        }

        if (textViewVendorName != null) {
            textViewVendorName.setText(announcement.getVendorName() != null ?
                announcement.getVendorName() : "Vendedor");
        }

        if (textViewPrice != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            textViewPrice.setText(currencyFormat.format(announcement.getSuggestedPrice()));
        }

        if (textViewType != null) {
            String typeText = "";
            switch (announcement.getType()) {
                case SALE:
                    typeText = "Venda";
                    break;
                case TRADE:
                    typeText = "Troca";
                    break;
                case REQUEST:
                    typeText = "Busca";
                    break;
            }
            textViewType.setText(typeText);
        }

        if (textViewCondition != null) {
            textViewCondition.setText(announcement.getCondition() != null ?
                announcement.getCondition() : "Não especificado");
        }

        if (textViewStatus != null) {
            Announcement.AnnouncementStatus status = announcement.getStatus() != null ?
                announcement.getStatus() : Announcement.AnnouncementStatus.AVAILABLE;
            String statusText = "";
            int statusColor = getColor(R.color.success);
            switch (status) {
                case AVAILABLE:
                    statusText = "Disponível";
                    statusColor = getColor(R.color.success);
                    break;
                case RESERVED:
                    statusText = "Reservado";
                    statusColor = getColor(R.color.warning);
                    break;
                case SOLD:
                    statusText = "Vendido";
                    statusColor = getColor(R.color.error);
                    break;
            }
            textViewStatus.setText(statusText);
            textViewStatus.setTextColor(statusColor);
        }

        if (imageViewProduct != null) {
            if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
                Glide.with(this).load(announcement.getImageUrl())
                    .placeholder(R.drawable.ic_product_placeholder)
                    .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
            }
        }

        // Atualiza visibilidade dos botões de status
        if (isOwner && buttonMarkSold != null && buttonMarkReserved != null) {
            Announcement.AnnouncementStatus status = announcement.getStatus() != null ?
                announcement.getStatus() : Announcement.AnnouncementStatus.AVAILABLE;
            
            if (status == Announcement.AnnouncementStatus.SOLD) {
                buttonMarkSold.setEnabled(false);
                buttonMarkReserved.setEnabled(false);
            } else if (status == Announcement.AnnouncementStatus.RESERVED) {
                buttonMarkSold.setEnabled(true);
                buttonMarkReserved.setEnabled(true);
                buttonMarkReserved.setText("Remover Reserva");
            } else {
                buttonMarkSold.setEnabled(true);
                buttonMarkReserved.setEnabled(true);
                buttonMarkReserved.setText("Marcar como Reservado");
            }
        }
    }

    private void contactVendor() {
        if (currentAnnouncement == null || currentUser == null) {
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        String chatId = com.miaumigo.app.utils.ChatManager.getInstance()
            .getOrCreateChatId(currentUser.getUid(), currentAnnouncement.getVendorId());
        intent.putExtra("chat_id", chatId);
        intent.putExtra("other_vendor_id", currentAnnouncement.getVendorId());
        intent.putExtra("other_vendor_name", currentAnnouncement.getVendorName());
        startActivity(intent);
    }

    private void editAnnouncement() {
        if (announcementId == null) {
            return;
        }

        Intent intent = new Intent(this, CreateAnnouncementActivity.class);
        intent.putExtra("announcement_id", announcementId);
        intent.putExtra("edit_mode", true);
        startActivity(intent);
    }

    private void deleteAnnouncement() {
        if (currentAnnouncement == null || currentUser == null) {
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Deseja realmente excluir este anúncio?")
            .setPositiveButton("Excluir", (dialog, which) -> {
                showLoading(true);
                announcementManager.deleteAnnouncement(announcementId, currentUser.getUid());
                showLoading(false);
                Toast.makeText(this, "Anúncio excluído com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void markAsSold() {
        if (currentAnnouncement == null) {
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Marcar como Vendido")
            .setMessage("Deseja marcar este anúncio como vendido?")
            .setPositiveButton("Confirmar", (dialog, which) -> {
                updateAnnouncementStatus(Announcement.AnnouncementStatus.SOLD);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void markAsReserved() {
        if (currentAnnouncement == null) {
            return;
        }

        Announcement.AnnouncementStatus currentStatus = currentAnnouncement.getStatus() != null ?
            currentAnnouncement.getStatus() : Announcement.AnnouncementStatus.AVAILABLE;

        if (currentStatus == Announcement.AnnouncementStatus.RESERVED) {
            // Remove reserva
            new AlertDialog.Builder(this)
                .setTitle("Remover Reserva")
                .setMessage("Deseja remover a reserva deste anúncio?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    updateAnnouncementStatus(Announcement.AnnouncementStatus.AVAILABLE);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        } else {
            // Marca como reservado
            new AlertDialog.Builder(this)
                .setTitle("Marcar como Reservado")
                .setMessage("Deseja marcar este anúncio como reservado?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    updateAnnouncementStatus(Announcement.AnnouncementStatus.RESERVED);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        }
    }

    private void updateAnnouncementStatus(Announcement.AnnouncementStatus status) {
        if (currentAnnouncement == null) {
            return;
        }

        showLoading(true);
        currentAnnouncement.setStatus(status);
        currentAnnouncement.setUpdatedAt(System.currentTimeMillis());
        announcementManager.updateAnnouncement(currentAnnouncement);

        showLoading(false);
        String statusText = status == Announcement.AnnouncementStatus.SOLD ? "vendido" :
            status == Announcement.AnnouncementStatus.RESERVED ? "reservado" : "disponível";
        Toast.makeText(this, "Anúncio marcado como " + statusText + "!", Toast.LENGTH_SHORT).show();
        displayAnnouncement(currentAnnouncement);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

