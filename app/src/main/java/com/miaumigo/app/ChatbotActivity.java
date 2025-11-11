package com.miaumigo.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.adapters.ChatbotAdapter;
import com.miaumigo.app.models.ChatMessage;
import com.miaumigo.app.utils.PetshopChatbot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatbotAdapter chatAdapter;
    private PetshopChatbot chatbot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        initializeViews();
        setupChatbot();
        setupRecyclerView();
    }

    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupChatbot() {
        chatbot = new PetshopChatbot(this);
        addBotMessage(chatbot.getMainMenu());
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatbotAdapter();
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void sendMessage() {
        String userMessage = messageInput.getText().toString().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        addUserMessage(userMessage);
        messageInput.setText("");

        String botResponse = chatbot.processMessage(userMessage);
        addBotMessage(botResponse);
    }

    private void addUserMessage(String message) {
        ChatMessage userMessage = new ChatMessage(message, true, getCurrentTime());
        chatAdapter.addMessage(userMessage);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        new Handler().postDelayed(() -> {
            ChatMessage botMessage = new ChatMessage(message, false, getCurrentTime());
            chatAdapter.addMessage(botMessage);
            scrollToBottom();
        }, 400);
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}

