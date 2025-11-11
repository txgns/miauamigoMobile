package com.miaumigo.app.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miaumigo.app.models.Store;

import java.util.UUID;

public class StoreManager {
    private static StoreManager instance;
    private DatabaseReference databaseReference;

    private StoreManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static StoreManager getInstance() {
        if (instance == null) {
            instance = new StoreManager();
        }
        return instance;
    }

    /**
     * Cria ou atualiza uma loja
     */
    public void createOrUpdateStore(Store store) {
        if (store.getId() == null || store.getId().isEmpty()) {
            store.setId(UUID.randomUUID().toString());
        }
        store.setUpdatedAt(System.currentTimeMillis());
        databaseReference.child("stores").child(store.getId()).setValue(store);
        
        // Também salva referência por vendorId para busca rápida
        databaseReference.child("vendor_stores").child(store.getVendorId()).setValue(store.getId());
    }

    /**
     * Obtém a loja de um vendedor
     */
    public com.google.firebase.database.Query getStoreByVendorId(String vendorId) {
        return databaseReference.child("stores").orderByChild("vendorId").equalTo(vendorId)
                .limitToFirst(1);
    }

    /**
     * Atualiza a avaliação média da loja
     */
    public void updateStoreRating(String storeId, double newRating) {
        databaseReference.child("stores").child(storeId).child("rating").setValue(newRating);
        databaseReference.child("stores").child(storeId).child("updatedAt")
                .setValue(System.currentTimeMillis());
    }

    /**
     * Incrementa contador de vendas
     */
    public void incrementSales(String storeId) {
        databaseReference.child("stores").child(storeId)
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            com.google.firebase.database.MutableData mutableData) {
                        Store store = mutableData.getValue(Store.class);
                        if (store == null) {
                            return com.google.firebase.database.Transaction.success(mutableData);
                        }
                        store.setTotalSales(store.getTotalSales() + 1);
                        mutableData.setValue(store);
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(com.google.firebase.database.DatabaseError error,
                                         boolean committed, com.google.firebase.database.DataSnapshot currentData) {
                        // Handle completion
                    }
                });
    }

    /**
     * Incrementa contador de trocas
     */
    public void incrementTrades(String storeId) {
        databaseReference.child("stores").child(storeId)
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            com.google.firebase.database.MutableData mutableData) {
                        Store store = mutableData.getValue(Store.class);
                        if (store == null) {
                            return com.google.firebase.database.Transaction.success(mutableData);
                        }
                        store.setTotalTrades(store.getTotalTrades() + 1);
                        mutableData.setValue(store);
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(com.google.firebase.database.DatabaseError error,
                                         boolean committed, com.google.firebase.database.DataSnapshot currentData) {
                        // Handle completion
                    }
                });
    }
}

