package com.miaumigo.app.fragments;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.R;
import com.miaumigo.app.OrderDetailsActivity;
import com.miaumigo.app.adapters.OrderAdapter;
import com.miaumigo.app.models.Order;
import com.miaumigo.app.utils.OrderManager;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerViewOrders;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private OrderManager orderManager;
    private FirebaseUser firebaseUser;

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
        
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        orderManager = new OrderManager();
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, order -> {
            // Navegar para detalhes do pedido
            Intent intent = new Intent(getContext(), OrderDetailsActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        if (firebaseUser == null) {
            textViewEmpty.setVisibility(View.VISIBLE);
            textViewEmpty.setText("Faça login para ver seus pedidos");
            return;
        }

        showLoading(true);
        
        Query ordersQuery = orderManager.getOrdersByUser(firebaseUser.getUid());
        ordersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        order.setId(orderSnapshot.getKey());
                        orderList.add(order);
                    }
                }
                
                // Ordenar por data (mais recente primeiro)
                orderList.sort((o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                
                orderAdapter.notifyDataSetChanged();
                updateEmptyState();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(getContext(), "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
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
