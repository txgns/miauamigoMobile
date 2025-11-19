package com.miaumigo.app;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.R;
import com.miaumigo.app.models.Announcement;
import com.miaumigo.app.models.User;
import com.miaumigo.app.utils.AnnouncementManager;
import com.miaumigo.app.utils.EncryptionManager;

public class CreateAnnouncementActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText editTextProductName;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private Spinner spinnerType;
    private Spinner spinnerCondition;
    private Button buttonCreate;
    private ProgressBar progressBar;
    private ImageView imageViewProduct;
    private com.google.android.material.button.MaterialButton buttonTakePhoto;
    private com.google.android.material.button.MaterialButton buttonChoosePhoto;
    
    private FirebaseUser currentUser;
    private AnnouncementManager announcementManager;
    private DatabaseReference userReference;
    private DatabaseReference announcementReference;
    private String announcementId;
    private boolean isEditMode;
    private Announcement currentAnnouncement;
    private Uri imageUri;
    private java.io.File photoFile;
    private com.google.firebase.storage.StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        announcementId = getIntent().getStringExtra("announcement_id");
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        
        initViews();
        setupToolbar();
        setupSpinners();
        setupButtons();
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        announcementManager = AnnouncementManager.getInstance();
        
        // Inicializa Firebase Storage - usa a instância padrão configurada no google-services.json
        com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        // Tenta obter o bucket do google-services.json, se não conseguir, usa o padrão
        try {
            String bucketUrl = "gs://miaumigo-686d4.firebasestorage.app";
            storage = com.google.firebase.storage.FirebaseStorage.getInstance(bucketUrl);
            android.util.Log.d("CreateAnnouncement", "Firebase Storage inicializado com bucket: " + bucketUrl);
        } catch (Exception e) {
            android.util.Log.w("CreateAnnouncement", "Usando storage padrão do google-services.json", e);
            storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        }
        storageReference = storage.getReference();
        
        if (isEditMode) {
            loadAnnouncementData();
        }
    }

    private void initViews() {
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        buttonCreate = findViewById(R.id.buttonCreate);
        progressBar = findViewById(R.id.progressBar);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        buttonChoosePhoto = findViewById(R.id.buttonChoosePhoto);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Editar Anúncio" : "Criar Anúncio");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSpinners() {
        // Tipo de anúncio
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
            R.array.announcement_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        
        // Condição
        ArrayAdapter<CharSequence> conditionAdapter = ArrayAdapter.createFromResource(this,
            R.array.product_conditions, android.R.layout.simple_spinner_item);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupButtons() {
        buttonCreate.setText(isEditMode ? "Salvar Alterações" : "Criar Anúncio");
        buttonCreate.setOnClickListener(v -> saveAnnouncement());
        if (buttonTakePhoto != null) {
            buttonTakePhoto.setOnClickListener(v -> takePhoto());
        }
        if (buttonChoosePhoto != null) {
            buttonChoosePhoto.setOnClickListener(v -> choosePhoto());
        }
    }

    private void takePhoto() {
        if (checkCameraPermission()) {
            dispatchTakePictureIntent();
        }
    }

    private void choosePhoto() {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            
            // Fallback para ACTION_PICK se ACTION_OPEN_DOCUMENT não estiver disponível
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            
            Intent chooserIntent = Intent.createChooser(intent, "Selecione uma imagem");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
            startActivityForResult(chooserIntent, REQUEST_IMAGE_PICK);
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                dispatchTakePictureIntent();
            } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
                choosePhoto();
            }
        } else {
            Toast.makeText(this, "Permissão necessária para usar esta funcionalidade", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (java.io.IOException ex) {
                Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private java.io.File createImageFile() throws java.io.IOException {
        String imageFileName = "ANNOUNCEMENT_" + java.util.UUID.randomUUID().toString();
        java.io.File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        java.io.File image = java.io.File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoFile != null && photoFile.exists() && photoFile.length() > 0) {
                    // Para a câmera, usa o FileProvider URI se disponível, senão file://
                    try {
                        imageUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);
                    } catch (Exception e) {
                        // Fallback para file:// se FileProvider falhar
                        android.util.Log.w("CreateAnnouncement", "Usando URI file:// diretamente", e);
                        imageUri = Uri.fromFile(photoFile);
                    }
                    
                    if (imageUri != null) {
                        android.util.Log.d("CreateAnnouncement", "Imagem capturada: " + imageUri.toString() + " (tamanho: " + photoFile.length() + " bytes)");
                        if (imageViewProduct != null) {
                            com.bumptech.glide.Glide.with(this).load(imageUri).into(imageViewProduct);
                        }
                    } else {
                        Toast.makeText(this, "Erro: não foi possível obter URI da imagem", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Erro: foto não foi capturada corretamente";
                    if (photoFile != null) {
                        errorMsg += " (arquivo existe: " + photoFile.exists() + ", tamanho: " + (photoFile.exists() ? photoFile.length() : 0) + ")";
                    }
                    android.util.Log.e("CreateAnnouncement", errorMsg);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    // Concede permissão de leitura persistente
                    try {
                        getContentResolver().takePersistableUriPermission(imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        android.util.Log.w("CreateAnnouncement", "Não foi possível conceder permissão persistente", e);
                    }
                    
                    if (imageViewProduct != null) {
                        com.bumptech.glide.Glide.with(this).load(imageUri).into(imageViewProduct);
                    }
                }
            }
        }
    }

    private void loadAnnouncementData() {
        if (announcementId == null || announcementId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do anúncio não fornecido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        announcementReference = FirebaseDatabase.getInstance()
            .getReference("announcements").child(announcementId);
        
        announcementReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                if (snapshot.exists()) {
                    currentAnnouncement = snapshot.getValue(Announcement.class);
                    if (currentAnnouncement != null) {
                        displayAnnouncementData(currentAnnouncement);
                    }
                } else {
                    Toast.makeText(CreateAnnouncementActivity.this, 
                        "Anúncio não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(CreateAnnouncementActivity.this, 
                    "Erro ao carregar anúncio: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAnnouncementData(Announcement announcement) {
        editTextProductName.setText(announcement.getProductName());
        editTextDescription.setText(announcement.getDescription());
        editTextPrice.setText(String.valueOf(announcement.getSuggestedPrice()));
        
        // Seleciona tipo no spinner
        for (int i = 0; i < spinnerType.getCount(); i++) {
            String typeStr = spinnerType.getItemAtPosition(i).toString();
            if ((announcement.getType() == Announcement.AnnouncementType.SALE && typeStr.equals("Venda")) ||
                (announcement.getType() == Announcement.AnnouncementType.TRADE && typeStr.equals("Troca")) ||
                (announcement.getType() == Announcement.AnnouncementType.REQUEST && typeStr.equals("Busca"))) {
                spinnerType.setSelection(i);
                break;
            }
        }
        
        // Seleciona condição no spinner
        if (announcement.getCondition() != null) {
            for (int i = 0; i < spinnerCondition.getCount(); i++) {
                if (spinnerCondition.getItemAtPosition(i).toString().equals(announcement.getCondition())) {
                    spinnerCondition.setSelection(i);
                    break;
                }
            }
        }
        
        // Carrega imagem
        if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty() && imageViewProduct != null) {
            com.bumptech.glide.Glide.with(this).load(announcement.getImageUrl())
                .placeholder(R.drawable.ic_product_placeholder)
                .into(imageViewProduct);
        }
    }

    private void saveAnnouncement() {
        String productName = editTextProductName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        
        if (TextUtils.isEmpty(productName)) {
            editTextProductName.setError("Nome do produto é obrigatório");
            return;
        }
        
        if (TextUtils.isEmpty(description)) {
            editTextDescription.setError("Descrição é obrigatória");
            return;
        }
        
        double price = 0.0;
        if (!TextUtils.isEmpty(priceStr)) {
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                editTextPrice.setError("Preço inválido");
                return;
            }
        }
        
        if (currentUser == null) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        final double finalPrice = price;
        final String finalProductName = productName;
        final String finalDescription = description;
        
        // Busca dados do vendedor
        userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    showLoading(false);
                    Toast.makeText(CreateAnnouncementActivity.this, 
                        "Erro ao buscar dados do usuário", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                EncryptionManager encryptionManager = EncryptionManager.getInstance(getApplicationContext());
                user.setName(encryptionManager.decrypt(user.getName()));
                user.setPhone(encryptionManager.decrypt(user.getPhone()));

                Announcement announcement;
                if (isEditMode && currentAnnouncement != null) {
                    announcement = currentAnnouncement;
                } else {
                    announcement = new Announcement();
                    announcement.setId(java.util.UUID.randomUUID().toString());
                    announcement.setVendorId(currentUser.getUid());
                    announcement.setCreatedAt(System.currentTimeMillis());
                }
                
                announcement.setVendorName(user.getName());
                announcement.setVendorAvatar(user.getAvatarUrl());
                announcement.setProductName(finalProductName);
                announcement.setDescription(finalDescription);
                announcement.setSuggestedPrice(finalPrice);
                announcement.setCondition(spinnerCondition.getSelectedItem().toString());
                
                // Tipo
                String typeStr = spinnerType.getSelectedItem().toString();
                if (typeStr.equals("Venda")) {
                    announcement.setType(Announcement.AnnouncementType.SALE);
                } else if (typeStr.equals("Troca")) {
                    announcement.setType(Announcement.AnnouncementType.TRADE);
                } else {
                    announcement.setType(Announcement.AnnouncementType.REQUEST);
                }
                
                announcement.setUpdatedAt(System.currentTimeMillis());
                
                // Upload da imagem primeiro, se houver nova
                if (imageUri != null) {
                    uploadImageAndSaveAnnouncement(announcement);
                } else {
                    saveAnnouncementToDatabase(announcement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(CreateAnnouncementActivity.this, 
                    "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageAndSaveAnnouncement(Announcement announcement) {
        if (imageUri == null) {
            saveAnnouncementToDatabase(announcement);
            return;
        }

        if (currentUser == null) {
            showLoading(false);
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria o caminho da imagem: announcements/{vendorId}/{announcementId}.jpg
        String imageFileName = "announcements/" + currentUser.getUid() + "/" + announcement.getId() + "_" + System.currentTimeMillis() + ".jpg";
        com.google.firebase.storage.StorageReference imageRef = storageReference.child(imageFileName);
        
        android.util.Log.d("CreateAnnouncement", "Iniciando upload de imagem: " + imageFileName);
        android.util.Log.d("CreateAnnouncement", "URI: " + imageUri.toString());
        android.util.Log.d("CreateAnnouncement", "Scheme: " + imageUri.getScheme());
        
        try {
            com.google.firebase.storage.UploadTask uploadTask;
            
            // Verifica o tipo de URI e trata adequadamente
            String scheme = imageUri.getScheme();
            
            // Valida e trata diferentes tipos de URI
            if (scheme == null) {
                showLoading(false);
                Toast.makeText(this, "Erro: URI inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (scheme.equals("file")) {
                // URI file:// - converter para InputStream
                String path = imageUri.getPath();
                if (path == null) {
                    showLoading(false);
                    Toast.makeText(this, "Erro: caminho do arquivo inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                java.io.File file = new java.io.File(path);
                if (!file.exists()) {
                    showLoading(false);
                    android.util.Log.e("CreateAnnouncement", "Arquivo não existe: " + path);
                    Toast.makeText(this, "Erro: arquivo de imagem não encontrado. Tente selecionar novamente.", Toast.LENGTH_LONG).show();
                    return;
                }
                
                if (file.length() == 0) {
                    showLoading(false);
                    Toast.makeText(this, "Erro: arquivo de imagem está vazio", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                android.util.Log.d("CreateAnnouncement", "Arquivo encontrado: " + path + " (tamanho: " + file.length() + " bytes)");
                
                // Usa putStream para arquivos locais (file://)
                java.io.FileInputStream stream = new java.io.FileInputStream(file);
                uploadTask = imageRef.putStream(stream);
            } else if (scheme.equals("content")) {
                // URI content:// (da galeria ou FileProvider) - usa putFile
                // Concede permissão persistente se possível
                try {
                    getContentResolver().takePersistableUriPermission(imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    // Tenta conceder permissão temporária
                    try {
                        grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e2) {
                        android.util.Log.w("CreateAnnouncement", "Não foi possível conceder permissão", e2);
                    }
                }
                
                // Verifica se o arquivo existe através do ContentResolver
                try {
                    android.content.ContentResolver resolver = getContentResolver();
                    java.io.InputStream testStream = resolver.openInputStream(imageUri);
                    if (testStream == null) {
                        showLoading(false);
                        Toast.makeText(this, "Erro: não foi possível acessar a imagem. Tente selecionar novamente.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    testStream.close();
                } catch (Exception e) {
                    showLoading(false);
                    android.util.Log.e("CreateAnnouncement", "Erro ao verificar URI: " + imageUri.toString(), e);
                    Toast.makeText(this, "Erro: não foi possível acessar a imagem. Tente selecionar novamente.", Toast.LENGTH_LONG).show();
                    return;
                }
                
                uploadTask = imageRef.putFile(imageUri);
            } else {
                showLoading(false);
                Toast.makeText(this, "Erro: tipo de URI não suportado: " + scheme, Toast.LENGTH_SHORT).show();
                return;
            }
            
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                android.util.Log.d("CreateAnnouncement", "Upload concluído com sucesso");
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    android.util.Log.d("CreateAnnouncement", "URL da imagem obtida: " + imageUrl);
                    announcement.setImageUrl(imageUrl);
                    saveAnnouncementToDatabase(announcement);
                }).addOnFailureListener(e -> {
                    showLoading(false);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
                    android.util.Log.e("CreateAnnouncement", "Erro ao obter URL: " + errorMsg, e);
                    Toast.makeText(this, "Erro ao obter URL da imagem: " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }).addOnFailureListener(e -> {
                showLoading(false);
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
                android.util.Log.e("CreateAnnouncement", "Erro no upload: " + errorMsg, e);
                
                // Mensagem de erro mais amigável
                if (errorMsg.contains("Object does not exist")) {
                    Toast.makeText(this, "Erro: arquivo de imagem não encontrado. Tente selecionar novamente.", Toast.LENGTH_LONG).show();
                } else if (errorMsg.contains("Permission denied")) {
                    Toast.makeText(this, "Erro: permissão negada. Verifique as permissões do app.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Erro ao fazer upload: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(taskSnapshot -> {
                // Mostrar progresso se necessário
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                android.util.Log.d("CreateAnnouncement", "Upload progress: " + progress + "%");
            });
        } catch (java.io.FileNotFoundException e) {
            showLoading(false);
            android.util.Log.e("CreateAnnouncement", "Arquivo não encontrado", e);
            Toast.makeText(this, "Erro: arquivo de imagem não encontrado", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            showLoading(false);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
            android.util.Log.e("CreateAnnouncement", "Erro ao processar imagem: " + errorMsg, e);
            Toast.makeText(this, "Erro ao processar imagem: " + errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void saveAnnouncementToDatabase(Announcement announcement) {
        android.util.Log.d("CreateAnnouncement", "Salvando anúncio no banco de dados");
        android.util.Log.d("CreateAnnouncement", "ID: " + announcement.getId());
        android.util.Log.d("CreateAnnouncement", "Nome: " + announcement.getProductName());
        android.util.Log.d("CreateAnnouncement", "ImageUrl: " + announcement.getImageUrl());
        
        if (isEditMode) {
            announcementManager.updateAnnouncement(announcement);
            showLoading(false);
            android.util.Log.d("CreateAnnouncement", "Anúncio atualizado com sucesso!");
            Toast.makeText(this, "Anúncio atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            announcementManager.createAnnouncement(announcement);
            showLoading(false);
            android.util.Log.d("CreateAnnouncement", "Anúncio criado com sucesso!");
            Toast.makeText(this, "Anúncio criado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonCreate.setEnabled(!show);
        if (buttonTakePhoto != null) {
            buttonTakePhoto.setEnabled(!show);
        }
        if (buttonChoosePhoto != null) {
            buttonChoosePhoto.setEnabled(!show);
        }
    }
}

