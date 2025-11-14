package com.miaumigo.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.adapters.CheckoutItemAdapter;
import com.miaumigo.app.models.Address;
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.models.Order;
import com.miaumigo.app.models.OrderItem;
import com.miaumigo.app.utils.CartManager;
import com.miaumigo.app.utils.EncryptionManager;
import com.miaumigo.app.utils.OrderManager;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private TextView textViewSubtotal;
    private TextView textViewShipping;
    private TextView textViewDiscount;
    private TextView textViewTotal;
    private TextView textViewAddress;
    private Button buttonSelectAddress;
    private Spinner spinnerPaymentMethod;
    private Button buttonConfirmOrder;
    private ProgressBar progressBar;

    private CheckoutItemAdapter checkoutAdapter;
    private List<CartItem> cartItems;
    private List<Address> userAddresses;
    private Address selectedAddress;
    private String selectedPaymentMethod;
    private double subtotal = 0.0;
    private double shipping = 10.0; // Frete fixo de exemplo
    private double discount = 0.0;
    private double total = 0.0;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private CartManager cartManager;
    private OrderManager orderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initFirebase();
        if (firebaseUser == null) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadCartItems();
        loadUserAddresses();
        setupPaymentMethod();
        setupClickListeners();
    }

    private void initFirebase() {
        FirebaseApp app = FirebaseApp.initializeApp(this);
        if (app == null && FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        cartManager = CartManager.getInstance(this);
        orderManager = new OrderManager();
    }

    private void initViews() {
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShipping = findViewById(R.id.textViewShipping);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewAddress = findViewById(R.id.textViewAddress);
        buttonSelectAddress = findViewById(R.id.buttonSelectAddress);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        buttonConfirmOrder = findViewById(R.id.buttonConfirmOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        checkoutAdapter = new CheckoutItemAdapter(cartItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(checkoutAdapter);
    }

    private void loadCartItems() {
        cartItems.clear();
        cartItems.addAll(cartManager.getCartItems());
        checkoutAdapter.notifyDataSetChanged();
        calculateTotals();
    }

    private void loadUserAddresses() {
        if (firebaseUser == null) return;

        showLoading(true);
        Query query = databaseReference.child("addresses")
                .orderByChild("userId")
                .equalTo(firebaseUser.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                userAddresses = new ArrayList<>();
                EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());

                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    Address address = addressSnapshot.getValue(Address.class);
                    if (address != null) {
                        address.setId(addressSnapshot.getKey());
                        // Descriptografar endereço
                        address.setStreet(encryptionManager.decrypt(address.getStreet()));
                        address.setNumber(encryptionManager.decrypt(address.getNumber()));
                        address.setComplement(encryptionManager.decrypt(address.getComplement()));
                        address.setNeighborhood(encryptionManager.decrypt(address.getNeighborhood()));
                        address.setCity(encryptionManager.decrypt(address.getCity()));
                        address.setState(encryptionManager.decrypt(address.getState()));
                        address.setZipCode(encryptionManager.decrypt(address.getZipCode()));
                        userAddresses.add(address);
                    }
                }

                // Selecionar endereço padrão
                for (Address address : userAddresses) {
                    if (address.isDefault()) {
                        selectedAddress = address;
                        updateAddressDisplay();
                        break;
                    }
                }

                if (userAddresses.isEmpty()) {
                    textViewAddress.setText("Nenhum endereço cadastrado");
                    buttonSelectAddress.setText("Cadastrar Endereço");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this, "Erro ao carregar endereços", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPaymentMethod() {
        String[] paymentMethods = {"Cartão de Crédito", "PIX", "Boleto"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(adapter);
        spinnerPaymentMethod.setSelection(0);
        selectedPaymentMethod = paymentMethods[0];

        spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPaymentMethod = paymentMethods[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupClickListeners() {
        buttonSelectAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditAddressActivity.class);
            startActivityForResult(intent, 100);
        });

        buttonConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void updateAddressDisplay() {
        if (selectedAddress != null) {
            String addressText = String.format("%s, %s\n%s - %s/%s\nCEP: %s",
                    selectedAddress.getStreet(),
                    selectedAddress.getNumber(),
                    selectedAddress.getNeighborhood(),
                    selectedAddress.getCity(),
                    selectedAddress.getState(),
                    selectedAddress.getZipCode());
            if (!TextUtils.isEmpty(selectedAddress.getComplement())) {
                addressText = selectedAddress.getComplement() + "\n" + addressText;
            }
            textViewAddress.setText(addressText);
        }
    }

    private void calculateTotals() {
        subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        total = subtotal + shipping - discount;

        textViewSubtotal.setText(String.format("R$ %.2f", subtotal));
        textViewShipping.setText(String.format("R$ %.2f", shipping));
        textViewDiscount.setText(String.format("R$ %.2f", discount));
        textViewTotal.setText(String.format("R$ %.2f", total));
    }

    private void confirmOrder() {
        // Validações
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Carrinho vazio", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (selectedAddress == null) {
            Toast.makeText(this, "Selecione um endereço de entrega", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedPaymentMethod)) {
            Toast.makeText(this, "Selecione um método de pagamento", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Converter CartItems para OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getId(),
                    cartItem.getName(),
                    cartItem.getImageUrl(),
                    cartItem.getPrice(),
                    cartItem.getQuantity()
            );
            orderItems.add(orderItem);
        }

        // Criar pedido
        Order order = new Order(firebaseUser.getUid(), orderItems, selectedAddress, selectedPaymentMethod);
        order.setShipping(shipping);
        order.setDiscount(discount);

        // Criptografar endereço antes de salvar
        EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());
        Address encryptedAddress = new Address();
        encryptedAddress.setId(selectedAddress.getId());
        encryptedAddress.setUserId(selectedAddress.getUserId());
        encryptedAddress.setStreet(encryptionManager.encrypt(selectedAddress.getStreet()));
        encryptedAddress.setNumber(encryptionManager.encrypt(selectedAddress.getNumber()));
        encryptedAddress.setComplement(encryptionManager.encrypt(selectedAddress.getComplement()));
        encryptedAddress.setNeighborhood(encryptionManager.encrypt(selectedAddress.getNeighborhood()));
        encryptedAddress.setCity(encryptionManager.encrypt(selectedAddress.getCity()));
        encryptedAddress.setState(encryptionManager.encrypt(selectedAddress.getState()));
        encryptedAddress.setZipCode(encryptionManager.encrypt(selectedAddress.getZipCode()));
        encryptedAddress.setDefault(selectedAddress.isDefault());
        order.setDeliveryAddress(encryptedAddress);

        orderManager.createOrder(order, new OrderManager.OnOrderCreatedListener() {
            @Override
            public void onSuccess(Order createdOrder) {
                showLoading(false);
                // Limpar carrinho
                cartManager.clearCart();
                Toast.makeText(CheckoutActivity.this, "Pedido realizado com sucesso!", Toast.LENGTH_SHORT).show();
                
                // Redirecionar para Orders
                Intent intent = new Intent(CheckoutActivity.this, HomeActivity.class);
                intent.putExtra("fragment", "orders");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this, "Erro ao criar pedido: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadUserAddresses();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonConfirmOrder.setEnabled(!show);
    }
}

