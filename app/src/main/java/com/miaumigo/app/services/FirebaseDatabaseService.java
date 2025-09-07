package com.miaumigo.app.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.models.Order;
import com.miaumigo.app.models.Product;
import com.miaumigo.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseService {
    private DatabaseReference mDatabase;
    private Context context;

    public FirebaseDatabaseService(Context context) {
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public interface ListCallback<T> {
        void onSuccess(List<T> data);
        void onError(String error);
    }

    // User operations
    public void getUser(String userId, DataCallback<User> callback) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("Usuário não encontrado");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void updateUser(User user, DataCallback<Void> callback) {
        user.setUpdatedAt(System.currentTimeMillis());
        mDatabase.child("users").child(user.getUid()).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao atualizar usuário");
                    }
                });
    }

    // Product operations
    public void getProducts(ListCallback<Product> callback) {
        mDatabase.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> products = new ArrayList<>();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setId(productSnapshot.getKey());
                        products.add(product);
                    }
                }
                callback.onSuccess(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getProduct(String productId, DataCallback<Product> callback) {
        mDatabase.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    product.setId(snapshot.getKey());
                    callback.onSuccess(product);
                } else {
                    callback.onError("Produto não encontrado");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getProductsByCategory(String category, ListCallback<Product> callback) {
        mDatabase.child("products").orderByChild("category").equalTo(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Product> products = new ArrayList<>();
                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            Product product = productSnapshot.getValue(Product.class);
                            if (product != null) {
                                product.setId(productSnapshot.getKey());
                                products.add(product);
                            }
                        }
                        callback.onSuccess(products);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // Cart operations
    public void addToCart(String userId, CartItem cartItem, DataCallback<Void> callback) {
        mDatabase.child("carts").child(userId).child(cartItem.getProductId()).setValue(cartItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao adicionar ao carrinho");
                    }
                });
    }

    public void getCart(String userId, ListCallback<CartItem> callback) {
        mDatabase.child("carts").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CartItem> cartItems = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItems.add(cartItem);
                    }
                }
                callback.onSuccess(cartItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void updateCartItem(String userId, String productId, int quantity, DataCallback<Void> callback) {
        if (quantity <= 0) {
            removeFromCart(userId, productId, callback);
        } else {
            mDatabase.child("carts").child(userId).child(productId).child("quantity").setValue(quantity)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError("Erro ao atualizar carrinho");
                        }
                    });
        }
    }

    public void removeFromCart(String userId, String productId, DataCallback<Void> callback) {
        mDatabase.child("carts").child(userId).child(productId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao remover do carrinho");
                    }
                });
    }

    public void clearCart(String userId, DataCallback<Void> callback) {
        mDatabase.child("carts").child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao limpar carrinho");
                    }
                });
    }

    // Order operations
    public void createOrder(Order order, DataCallback<Void> callback) {
        String orderId = mDatabase.child("orders").push().getKey();
        order.setId(orderId);
        mDatabase.child("orders").child(orderId).setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao criar pedido");
                    }
                });
    }

    public void getOrders(String userId, ListCallback<Order> callback) {
        mDatabase.child("orders").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> orders = new ArrayList<>();
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                order.setId(orderSnapshot.getKey());
                                orders.add(order);
                            }
                        }
                        callback.onSuccess(orders);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void updateOrderStatus(String orderId, String status, DataCallback<Void> callback) {
        mDatabase.child("orders").child(orderId).child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao atualizar status do pedido");
                    }
                });
    }
}

