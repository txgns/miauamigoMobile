package com.miaumigo.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_AUDIO_PERMISSION = 102;

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonAttach;
    private ImageButton buttonCamera;
    private ImageButton buttonGallery;
    private ImageButton buttonAudio;
    private LinearLayout layoutAudioRecording;
    private TextView textViewAudioDuration;
    private com.google.android.material.button.MaterialButton buttonStopAudio;
    private ProgressBar progressBar;
    
    private String chatId;
    private String otherVendorId;
    private String otherVendorName;
    private FirebaseUser currentUser;
    private ChatManager chatManager;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private com.google.firebase.database.ChildEventListener messagesListener;
    
    // Para áudio
    private com.miaumigo.app.utils.AudioRecorder audioRecorder;
    private Handler audioHandler;
    private Runnable audioUpdateRunnable;
    private long audioStartTime;
    private java.io.File audioFile;
    
    // Para upload de arquivos
    private com.google.firebase.storage.StorageReference storageReference;
    
    // ActivityResultLaunchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> fileLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        getIntentData();
        setupRecyclerView();
        setupToolbar();
        setupActivityResultLaunchers();
        loadMessages();
        setupButtons();
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatManager = ChatManager.getInstance();
        storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().getReference();
        audioRecorder = new com.miaumigo.app.utils.AudioRecorder(this);
        audioHandler = new Handler();
        messageList = new ArrayList<>();
    }

    private void initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonAttach = findViewById(R.id.buttonAttach);
        buttonCamera = findViewById(R.id.buttonCamera);
        buttonGallery = findViewById(R.id.buttonGallery);
        buttonAudio = findViewById(R.id.buttonAudio);
        layoutAudioRecording = findViewById(R.id.layoutAudioRecording);
        textViewAudioDuration = findViewById(R.id.textViewAudioDuration);
        buttonStopAudio = findViewById(R.id.buttonStopAudio);
        progressBar = findViewById(R.id.progressBar);
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatManager = ChatManager.getInstance();
        messageList = new ArrayList<>();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setupActivityResultLaunchers() {
        // Launcher para câmera
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && audioFile != null && audioFile.exists()) {
                    uploadImage(audioFile);
                }
            }
        );
        
        // Launcher para galeria
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadImageFromUri(imageUri);
                    }
                }
            }
        );
        
        // Launcher para arquivos
        fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadFile(fileUri);
                    }
                }
            }
        );
        
        // Launcher para permissões
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    Toast.makeText(this, "Permissões necessárias para usar esta funcionalidade", Toast.LENGTH_SHORT).show();
                }
            }
        );
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

    private void setupButtons() {
        buttonSend.setOnClickListener(v -> sendTextMessage());
        buttonAttach.setOnClickListener(v -> attachFile());
        buttonCamera.setOnClickListener(v -> takePhoto());
        buttonGallery.setOnClickListener(v -> chooseFromGallery());
        buttonAudio.setOnLongClickListener(v -> {
            startRecording();
            return true;
        });
        
        buttonAudio.setOnClickListener(v -> {
            // Ao soltar, para a gravação
            if (audioRecorder != null && audioRecorder.isRecording()) {
                stopRecording();
            }
        });
        buttonStopAudio.setOnClickListener(v -> cancelRecording());
    }
    
    private void attachFile() {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            fileLauncher.launch(intent);
        }
    }
    
    private void takePhoto() {
        if (checkCameraPermission()) {
            try {
                audioFile = createImageFile();
                if (audioFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        audioFile);
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cameraLauncher.launch(takePictureIntent);
                }
            } catch (java.io.IOException e) {
                Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void chooseFromGallery() {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        }
    }
    
    private java.io.File createImageFile() throws java.io.IOException {
        String imageFileName = "CHAT_" + System.currentTimeMillis();
        java.io.File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return java.io.File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            return false;
        }
        return true;
    }
    
    private boolean checkStoragePermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (!allGranted) {
            permissionLauncher.launch(permissions);
            return false;
        }
        return true;
    }
    
    private boolean checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO});
            return false;
        }
        return true;
    }
    
    private void startRecording() {
        if (!checkAudioPermission()) {
            return;
        }
        
        try {
            audioRecorder.startRecording();
            layoutAudioRecording.setVisibility(View.VISIBLE);
            audioStartTime = System.currentTimeMillis();
            startAudioTimer();
        } catch (java.io.IOException e) {
            Toast.makeText(this, "Erro ao iniciar gravação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopRecording() {
        if (!audioRecorder.isRecording()) {
            return;
        }
        
        audioRecorder.stopRecording();
        layoutAudioRecording.setVisibility(View.GONE);
        stopAudioTimer();
        
        String audioPath = audioRecorder.getOutputFile();
        if (audioPath != null && new java.io.File(audioPath).exists()) {
            long duration = audioRecorder.getDuration();
            uploadAudio(audioPath, duration);
        } else {
            Toast.makeText(this, "Erro: áudio não foi gravado", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void cancelRecording() {
        audioRecorder.cancelRecording();
        layoutAudioRecording.setVisibility(View.GONE);
        stopAudioTimer();
    }
    
    private void startAudioTimer() {
        audioUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (audioRecorder.isRecording()) {
                    long elapsed = (System.currentTimeMillis() - audioStartTime) / 1000;
                    long minutes = elapsed / 60;
                    long seconds = elapsed % 60;
                    textViewAudioDuration.setText(String.format("%02d:%02d", minutes, seconds));
                    audioHandler.postDelayed(this, 1000);
                }
            }
        };
        audioHandler.post(audioUpdateRunnable);
    }
    
    private void stopAudioTimer() {
        if (audioUpdateRunnable != null) {
            audioHandler.removeCallbacks(audioUpdateRunnable);
        }
        textViewAudioDuration.setText("00:00");
    }

    private void sendTextMessage() {
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
    
    private void uploadImage(java.io.File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, "Erro: arquivo de imagem não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        String fileName = "chat_images/" + chatId + "/" + System.currentTimeMillis() + ".jpg";
        com.google.firebase.storage.StorageReference imageRef = storageReference.child(fileName);
        
        try {
            java.io.FileInputStream stream = new java.io.FileInputStream(imageFile);
            com.google.firebase.storage.UploadTask uploadTask = imageRef.putStream(stream);
            
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    chatManager.sendMessageWithAttachment(chatId, currentUser.getUid(), otherVendorId,
                        uri.toString(), "image", Message.MessageType.IMAGE, null);
                    showLoading(false);
                }).addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Erro ao obter URL da imagem", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Erro ao fazer upload da imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } catch (java.io.FileNotFoundException e) {
            showLoading(false);
            Toast.makeText(this, "Erro ao ler arquivo de imagem", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uploadImageFromUri(Uri imageUri) {
        showLoading(true);
        String fileName = "chat_images/" + chatId + "/" + System.currentTimeMillis() + ".jpg";
        com.google.firebase.storage.StorageReference imageRef = storageReference.child(fileName);
        
        com.google.firebase.storage.UploadTask uploadTask = imageRef.putFile(imageUri);
        
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                chatManager.sendMessageWithAttachment(chatId, currentUser.getUid(), otherVendorId,
                    uri.toString(), "image", Message.MessageType.IMAGE, null);
                showLoading(false);
            }).addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Erro ao obter URL da imagem", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            showLoading(false);
            Toast.makeText(this, "Erro ao fazer upload da imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    
    private void uploadAudio(String audioPath, long duration) {
        showLoading(true);
        java.io.File audioFile = new java.io.File(audioPath);
        if (!audioFile.exists()) {
            showLoading(false);
            Toast.makeText(this, "Erro: arquivo de áudio não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String fileName = "chat_audio/" + chatId + "/" + System.currentTimeMillis() + ".m4a";
        com.google.firebase.storage.StorageReference audioRef = storageReference.child(fileName);
        
        try {
            java.io.FileInputStream stream = new java.io.FileInputStream(audioFile);
            com.google.firebase.storage.UploadTask uploadTask = audioRef.putStream(stream);
            
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    chatManager.sendAudioMessage(chatId, currentUser.getUid(), otherVendorId,
                        uri.toString(), duration);
                    showLoading(false);
                }).addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Erro ao obter URL do áudio", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Erro ao fazer upload do áudio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } catch (java.io.FileNotFoundException e) {
            showLoading(false);
            Toast.makeText(this, "Erro ao ler arquivo de áudio", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uploadFile(Uri fileUri) {
        showLoading(true);
        
        // Obtém nome do arquivo
        String fileName = getFileName(fileUri);
        if (fileName == null || fileName.isEmpty()) {
            fileName = "arquivo_" + System.currentTimeMillis();
        }
        
        // Determina tipo de arquivo
        String mimeType = getContentResolver().getType(fileUri);
        Message.MessageType messageType = Message.MessageType.FILE;
        if (mimeType != null) {
            if (mimeType.contains("pdf")) {
                messageType = Message.MessageType.PDF;
            } else if (mimeType.startsWith("image")) {
                messageType = Message.MessageType.IMAGE;
            }
        }
        
        // Cria cópias finais para usar na lambda
        final String finalMimeType = mimeType != null ? mimeType : "file";
        final Message.MessageType finalMessageType = messageType;
        final String finalFileName = fileName;
        
        String storagePath = "chat_files/" + chatId + "/" + System.currentTimeMillis() + "_" + fileName;
        com.google.firebase.storage.StorageReference fileRef = storageReference.child(storagePath);
        
        com.google.firebase.storage.UploadTask uploadTask = fileRef.putFile(fileUri);
        
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                chatManager.sendMessageWithAttachment(chatId, currentUser.getUid(), otherVendorId,
                    uri.toString(), finalMimeType, finalMessageType, finalFileName);
                showLoading(false);
            }).addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Erro ao obter URL do arquivo", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            showLoading(false);
            Toast.makeText(this, "Erro ao fazer upload do arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1 && result != null) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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
        
        // Para gravação de áudio se estiver ativa
        if (audioRecorder != null && audioRecorder.isRecording()) {
            audioRecorder.cancelRecording();
        }
        stopAudioTimer();
        
        // Remove listener de mensagens
        if (messagesListener != null && chatId != null) {
            DatabaseReference messagesReference = FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("messages");
            messagesReference.removeEventListener(messagesListener);
        }
    }
}

