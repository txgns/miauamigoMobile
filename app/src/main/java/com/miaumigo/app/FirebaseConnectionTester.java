package com.miaumigo.app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConnectionTester {
    
    private static final String TAG = "FirebaseTester";
    
    public static void runFullDiagnostic(Context context) {
        Log.d(TAG, "=== INICIANDO DIAGNÓSTICO COMPLETO FIREBASE ===");
        
        // 1. Teste de inicialização
        testFirebaseInitialization(context);
        
        // 2. Teste de Auth
        testFirebaseAuth(context);
        
        // 3. Teste de Database
        testFirebaseDatabase(context);
        
        // 4. Teste de conectividade
        testNetworkConnectivity(context);
    }
    
    private static void testFirebaseInitialization(Context context) {
        Log.d(TAG, "1. Testando inicialização do Firebase...");
        
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            if (app != null) {
                Log.d(TAG, "Firebase App: " + app.getName());
                Log.d(TAG, "Project ID: " + app.getOptions().getProjectId());
                Log.d(TAG, "API Key: " + app.getOptions().getApiKey());
                Toast.makeText(context, "Firebase inicializado", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, " Firebase App é null");
                Toast.makeText(context, " Firebase App null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, " Erro inicialização: " + e.getMessage());
            Toast.makeText(context, " Erro inicialização: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private static void testFirebaseAuth(Context context) {
        Log.d(TAG, "2. Testando Firebase Auth...");
        
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth != null) {
                Log.d(TAG, " Firebase Auth OK");
                Toast.makeText(context, " Firebase Auth OK", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, " Firebase Auth null");
                Toast.makeText(context, " Firebase Auth null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, " Erro Auth: " + e.getMessage());
            Toast.makeText(context, " Erro Auth: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private static void testFirebaseDatabase(Context context) {
        Log.d(TAG, "3. Testando Firebase Database...");
        
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference();
            
            if (database != null && ref != null) {
                Log.d(TAG, " Firebase Database OK");
                Toast.makeText(context, " Database OK", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Firebase Database null");
                Toast.makeText(context, " Database null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, " Erro Database: " + e.getMessage());
            Toast.makeText(context, " Erro Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private static void testNetworkConnectivity(Context context) {
        Log.d(TAG, "4. Testando conectividade de rede...");
        
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference testRef = database.getReference("connection_test");
            
            testRef.setValue("test_" + System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, " Conectividade OK - Dados salvos");
                    Toast.makeText(context, " Conectividade OK", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Erro conectividade: " + e.getMessage());
                    Toast.makeText(context, " Erro rede: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    
                    // Diagnóstico específico do erro
                    diagnoseSpecificError(e.getMessage(), context);
                });
                
        } catch (Exception e) {
            Log.e(TAG, " Erro geral conectividade: " + e.getMessage());
            Toast.makeText(context, " Erro geral: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private static void diagnoseSpecificError(String errorMessage, Context context) {
        Log.d(TAG, "Diagnosticando erro específico: " + errorMessage);
        
        if (errorMessage.contains("Permission denied")) {
            Log.e(TAG, " DIAGNÓSTICO: Problema de permissão no Database");
            Toast.makeText(context, " Problema: Regras de segurança do Database", Toast.LENGTH_LONG).show();
            Toast.makeText(context, " Solução: Configure as regras no Console Firebase", Toast.LENGTH_LONG).show();
            
        } else if (errorMessage.contains("Network error") || errorMessage.contains("Connection failed")) {
            Log.e(TAG, " DIAGNÓSTICO: Problema de rede");
            Toast.makeText(context, " Problema: Conexão de rede", Toast.LENGTH_LONG).show();
            Toast.makeText(context, " Solução: Verifique internet e firewall", Toast.LENGTH_LONG).show();
            
        } else if (errorMessage.contains("App not found")) {
            Log.e(TAG, " DIAGNÓSTICO: App não encontrado");
            Toast.makeText(context, " Problema: Configuração do app", Toast.LENGTH_LONG).show();
            Toast.makeText(context, " Solução: Verifique google-services.json", Toast.LENGTH_LONG).show();
            
        } else if (errorMessage.contains("Invalid API key")) {
            Log.e(TAG, " DIAGNÓSTICO: API Key inválida");
            Toast.makeText(context, " Problema: API Key", Toast.LENGTH_LONG).show();
            Toast.makeText(context, " Solução: Re-download google-services.json", Toast.LENGTH_LONG).show();
            
        } else {
            Log.e(TAG, " DIAGNÓSTICO: Erro desconhecido");
            Toast.makeText(context, " Erro desconhecido: " + errorMessage, Toast.LENGTH_LONG).show();
        }
    }
}
