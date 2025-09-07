package com.miaumigo.app.services;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miaumigo.app.R;
import com.miaumigo.app.models.User;

public class FirebaseAuthService {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Context context;

    public FirebaseAuthService(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }

    public void registerUser(String email, String password, String name, String phone, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                User user = new User(firebaseUser.getUid(), name, email, phone);
                                saveUserToDatabase(user, callback);
                            }
                        } else {
                            callback.onError(task.getException() != null ? 
                                task.getException().getMessage() : "Erro ao criar conta");
                        }
                    }
                });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException() != null ? 
                                task.getException().getMessage() : "Erro ao fazer login");
                        }
                    }
                });
    }

    public void logout() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private void saveUserToDatabase(User user, AuthCallback callback) {
        mDatabase.child("users").child(user.getUid()).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Erro ao salvar dados do usuário");
                        }
                    }
                });
    }

    public void resetPassword(String email, AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException() != null ? 
                                task.getException().getMessage() : "Erro ao enviar email de recuperação");
                        }
                    }
                });
    }
}

