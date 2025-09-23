package com.miaumigo.app.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.models.Address;
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
        try {
            mDatabase.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Product> products = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            try {
                                Product product = productSnapshot.getValue(Product.class);
                                if (product != null) {
                                    product.setId(productSnapshot.getKey());
                                    products.add(product);
                                }
                            } catch (Exception e) {
                                // Skip invalid products
                                continue;
                            }
                        }
                    }
                    callback.onSuccess(products);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Erro ao carregar produtos");
                }
            });
        } catch (Exception e) {
            callback.onError("Erro de conexão");
        }
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

    // Address operations
    public void createAddress(Address address, DataCallback<Void> callback) {
        String addressId = mDatabase.child("addresses").push().getKey();
        address.setId(addressId);
        address.setUpdatedAt(System.currentTimeMillis());
        mDatabase.child("addresses").child(addressId).setValue(address)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao criar endereço");
                    }
                });
    }

    public void getUserAddresses(String userId, ListCallback<Address> callback) {
        mDatabase.child("addresses").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Address> addresses = new ArrayList<>();
                        for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                            Address address = addressSnapshot.getValue(Address.class);
                            if (address != null) {
                                address.setId(addressSnapshot.getKey());
                                addresses.add(address);
                            }
                        }
                        callback.onSuccess(addresses);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void updateAddress(Address address, DataCallback<Void> callback) {
        address.setUpdatedAt(System.currentTimeMillis());
        mDatabase.child("addresses").child(address.getId()).setValue(address)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao atualizar endereço");
                    }
                });
    }

    public void deleteAddress(String addressId, DataCallback<Void> callback) {
        mDatabase.child("addresses").child(addressId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Erro ao deletar endereço");
                    }
                });
    }

    public void setDefaultAddress(String userId, String addressId, DataCallback<Void> callback) {
        // First, remove default from all addresses
        mDatabase.child("addresses").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                            mDatabase.child("addresses").child(addressSnapshot.getKey())
                                    .child("isDefault").setValue(false);
                        }
                        
                        // Then set the selected address as default
                        mDatabase.child("addresses").child(addressId).child("isDefault").setValue(true)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        callback.onSuccess(null);
                                    } else {
                                        callback.onError("Erro ao definir endereço padrão");
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}

