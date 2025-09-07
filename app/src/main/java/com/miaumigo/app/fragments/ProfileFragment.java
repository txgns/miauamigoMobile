package com.miaumigo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.R;
import com.miaumigo.app.models.User;
import com.miaumigo.app.services.FirebaseAuthService;
import com.miaumigo.app.services.FirebaseDatabaseService;

public class ProfileFragment extends Fragment {

    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private Button buttonEditProfile;
    private Button buttonLogout;
    private ProgressBar progressBar;
    private FirebaseAuthService authService;
    private FirebaseDatabaseService databaseService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupClickListeners();
        loadUserProfile();
        
        return view;
    }

    private void initializeViews(View view) {
        textViewName = view.findViewById(R.id.textViewName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewPhone = view.findViewById(R.id.textViewPhone);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        progressBar = view.findViewById(R.id.progressBar);
        
        authService = new FirebaseAuthService(getContext());
        databaseService = new FirebaseDatabaseService(getContext());
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
            Toast.makeText(getContext(), "Funcionalidade de edição em desenvolvimento", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            authService.logout();
            Intent intent = new Intent(getContext(), com.miaumigo.app.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        showLoading(true);
        
        databaseService.getUser(currentUser.getUid(), new FirebaseDatabaseService.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                showLoading(false);
                if (user != null) {
                    textViewName.setText(user.getName());
                    textViewEmail.setText(user.getEmail());
                    textViewPhone.setText(user.getPhone() != null ? user.getPhone() : "Não informado");
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                
                // Fallback to Firebase user data
                textViewName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário");
                textViewEmail.setText(currentUser.getEmail());
                textViewPhone.setText("Não informado");
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

