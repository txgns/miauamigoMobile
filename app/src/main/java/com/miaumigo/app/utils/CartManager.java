package com.miaumigo.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.miaumigo.app.models.CartItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartManager {
    private static final String PREFS_NAME = "cart_prefs";
    private static final String CART_ITEMS_KEY = "cart_items";
    private static CartManager instance;
    private Context context;
    private List<CartItem> cartItems;

    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        loadCartItems();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public void addToCart(CartItem item) {
        // Verifica se o item já existe no carrinho
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId().equals(item.getId())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                saveCartItems();
                return;
            }
        }
        
        // Se não existe, adiciona o novo item
        cartItems.add(item);
        saveCartItems();
    }

    public void removeFromCart(String itemId) {
        cartItems.removeIf(item -> item.getId().equals(itemId));
        saveCartItems();
    }

    public void updateQuantity(String itemId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getId().equals(itemId)) {
                if (quantity <= 0) {
                    removeFromCart(itemId);
                } else {
                    item.setQuantity(quantity);
                    saveCartItems();
                }
                return;
            }
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getTotalItems() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
        saveCartItems();
    }

    private void loadCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> cartItemsSet = prefs.getStringSet(CART_ITEMS_KEY, new HashSet<>());
        
        cartItems = new ArrayList<>();
        for (String itemString : cartItemsSet) {
            try {
                String[] parts = itemString.split("\\|");
                if (parts.length >= 5) {
                    CartItem item = new CartItem(
                        parts[0], // id
                        parts[1], // name
                        Double.parseDouble(parts[2]), // price
                        Integer.parseInt(parts[3]), // quantity
                        parts[4] // imageUrl
                    );
                    cartItems.add(item);
                }
            } catch (Exception e) {
                Log.e("CartManager", "Error parsing cart item: " + itemString, e);
            }
        }
    }

    private void saveCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> cartItemsSet = new HashSet<>();
        
        for (CartItem item : cartItems) {
            String itemString = item.getId() + "|" + 
                               item.getName() + "|" + 
                               item.getPrice() + "|" + 
                               item.getQuantity() + "|" + 
                               item.getImageUrl();
            cartItemsSet.add(itemString);
        }
        
        prefs.edit().putStringSet(CART_ITEMS_KEY, cartItemsSet).apply();
    }
}
