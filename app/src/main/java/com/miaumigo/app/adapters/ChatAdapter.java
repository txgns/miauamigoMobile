package com.miaumigo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.miaumigo.app.R;
import com.miaumigo.app.models.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener listener;
    private String currentUserId;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(List<Chat> chatList, OnChatClickListener listener, String currentUserId) {
        this.chatList = chatList;
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewAvatar;
        private TextView textViewVendorName;
        private TextView textViewLastMessage;
        private TextView textViewTimestamp;
        private View viewOnlineIndicator;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewVendorName = itemView.findViewById(R.id.textViewVendorName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            viewOnlineIndicator = itemView.findViewById(R.id.viewOnlineIndicator);
        }

        void bind(Chat chat) {
            // Determina qual vendedor mostrar (o outro, não o atual)
            String vendorName;
            String avatarUrl;
            boolean isOnline;
            
            if (currentUserId != null && currentUserId.equals(chat.getVendor1Id())) {
                // O usuário atual é vendor1, mostra vendor2
                vendorName = chat.getVendor2Name() != null ? chat.getVendor2Name() : "Vendedor";
                avatarUrl = chat.getVendor2Avatar();
                isOnline = chat.isVendor2Online();
            } else {
                // O usuário atual é vendor2, mostra vendor1
                vendorName = chat.getVendor1Name() != null ? chat.getVendor1Name() : "Vendedor";
                avatarUrl = chat.getVendor1Avatar();
                isOnline = chat.isVendor1Online();
            }
            
            textViewVendorName.setText(vendorName);
            textViewLastMessage.setText(chat.getLastMessage() != null ? 
                chat.getLastMessage() : "Nenhuma mensagem");
            
            // Formata timestamp
            if (chat.getLastMessageTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                textViewTimestamp.setText(sdf.format(new Date(chat.getLastMessageTimestamp())));
            } else {
                textViewTimestamp.setText("");
            }
            
            // Carrega avatar
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(avatarUrl).into(imageViewAvatar);
            } else {
                imageViewAvatar.setImageResource(R.drawable.ic_person);
            }
            
            // Mostra indicador online
            if (isOnline) {
                viewOnlineIndicator.setVisibility(View.VISIBLE);
            } else {
                viewOnlineIndicator.setVisibility(View.GONE);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatClick(chat);
                }
            });
        }
    }
}

