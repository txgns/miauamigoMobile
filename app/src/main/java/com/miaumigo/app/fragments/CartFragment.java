package com.miaumigo.app.fragments;

import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.R;
import com.miaumigo.app.adapters.CartAdapter;
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemClickListener {

    private RecyclerView recyclerViewCart;
    private TextView textViewEmpty;
    private TextView textViewTotal;
    private Button buttonCheckout;
    private ProgressBar progressBar;
    
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private CartManager cartManager;
    private double totalPrice = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadCartItems();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewTotal = view.findViewById(R.id.textViewTotal);
        buttonCheckout = view.findViewById(R.id.buttonCheckout);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        cartManager = CartManager.getInstance(getContext());
        cartAdapter = new CartAdapter(cartItems, this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        buttonCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void loadCartItems() {
        showLoading(true);
        
        // Carregar itens do carrinho usando CartManager
        cartItems.clear();
        cartItems.addAll(cartManager.getCartItems());
        
        cartAdapter.notifyDataSetChanged();
        calculateTotal();
        updateEmptyState();
        
        showLoading(false);
    }

    private void calculateTotal() {
        totalPrice = 0.0;
        for (CartItem item : cartItems) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        
        textViewTotal.setText(String.format("R$ %.2f", totalPrice));
    }

    private void updateEmptyState() {
        if (cartItems.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewCart.setVisibility(View.GONE);
            buttonCheckout.setEnabled(false);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewCart.setVisibility(View.VISIBLE);
            buttonCheckout.setEnabled(true);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Carrinho vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Implementar lógica de checkout
        Toast.makeText(getContext(), "Redirecionando para checkout...", Toast.LENGTH_SHORT).show();
        // Aqui você implementaria a navegação para a tela de checkout
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartManager.removeFromCart(item.getId());
        loadCartItems(); // Recarregar itens do carrinho
        Toast.makeText(getContext(), "Item removido do carrinho", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateQuantity(CartItem item, int newQuantity) {
        cartManager.updateQuantity(item.getId(), newQuantity);
        loadCartItems(); // Recarregar itens do carrinho
    }
}
