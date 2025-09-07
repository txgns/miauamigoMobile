package com.miaumigo.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.miaumigo.app.adapters.OrderAdapter;
import com.miaumigo.app.models.Order;
import com.miaumigo.app.services.FirebaseAuthService;
import com.miaumigo.app.services.FirebaseDatabaseService;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerViewOrders;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private OrderAdapter orderAdapter;
    private FirebaseDatabaseService databaseService;
    private FirebaseAuthService authService;
    private List<Order> orders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadOrders();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        
        databaseService = new FirebaseDatabaseService(getContext());
        authService = new FirebaseAuthService(getContext());
        orders = new ArrayList<>();
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(orders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            textViewEmpty.setVisibility(View.VISIBLE);
            return;
        }

        showLoading(true);
        
        databaseService.getOrders(currentUser.getUid(), new FirebaseDatabaseService.ListCallback<Order>() {
            @Override
            public void onSuccess(List<Order> orderList) {
                showLoading(false);
                orders.clear();
                orders.addAll(orderList);
                orderAdapter.notifyDataSetChanged();
                
                if (orders.isEmpty()) {
                    textViewEmpty.setVisibility(View.VISIBLE);
                } else {
                    textViewEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}

