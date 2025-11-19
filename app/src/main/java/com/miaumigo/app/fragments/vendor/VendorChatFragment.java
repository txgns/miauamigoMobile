package com.miaumigo.app.fragments.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.ChatActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ChatAdapter;
import com.miaumigo.app.models.Chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VendorChatFragment extends Fragment {

    private RecyclerView recyclerViewChats;
    private ProgressBar progressBar;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private FirebaseUser currentUser;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        chatList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_vendor_chat, container, false);
            
            recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
            progressBar = view.findViewById(R.id.progressBar);
            
            setupRecyclerView();
            loadChats();
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("VendorChatFragment", "Erro ao criar view", e);
            e.printStackTrace();
            return new View(getContext());
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(chatList, chat -> {
            // Abre a tela de chat
            if (currentUserId == null) {
                Toast.makeText(getContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chat_id", chat.getId());
            String otherVendorId = chat.getOtherVendorId(currentUserId);
            String otherVendorName = chat.getOtherVendorName(currentUserId);
            intent.putExtra("other_vendor_id", otherVendorId != null ? otherVendorId : "");
            intent.putExtra("other_vendor_name", otherVendorName != null ? otherVendorName : "Vendedor");
            startActivity(intent);
        }, currentUserId);
        
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChats.setAdapter(chatAdapter);
    }

    private void loadChats() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        
        DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference("chats");
        
        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    if (chat != null && 
                        (chat.getVendor1Id().equals(currentUserId) || 
                         chat.getVendor2Id().equals(currentUserId))) {
                        chatList.add(chat);
                    }
                }
                
                // Ordena por última mensagem (mais recente primeiro)
                Collections.sort(chatList, (c1, c2) -> 
                    Long.compare(c2.getLastMessageTimestamp(), c1.getLastMessageTimestamp()));
                
                chatAdapter.notifyDataSetChanged();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(getContext(), "Erro ao carregar conversas: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
    }
}

