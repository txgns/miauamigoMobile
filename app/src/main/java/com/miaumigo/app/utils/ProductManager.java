package com.miaumigo.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductManager {
    private static final String PREFS_NAME = "product_prefs";
    private static final String FAVORITES_KEY = "favorite_products";
    private static ProductManager instance;
    private Context context;
    private List<String> favoriteProductIds;

    private ProductManager(Context context) {
        this.context = context.getApplicationContext();
        loadFavorites();
    }

    public static synchronized ProductManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProductManager(context);
        }
        return instance;
    }

    public void addToFavorites(String productId) {
        if (!favoriteProductIds.contains(productId)) {
            favoriteProductIds.add(productId);
            saveFavorites();
        }
    }

    public void removeFromFavorites(String productId) {
        favoriteProductIds.remove(productId);
        saveFavorites();
    }

    public boolean isFavorite(String productId) {
        return favoriteProductIds.contains(productId);
    }

    public List<String> getFavoriteProductIds() {
        return new ArrayList<>(favoriteProductIds);
    }

    public void toggleFavorite(String productId) {
        if (isFavorite(productId)) {
            removeFromFavorites(productId);
        } else {
            addToFavorites(productId);
        }
    }

    private void loadFavorites() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favoritesSet = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        favoriteProductIds = new ArrayList<>(favoritesSet);
    }

    private void saveFavorites() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favoritesSet = new HashSet<>(favoriteProductIds);
        prefs.edit().putStringSet(FAVORITES_KEY, favoritesSet).apply();
    }
}