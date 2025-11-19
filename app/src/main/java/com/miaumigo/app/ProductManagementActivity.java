package com.miaumigo.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.miaumigo.app.R;
import com.miaumigo.app.models.Product;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ProductManagementActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText editTextProductName;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private EditText editTextQuantity;
    private CheckBox checkBoxInStock;
    private CheckBox checkBoxVisibleToCustomers;
    private Button buttonSave;
    private Button buttonDelete;
    private ProgressBar progressBar;
    private ImageView imageViewProduct;
    private MaterialButton buttonTakePhoto;
    private MaterialButton buttonChoosePhoto;
    
    private FirebaseUser currentUser;
    private DatabaseReference productReference;
    private Product currentProduct;
    private boolean isEditMode;
    private String productId;
    private Uri imageUri;
    private File photoFile;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        initViews();
        setupToolbar();
        getIntentData();
        setupButtons();
        
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Inicializa Firebase Storage - usa a instância padrão configurada no google-services.json
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Tenta obter o bucket do google-services.json, se não conseguir, usa o padrão
        try {
            String bucketUrl = "gs://miaumigo-686d4.firebasestorage.app";
            storage = FirebaseStorage.getInstance(bucketUrl);
            android.util.Log.d("ProductManagement", "Firebase Storage inicializado com bucket: " + bucketUrl);
        } catch (Exception e) {
            android.util.Log.w("ProductManagement", "Usando storage padrão do google-services.json", e);
            storage = FirebaseStorage.getInstance();
        }
        storageReference = storage.getReference();
        
        if (isEditMode) {
            loadProductData();
        } else {
            buttonDelete.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        checkBoxInStock = findViewById(R.id.checkBoxInStock);
        checkBoxVisibleToCustomers = findViewById(R.id.checkBoxVisibleToCustomers);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        progressBar = findViewById(R.id.progressBar);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        buttonChoosePhoto = findViewById(R.id.buttonChoosePhoto);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Editar Produto" : "Novo Produto");
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

    private void getIntentData() {
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        productId = getIntent().getStringExtra("product_id");
    }

    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveProduct());
        buttonDelete.setOnClickListener(v -> deleteProduct());
        buttonTakePhoto.setOnClickListener(v -> takePhoto());
        buttonChoosePhoto.setOnClickListener(v -> choosePhoto());
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
            } catch (IOException ex) {
                Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "PRODUCT_" + UUID.randomUUID().toString();
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
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
                        android.util.Log.w("ProductManagement", "Usando URI file:// diretamente", e);
                        imageUri = Uri.fromFile(photoFile);
                    }
                    
                    if (imageUri != null) {
                        android.util.Log.d("ProductManagement", "Imagem capturada: " + imageUri.toString() + " (tamanho: " + photoFile.length() + " bytes)");
                        Glide.with(this).load(imageUri).into(imageViewProduct);
                    } else {
                        Toast.makeText(this, "Erro: não foi possível obter URI da imagem", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Erro: foto não foi capturada corretamente";
                    if (photoFile != null) {
                        errorMsg += " (arquivo existe: " + photoFile.exists() + ", tamanho: " + (photoFile.exists() ? photoFile.length() : 0) + ")";
                    }
                    android.util.Log.e("ProductManagement", errorMsg);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    android.util.Log.d("ProductManagement", "Imagem selecionada: " + imageUri.toString());
                    
                    // Concede permissão de leitura persistente
                    try {
                        getContentResolver().takePersistableUriPermission(imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        android.util.Log.d("ProductManagement", "Permissão persistente concedida");
                    } catch (Exception e) {
                        android.util.Log.w("ProductManagement", "Não foi possível conceder permissão persistente, tentando temporária", e);
                        // Tenta conceder permissão temporária
                        try {
                            grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e2) {
                            android.util.Log.w("ProductManagement", "Não foi possível conceder permissão temporária", e2);
                        }
                    }
                    
                    // Verifica se consegue acessar o arquivo
                    try {
                        android.content.ContentResolver resolver = getContentResolver();
                        java.io.InputStream testStream = resolver.openInputStream(imageUri);
                        if (testStream != null) {
                            long size = testStream.available();
                            testStream.close();
                            android.util.Log.d("ProductManagement", "Imagem acessível (tamanho: " + size + " bytes)");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProductManagement", "Erro ao verificar imagem: " + imageUri.toString(), e);
                    }
                    
                    Glide.with(this).load(imageUri).into(imageViewProduct);
                }
            }
        }
    }

    private void loadProductData() {
        if (productId == null) {
            Toast.makeText(this, "Erro: ID do produto não fornecido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        productReference = FirebaseDatabase.getInstance().getReference("products").child(productId);
        
        productReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                
                if (snapshot.exists()) {
                    currentProduct = snapshot.getValue(Product.class);
                    if (currentProduct != null) {
                        displayProductData(currentProduct);
                    }
                } else {
                    Toast.makeText(ProductManagementActivity.this, 
                        "Produto não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(ProductManagementActivity.this, 
                    "Erro ao carregar produto: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductData(Product product) {
        editTextProductName.setText(product.getName());
        editTextDescription.setText(product.getDescription());
        editTextPrice.setText(String.valueOf(product.getPrice()));
        editTextQuantity.setText(String.valueOf(product.getQuantity()));
        checkBoxInStock.setChecked(product.isInStock());
        checkBoxVisibleToCustomers.setChecked(product.isVisibleToCustomers());
        
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this).load(product.getImageUrl()).into(imageViewProduct);
        }
    }

    private void saveProduct() {
        String productName = editTextProductName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();
        
        if (TextUtils.isEmpty(productName)) {
            editTextProductName.setError("Nome do produto é obrigatório");
            return;
        }
        
        if (TextUtils.isEmpty(priceStr)) {
            editTextPrice.setError("Preço é obrigatório");
            return;
        }
        
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            editTextPrice.setError("Preço inválido");
            return;
        }
        
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityStr)) {
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                editTextQuantity.setError("Quantidade inválida");
                return;
            }
        }
        
        if (currentUser == null) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        if (currentProduct == null) {
            // Cria novo produto
            currentProduct = new Product();
            currentProduct.setId(UUID.randomUUID().toString());
            currentProduct.setVendorId(currentUser.getUid());
        }
        
        currentProduct.setName(productName);
        currentProduct.setDescription(description);
        currentProduct.setPrice(price);
        currentProduct.setQuantity(quantity);
        currentProduct.setInStock(checkBoxInStock.isChecked());
        currentProduct.setVisibleToCustomers(checkBoxVisibleToCustomers.isChecked());
        currentProduct.setUpdatedAt(System.currentTimeMillis());
        
        if (currentProduct.getCreatedAt() == 0) {
            currentProduct.setCreatedAt(System.currentTimeMillis());
        }
        
        // Upload da imagem primeiro, se houver
        if (imageUri != null) {
            uploadImageAndSaveProduct();
        } else {
            saveProductToDatabase();
        }
    }

    private void uploadImageAndSaveProduct() {
        if (imageUri == null) {
            saveProductToDatabase();
            return;
        }

        if (currentUser == null) {
            showLoading(false);
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria o caminho da imagem: products/{vendorId}/{productId}.jpg
        String imageFileName = "products/" + currentUser.getUid() + "/" + currentProduct.getId() + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageReference.child(imageFileName);
        
        android.util.Log.d("ProductManagement", "Iniciando upload de imagem: " + imageFileName);
        android.util.Log.d("ProductManagement", "URI: " + imageUri.toString());
        android.util.Log.d("ProductManagement", "Scheme: " + imageUri.getScheme());
        
        try {
            UploadTask uploadTask;
            
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
                
                File file = new File(path);
                if (!file.exists()) {
                    showLoading(false);
                    android.util.Log.e("ProductManagement", "Arquivo não existe: " + path);
                    Toast.makeText(this, "Erro: arquivo de imagem não encontrado. Tente selecionar novamente.", Toast.LENGTH_LONG).show();
                    return;
                }
                
                if (file.length() == 0) {
                    showLoading(false);
                    Toast.makeText(this, "Erro: arquivo de imagem está vazio", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                android.util.Log.d("ProductManagement", "Arquivo encontrado: " + path + " (tamanho: " + file.length() + " bytes)");
                
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
                        android.util.Log.w("ProductManagement", "Não foi possível conceder permissão", e2);
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
                    android.util.Log.e("ProductManagement", "Erro ao verificar URI: " + imageUri.toString(), e);
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
                android.util.Log.d("ProductManagement", "Upload concluído com sucesso");
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    android.util.Log.d("ProductManagement", "URL da imagem obtida: " + imageUrl);
                    currentProduct.setImageUrl(imageUrl);
                    saveProductToDatabase();
                }).addOnFailureListener(e -> {
                    showLoading(false);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
                    android.util.Log.e("ProductManagement", "Erro ao obter URL: " + errorMsg, e);
                    Toast.makeText(this, "Erro ao obter URL da imagem: " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }).addOnFailureListener(e -> {
                android.util.Log.e("ProductManagement", "Erro no upload", e);
                showLoading(false);
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
                android.util.Log.e("ProductManagement", "Erro no upload: " + errorMsg, e);
                
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
                android.util.Log.d("ProductManagement", "Upload progress: " + progress + "%");
            });
        } catch (java.io.FileNotFoundException e) {
            showLoading(false);
            android.util.Log.e("ProductManagement", "Arquivo não encontrado", e);
            Toast.makeText(this, "Erro: arquivo de imagem não encontrado", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            showLoading(false);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
            android.util.Log.e("ProductManagement", "Erro ao processar imagem: " + errorMsg, e);
            Toast.makeText(this, "Erro ao processar imagem: " + errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void saveProductToDatabase() {
        productReference = FirebaseDatabase.getInstance().getReference("products")
            .child(currentProduct.getId());
        
        android.util.Log.d("ProductManagement", "Salvando produto no banco de dados");
        android.util.Log.d("ProductManagement", "ID: " + currentProduct.getId());
        android.util.Log.d("ProductManagement", "Nome: " + currentProduct.getName());
        android.util.Log.d("ProductManagement", "ImageUrl: " + currentProduct.getImageUrl());
        
        productReference.setValue(currentProduct)
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                android.util.Log.d("ProductManagement", "Produto salvo com sucesso!");
                Toast.makeText(ProductManagementActivity.this, 
                    "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
                android.util.Log.e("ProductManagement", "Erro ao salvar produto: " + errorMsg, e);
                Toast.makeText(ProductManagementActivity.this, 
                    "Erro ao salvar produto: " + errorMsg, Toast.LENGTH_LONG).show();
            });
    }

    private void deleteProduct() {
        if (productId == null || currentProduct == null) {
            return;
        }
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Deseja realmente excluir este produto?")
            .setPositiveButton("Excluir", (dialog, which) -> {
                showLoading(true);
                productReference = FirebaseDatabase.getInstance().getReference("products")
                    .child(productId);
                productReference.removeValue()
                    .addOnCompleteListener(task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(ProductManagementActivity.this, 
                                "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ProductManagementActivity.this, 
                                "Erro ao excluir produto", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSave.setEnabled(!show);
        buttonDelete.setEnabled(!show);
        buttonTakePhoto.setEnabled(!show);
        buttonChoosePhoto.setEnabled(!show);
    }
}
