package com.miaumigo.app.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miaumigo.app.models.Announcement;

import java.util.UUID;

public class AnnouncementManager {
    private static AnnouncementManager instance;
    private DatabaseReference databaseReference;

    private AnnouncementManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static AnnouncementManager getInstance() {
        if (instance == null) {
            instance = new AnnouncementManager();
        }
        return instance;
    }

    /**
     * Cria um novo anúncio
     */
    public void createAnnouncement(Announcement announcement) {
        if (announcement.getId() == null || announcement.getId().isEmpty()) {
            announcement.setId(UUID.randomUUID().toString());
        }
        announcement.setCreatedAt(System.currentTimeMillis());
        announcement.setUpdatedAt(System.currentTimeMillis());
        
        databaseReference.child("announcements").child(announcement.getId()).setValue(announcement);
        
        // Também salva referência por vendorId para busca rápida
        databaseReference.child("vendor_announcements").child(announcement.getVendorId())
                .child(announcement.getId()).setValue(true);
    }

    /**
     * Atualiza um anúncio existente
     */
    public void updateAnnouncement(Announcement announcement) {
        announcement.setUpdatedAt(System.currentTimeMillis());
        databaseReference.child("announcements").child(announcement.getId()).setValue(announcement);
    }

    /**
     * Remove um anúncio
     */
    public void deleteAnnouncement(String announcementId, String vendorId) {
        databaseReference.child("announcements").child(announcementId).removeValue();
        databaseReference.child("vendor_announcements").child(vendorId).child(announcementId)
                .removeValue();
    }

    /**
     * Obtém todos os anúncios de um vendedor
     */
    public com.google.firebase.database.Query getAnnouncementsByVendor(String vendorId) {
        return databaseReference.child("announcements").orderByChild("vendorId").equalTo(vendorId);
    }

    /**
     * Obtém todos os anúncios públicos (para vendedores)
     */
    public com.google.firebase.database.Query getAllAnnouncements() {
        return databaseReference.child("announcements").orderByChild("createdAt");
    }

    /**
     * Filtra anúncios por tipo
     */
    public com.google.firebase.database.Query getAnnouncementsByType(Announcement.AnnouncementType type) {
        return databaseReference.child("announcements").orderByChild("type").equalTo(type.name());
    }

    /**
     * Filtra anúncios por categoria
     */
    public com.google.firebase.database.Query getAnnouncementsByCategory(String category) {
        return databaseReference.child("announcements").orderByChild("category").equalTo(category);
    }
}

