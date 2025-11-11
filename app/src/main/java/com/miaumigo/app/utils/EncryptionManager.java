package com.miaumigo.app.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.config.TinkConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class EncryptionManager {

    private static final String TAG = "EncryptionManager";
    private static final String KEYSET_NAME = "miauamigo_keyset";
    private static final String PREF_FILE_NAME = "miauamigo_encrypted_prefs";
    private static final String MASTER_KEY_URI = "android-keystore://miauamigo_master_key";

    private static final String ENCRYPTED_PREFIX = "ENC::";
    private static EncryptionManager instance;
    private final Aead aead;

    private EncryptionManager(Context context) throws GeneralSecurityException, IOException {
        this(buildAead(context));
    }

    private EncryptionManager(Aead aead) {
        this.aead = aead;
    }

    private static Aead buildAead(Context context) throws GeneralSecurityException, IOException {
        TinkConfig.register();
        AndroidKeysetManager keysetManager = new AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(MASTER_KEY_URI)
                .build();
        return keysetManager.getKeysetHandle().getPrimitive(Aead.class);
    }

    public static synchronized EncryptionManager getInstance(Context context) {
        if (instance == null) {
            try {
                instance = new EncryptionManager(context.getApplicationContext());
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "Failed to initialize encryption manager", e);
                instance = new EncryptionManager((Aead) null);
            }
        }
        return instance;
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        if (aead == null) {
            return plainText;
        }
        try {
            byte[] cipherBytes = aead.encrypt(plainText.getBytes(StandardCharsets.UTF_8), null);
            return ENCRYPTED_PREFIX + Base64.encodeToString(cipherBytes, Base64.NO_WRAP);
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Encryption failed", e);
            return plainText;
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        if (!cipherText.startsWith(ENCRYPTED_PREFIX)) {
            return cipherText;
        }
        if (aead == null) {
            return cipherText;
        }
        try {
            String payload = cipherText.substring(ENCRYPTED_PREFIX.length());
            byte[] decrypted = aead.decrypt(Base64.decode(payload, Base64.NO_WRAP), null);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            Log.e(TAG, "Decryption failed", e);
            return cipherText;
        }
    }

}

