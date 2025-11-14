package com.miaumigo.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.adapters.OrderItemAdapter;
import com.miaumigo.app.models.Address;
import com.miaumigo.app.models.Order;
import com.miaumigo.app.utils.EncryptionManager;
import com.miaumigo.app.utils.OrderManager;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView textViewOrderId;
    private TextView textViewOrderDate;
    private TextView textViewOrderStatus;
    private RecyclerView recyclerViewItems;
    private TextView textViewSubtotal;
    private TextView textViewShipping;
    private TextView textViewDiscount;
    private TextView textViewTotal;
    private TextView textViewAddress;
    private TextView textViewPaymentMethod;
    private TextView textViewVendorName;
    private ProgressBar progressBar;

    private OrderItemAdapter orderItemAdapter;
    private Order currentOrder;
    private String orderId;
    private OrderManager orderManager;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            Toast.makeText(this, "Pedido não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initFirebase();
        initViews();
        setupRecyclerView();
        loadOrderDetails();
    }

    private void initFirebase() {
        FirebaseApp app = FirebaseApp.initializeApp(this);
        if (app == null && FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        orderManager = new OrderManager();
    }

    private void initViews() {
        textViewOrderId = findViewById(R.id.textViewOrderId);
        textViewOrderDate = findViewById(R.id.textViewOrderDate);
        textViewOrderStatus = findViewById(R.id.textViewOrderStatus);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShipping = findViewById(R.id.textViewShipping);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewPaymentMethod = findViewById(R.id.textViewPaymentMethod);
        textViewVendorName = findViewById(R.id.textViewVendorName);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter(null);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(orderItemAdapter);
    }

    private void loadOrderDetails() {
        showLoading(true);

        orderManager.getOrderById(orderId, new OrderManager.OnOrderLoadedListener() {
            @Override
            public void onOrderLoaded(Order order) {
                currentOrder = order;
                displayOrderDetails();
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(OrderDetailsActivity.this, "Erro ao carregar pedido: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayOrderDetails() {
        if (currentOrder == null) return;

        // Informações gerais
        textViewOrderId.setText("Pedido #" + (currentOrder.getId() != null ? 
            currentOrder.getId().substring(0, Math.min(8, currentOrder.getId().length())) : "N/A"));
        
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        textViewOrderDate.setText(dateFormat.format(new java.util.Date(currentOrder.getCreatedAt())));
        
        textViewOrderStatus.setText(currentOrder.getStatus());
        setStatusColor(currentOrder.getStatus());

        // Itens do pedido
        if (currentOrder.getItems() != null) {
            orderItemAdapter.updateItems(currentOrder.getItems());
        }

        // Resumo financeiro
        textViewSubtotal.setText(String.format("R$ %.2f", currentOrder.getSubtotal()));
        textViewShipping.setText(String.format("R$ %.2f", currentOrder.getShipping()));
        textViewDiscount.setText(String.format("R$ %.2f", currentOrder.getDiscount()));
        textViewTotal.setText(String.format("R$ %.2f", currentOrder.getTotal()));

        // Endereço de entrega
        Address address = currentOrder.getDeliveryAddress();
        if (address != null) {
            EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());
            String addressText = String.format("%s, %s\n%s - %s/%s\nCEP: %s",
                    encryptionManager.decrypt(address.getStreet()),
                    encryptionManager.decrypt(address.getNumber()),
                    encryptionManager.decrypt(address.getNeighborhood()),
                    encryptionManager.decrypt(address.getCity()),
                    encryptionManager.decrypt(address.getState()),
                    encryptionManager.decrypt(address.getZipCode()));
            if (address.getComplement() != null && !address.getComplement().isEmpty()) {
                addressText = encryptionManager.decrypt(address.getComplement()) + "\n" + addressText;
            }
            textViewAddress.setText(addressText);
        }

        // Método de pagamento
        textViewPaymentMethod.setText(currentOrder.getPaymentMethod() != null ? 
            currentOrder.getPaymentMethod() : "Não informado");

        // Vendedor (se aplicável)
        if (currentOrder.getVendorName() != null && !currentOrder.getVendorName().isEmpty()) {
            textViewVendorName.setVisibility(View.VISIBLE);
            textViewVendorName.setText("Loja: " + currentOrder.getVendorName());
        } else {
            textViewVendorName.setVisibility(View.GONE);
        }
    }

    private void setStatusColor(String status) {
        if (status == null) {
            textViewOrderStatus.setBackgroundResource(R.drawable.status_default);
            textViewOrderStatus.setTextColor(getColor(R.color.white));
            return;
        }
        
        // Sempre usar texto branco para melhor contraste
        textViewOrderStatus.setTextColor(getColor(R.color.white));
        
        switch (status.toLowerCase()) {
            case "pendente":
                textViewOrderStatus.setBackgroundResource(R.drawable.status_pendente);
                break;
            case "processando":
                textViewOrderStatus.setBackgroundResource(R.drawable.status_processando);
                break;
            case "enviado":
                textViewOrderStatus.setBackgroundResource(R.drawable.status_enviado);
                break;
            case "entregue":
                textViewOrderStatus.setBackgroundResource(R.drawable.status_entregue);
                break;
            case "cancelado":
                textViewOrderStatus.setBackgroundResource(R.drawable.status_cancelado);
                break;
            default:
                textViewOrderStatus.setBackgroundResource(R.drawable.status_default);
                break;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

