package com.miaumigo.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.MessageAdapter;
import com.miaumigo.app.models.Message;
import com.miaumigo.app.utils.ChatManager;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ProgressBar progressBar;
    
    private String chatId;
    private String otherVendorId;
    private String otherVendorName;
    private FirebaseUser currentUser;
    private ChatManager chatManager;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private com.google.firebase.database.ChildEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        getIntentData();
        setupRecyclerView();
        setupToolbar();
        loadMessages();
        setupSendButton();
    }

    private void initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        progressBar = findViewById(R.id.progressBar);
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatManager = ChatManager.getInstance();
        messageList = new ArrayList<>();
    }

    private void getIntentData() {
        chatId = getIntent().getStringExtra("chat_id");
        otherVendorId = getIntent().getStringExtra("other_vendor_id");
        otherVendorName = getIntent().getStringExtra("other_vendor_name");
        
        if (chatId == null && currentUser != null && otherVendorId != null) {
            chatId = chatManager.getOrCreateChatId(currentUser.getUid(), otherVendorId);
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messageList, currentUser != null ? currentUser.getUid() : "");
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(otherVendorName != null ? otherVendorName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSendButton() {
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString().trim();
        
        if (TextUtils.isEmpty(content)) {
            return;
        }
        
        if (currentUser == null || otherVendorId == null || chatId == null) {
            Toast.makeText(this, "Erro: dados incompletos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        chatManager.sendMessage(chatId, currentUser.getUid(), otherVendorId, content);
        editTextMessage.setText("");
    }

    private void loadMessages() {
        if (chatId == null) {
            Toast.makeText(this, "Erro: chat não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        
        DatabaseReference messagesReference = FirebaseDatabase.getInstance()
            .getReference("chats").child(chatId).child("messages");
        
        messagesListener = new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                }
                showLoading(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Atualiza mensagem se necessário
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Remove mensagem se necessário
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Move mensagem se necessário
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(ChatActivity.this, "Erro ao carregar mensagens: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        
        messagesReference.addChildEventListener(messagesListener);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null && chatId != null) {
            DatabaseReference messagesReference = FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("messages");
            messagesReference.removeEventListener(messagesListener);
        }
    }
}

