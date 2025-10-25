package com.miaumigo.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.R;
import com.miaumigo.app.adapters.OrderAdapter;
import com.miaumigo.app.models.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerViewOrders;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadOrders();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        showLoading(true);
        
        // Simular carregamento de pedidos
        // Em uma implementação real, isso viria do Firebase
        orderList.clear();
        
        // Adicionar alguns pedidos de exemplo
        orderList.add(new Order("ORD001", new Date(), "Processando", 155.40, 3));
        orderList.add(new Order("ORD002", new Date(System.currentTimeMillis() - 86400000), "Enviado", 89.90, 1));
        orderList.add(new Order("ORD003", new Date(System.currentTimeMillis() - 172800000), "Entregue", 45.00, 1));
        
        orderAdapter.notifyDataSetChanged();
        updateEmptyState();
        showLoading(false);
    }

    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
