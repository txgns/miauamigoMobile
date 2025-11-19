package com.miaumigo.app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDiagnostic {
    
    private static final String TAG = "FirebaseDiagnostic";
    
    public static void testFirebaseConnection(Context context) {
        Log.d(TAG, "Iniciando diagnóstico do Firebase...");
        
        try {
            // 1. Teste de inicialização do Firebase
            FirebaseApp app = FirebaseApp.initializeApp(context);
            if (app == null) {
                Log.e(TAG, "Firebase App não foi inicializado");
                Toast.makeText(context, "Firebase App não inicializado", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG, "Firebase App inicializado: " + app.getName());
            
            // 2. Teste de Firebase Auth
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth == null) {
                Log.e(TAG, "Firebase Auth não foi inicializado");
                Toast.makeText(context, "Firebase Auth não inicializado", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG, "Firebase Auth inicializado");
            
            // 3. Teste de Firebase Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            if (database == null) {
                Log.e(TAG, "Firebase Database não foi inicializado");
                Toast.makeText(context, "Firebase Database não inicializado", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG, "Firebase Database inicializado");
            
            // 4. Teste de conectividade com o banco
            DatabaseReference testRef = database.getReference("connection_test");
            testRef.setValue("test_" + System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase Database conectado com sucesso");
                    Toast.makeText(context, "Firebase conectado com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao conectar com Firebase Database: " + e.getMessage());
                    Toast.makeText(context, "Erro Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Erro geral no Firebase: " + e.getMessage());
            Toast.makeText(context, "Erro Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    public static void checkFirebaseConfiguration(Context context) {
        Log.d(TAG, "Verificando configuração do Firebase...");
        
        // Verificar se o google-services.json está presente
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            String projectId = app.getOptions().getProjectId();
            Log.d(TAG, "Project ID: " + projectId);
            Toast.makeText(context, "Project ID: " + projectId, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter configuração: " + e.getMessage());
            Toast.makeText(context, "Erro configuração: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
