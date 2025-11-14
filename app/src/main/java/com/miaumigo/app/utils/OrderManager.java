package com.miaumigo.app.utils;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    private static final String TAG = "OrderManager";
    private static final String ORDERS_PATH = "orders";
    
    private DatabaseReference databaseReference;
    
    public OrderManager() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference(ORDERS_PATH);
    }
    
    /**
     * Cria um novo pedido no Firebase
     */
    public void createOrder(Order order, OnOrderCreatedListener listener) {
        if (order == null || order.getUserId() == null) {
            if (listener != null) {
                listener.onError("Pedido inválido");
            }
            return;
        }
        
        String orderId = databaseReference.push().getKey();
        if (orderId == null) {
            if (listener != null) {
                listener.onError("Erro ao gerar ID do pedido");
            }
            return;
        }
        
        order.setId(orderId);
        
        databaseReference.child(orderId)
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Pedido criado com sucesso: " + orderId);
                        if (listener != null) {
                            listener.onSuccess(order);
                        }
                    } else {
                        Log.e(TAG, "Erro ao criar pedido", task.getException());
                        if (listener != null) {
                            listener.onError(task.getException() != null ? 
                                task.getException().getMessage() : "Erro ao criar pedido");
                        }
                    }
                });
    }
    
    /**
     * Busca todos os pedidos de um usuário
     */
    public Query getOrdersByUser(String userId) {
        return databaseReference.orderByChild("userId").equalTo(userId);
    }
    
    /**
     * Busca um pedido específico por ID
     */
    public void getOrderById(String orderId, OnOrderLoadedListener listener) {
        databaseReference.child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Order order = snapshot.getValue(Order.class);
                            if (order != null) {
                                order.setId(snapshot.getKey());
                                if (listener != null) {
                                    listener.onOrderLoaded(order);
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("Erro ao carregar pedido");
                                }
                            }
                        } else {
                            if (listener != null) {
                                listener.onError("Pedido não encontrado");
                            }
                        }
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Erro ao buscar pedido", error.toException());
                        if (listener != null) {
                            listener.onError(error.getMessage());
                        }
                    }
                });
    }
    
    /**
     * Atualiza o status de um pedido
     */
    public void updateOrderStatus(String orderId, String newStatus, OnOrderUpdatedListener listener) {
        databaseReference.child(orderId).child("status").setValue(newStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        databaseReference.child(orderId).child("updatedAt")
                                .setValue(System.currentTimeMillis());
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    } else {
                        if (listener != null) {
                            listener.onError(task.getException() != null ? 
                                task.getException().getMessage() : "Erro ao atualizar pedido");
                        }
                    }
                });
    }
    
    /**
     * Interface para callback de criação de pedido
     */
    public interface OnOrderCreatedListener {
        void onSuccess(Order order);
        void onError(String error);
    }
    
    /**
     * Interface para callback de carregamento de pedido
     */
    public interface OnOrderLoadedListener {
        void onOrderLoaded(Order order);
        void onError(String error);
    }
    
    /**
     * Interface para callback de atualização de pedido
     */
    public interface OnOrderUpdatedListener {
        void onSuccess();
        void onError(String error);
    }
}

