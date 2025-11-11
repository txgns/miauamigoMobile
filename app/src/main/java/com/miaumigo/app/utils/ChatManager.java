package com.miaumigo.app.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miaumigo.app.models.Chat;
import com.miaumigo.app.models.Message;

import java.util.UUID;

public class ChatManager {
    private static ChatManager instance;
    private DatabaseReference databaseReference;

    private ChatManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    /**
     * Cria ou retorna um chat existente entre dois vendedores
     */
    public String getOrCreateChatId(String vendor1Id, String vendor2Id) {
        if (vendor1Id == null || vendor2Id == null) {
            return "";
        }
        // Garante ordem consistente para evitar chats duplicados
        String chatId;
        if (vendor1Id.compareTo(vendor2Id) < 0) {
            chatId = vendor1Id + "_" + vendor2Id;
        } else {
            chatId = vendor2Id + "_" + vendor1Id;
        }
        return chatId;
    }

    /**
     * Envia uma mensagem
     */
    public void sendMessage(String chatId, String senderId, String receiverId, String content) {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(messageId, chatId, senderId, receiverId, content);
        
        // Salva a mensagem
        databaseReference.child("chats").child(chatId).child("messages").child(messageId)
                .setValue(message);
        
        // Atualiza informações do chat
        databaseReference.child("chats").child(chatId).child("lastMessage").setValue(content);
        databaseReference.child("chats").child(chatId).child("lastMessageTimestamp")
                .setValue(System.currentTimeMillis());
        databaseReference.child("chats").child(chatId).child("updatedAt")
                .setValue(System.currentTimeMillis());
    }

    /**
     * Cria um novo chat entre dois vendedores
     */
    public void createChat(String vendor1Id, String vendor2Id, String vendor1Name, 
                          String vendor2Name, String vendor1Avatar, String vendor2Avatar) {
        String chatId = getOrCreateChatId(vendor1Id, vendor2Id);
        
        Chat chat = new Chat(chatId, vendor1Id, vendor2Id);
        chat.setVendor1Name(vendor1Name);
        chat.setVendor2Name(vendor2Name);
        chat.setVendor1Avatar(vendor1Avatar);
        chat.setVendor2Avatar(vendor2Avatar);
        
        databaseReference.child("chats").child(chatId).setValue(chat);
    }

    /**
     * Marca mensagens como lidas
     */
    public void markMessagesAsRead(String chatId, String currentUserId) {
        databaseReference.child("chats").child(chatId).child("messages")
                .orderByChild("receiverId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        for (com.google.firebase.database.DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            messageSnapshot.getRef().child("isRead").setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        // Handle error
                    }
                });
    }

    /**
     * Atualiza status online/offline
     */
    public void updateOnlineStatus(String vendorId, boolean isOnline) {
        databaseReference.child("vendors").child(vendorId).child("isOnline").setValue(isOnline);
        databaseReference.child("vendors").child(vendorId).child("lastSeen")
                .setValue(System.currentTimeMillis());
    }
}

