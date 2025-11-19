package com.miaumigo.app.adapters;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.miaumigo.app.R;
import com.miaumigo.app.models.Message;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = viewType == 0 ? R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        // 0 = mensagem enviada, 1 = mensagem recebida
        return message.getSenderId().equals(currentUserId) ? 0 : 1;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTimestamp;
        private ImageView imageViewAttachment;
        private MaterialButton buttonPlayAudio;
        private TextView textViewAudioDuration;
        private TextView textViewFileName;
        private ProgressBar progressBar;
        private MediaPlayer mediaPlayer;
        private boolean isPlaying = false;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            imageViewAttachment = itemView.findViewById(R.id.imageViewAttachment);
            buttonPlayAudio = itemView.findViewById(R.id.buttonPlayAudio);
            textViewAudioDuration = itemView.findViewById(R.id.textViewAudioDuration);
            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        void bind(Message message) {
            // Esconde todos os elementos primeiro
            if (textViewMessage != null) {
                textViewMessage.setVisibility(View.GONE);
            }
            if (imageViewAttachment != null) {
                imageViewAttachment.setVisibility(View.GONE);
            }
            if (buttonPlayAudio != null) {
                buttonPlayAudio.setVisibility(View.GONE);
            }
            if (textViewFileName != null) {
                textViewFileName.setVisibility(View.GONE);
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            
            // Exibe baseado no tipo
            switch (message.getType()) {
                case TEXT:
                    if (textViewMessage != null) {
                        textViewMessage.setVisibility(View.VISIBLE);
                        textViewMessage.setText(message.getContent());
                    }
                    break;
                    
                case IMAGE:
                    if (imageViewAttachment != null && message.getAttachmentUrl() != null) {
                        imageViewAttachment.setVisibility(View.VISIBLE);
                        Glide.with(itemView.getContext())
                            .load(message.getAttachmentUrl())
                            .placeholder(R.drawable.ic_product_placeholder)
                            .into(imageViewAttachment);
                        
                        imageViewAttachment.setOnClickListener(v -> {
                            // Abrir imagem em tela cheia (implementar se necessário)
                        });
                    }
                    if (textViewMessage != null && message.getContent() != null && !message.getContent().isEmpty()) {
                        textViewMessage.setVisibility(View.VISIBLE);
                        textViewMessage.setText(message.getContent());
                    }
                    break;
                    
                case AUDIO:
                    if (buttonPlayAudio != null) {
                        buttonPlayAudio.setVisibility(View.VISIBLE);
                        buttonPlayAudio.setOnClickListener(v -> toggleAudioPlayback(message.getAttachmentUrl()));
                    }
                    if (textViewAudioDuration != null) {
                        textViewAudioDuration.setVisibility(View.VISIBLE);
                        long duration = message.getAudioDuration() > 0 ? message.getAudioDuration() : 0;
                        long minutes = duration / 60;
                        long seconds = duration % 60;
                        textViewAudioDuration.setText(String.format("%02d:%02d", minutes, seconds));
                    }
                    break;
                    
                case FILE:
                case PDF:
                    if (textViewFileName != null) {
                        textViewFileName.setVisibility(View.VISIBLE);
                        String fileName = message.getContent() != null && !message.getContent().isEmpty() 
                            ? message.getContent() : "Arquivo";
                        textViewFileName.setText("📎 " + fileName);
                        
                        textViewFileName.setOnClickListener(v -> {
                            // Abrir arquivo (implementar se necessário)
                            Toast.makeText(itemView.getContext(), "Download de arquivo: " + fileName, Toast.LENGTH_SHORT).show();
                        });
                    }
                    break;
            }
            
            // Timestamp
            if (textViewTimestamp != null) {
                if (message.getTimestamp() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    textViewTimestamp.setText(sdf.format(new Date(message.getTimestamp())));
                } else {
                    textViewTimestamp.setText("");
                }
            }
        }
        
        private void toggleAudioPlayback(String audioUrl) {
            if (audioUrl == null || audioUrl.isEmpty()) {
                return;
            }
            
            if (isPlaying && mediaPlayer != null) {
                // Para a reprodução
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
                if (buttonPlayAudio != null) {
                    buttonPlayAudio.setText("▶");
                }
            } else {
                // Inicia a reprodução
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        isPlaying = true;
                        if (buttonPlayAudio != null) {
                            buttonPlayAudio.setText("⏸");
                        }
                    });
                    mediaPlayer.setOnCompletionListener(mp -> {
                        mp.release();
                        mediaPlayer = null;
                        isPlaying = false;
                        if (buttonPlayAudio != null) {
                            buttonPlayAudio.setText("▶");
                        }
                    });
                } catch (IOException e) {
                    Toast.makeText(itemView.getContext(), "Erro ao reproduzir áudio", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

