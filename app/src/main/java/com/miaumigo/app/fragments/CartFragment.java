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

import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.CartAdapter;
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.services.FirebaseAuthService;
import com.miaumigo.app.services.FirebaseDatabaseService;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemClickListener {

    private RecyclerView recyclerViewCart;
    private TextView textViewEmpty;
    private TextView textViewTotal;
    private Button buttonCheckout;
    private ProgressBar progressBar;
    private CartAdapter cartAdapter;
    private FirebaseDatabaseService databaseService;
    private FirebaseAuthService authService;
    private List<CartItem> cartItems;
    private double totalAmount = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadCartItems();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewTotal = view.findViewById(R.id.textViewTotal);
        buttonCheckout = view.findViewById(R.id.buttonCheckout);
        progressBar = view.findViewById(R.id.progressBar);
        
        databaseService = new FirebaseDatabaseService(getContext());
        authService = new FirebaseAuthService(getContext());
        cartItems = new ArrayList<>();
        
        buttonCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        try {
            FirebaseUser currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                textViewEmpty.setVisibility(View.VISIBLE);
                return;
            }

            showLoading(true);
            
            databaseService.getCart(currentUser.getUid(), new FirebaseDatabaseService.ListCallback<CartItem>() {
                @Override
                public void onSuccess(List<CartItem> items) {
                    showLoading(false);
                    cartItems.clear();
                    if (items != null) {
                        cartItems.addAll(items);
                    }
                    cartAdapter.notifyDataSetChanged();
                    calculateTotal();
                    
                    if (cartItems.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        buttonCheckout.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        buttonCheckout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    textViewEmpty.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            showLoading(false);
            textViewEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void calculateTotal() {
        totalAmount = 0.0;
        for (CartItem item : cartItems) {
            totalAmount += item.getTotalPrice();
        }
        textViewTotal.setText(String.format("Total: R$ %.2f", totalAmount));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewCart.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Carrinho vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement checkout process
        Toast.makeText(getContext(), "Funcionalidade de checkout em desenvolvimento", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onQuantityChanged(CartItem cartItem, int newQuantity) {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) return;

        databaseService.updateCartItem(currentUser.getUid(), cartItem.getProductId(), newQuantity, 
            new FirebaseDatabaseService.DataCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    cartItem.setQuantity(newQuantity);
                    cartAdapter.notifyDataSetChanged();
                    calculateTotal();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onRemoveItem(CartItem cartItem) {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) return;

        databaseService.removeFromCart(currentUser.getUid(), cartItem.getProductId(), 
            new FirebaseDatabaseService.DataCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    cartItems.remove(cartItem);
                    cartAdapter.notifyDataSetChanged();
                    calculateTotal();
                    
                    if (cartItems.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        buttonCheckout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
    }
}

