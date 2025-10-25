package com.miaumigo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.EditProfileActivity;
import com.miaumigo.app.EditAddressActivity;
import com.miaumigo.app.R;

public class HomeFragment extends Fragment {

    private TextView textViewWelcome;
    private Button buttonEditProfile;
    private Button buttonEditAddress;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(view);
        initFirebase();
        setupClickListeners();
        loadUserData();
        
        return view;
    }

    private void initViews(View view) {
        textViewWelcome = view.findViewById(R.id.textViewWelcome);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonEditAddress = view.findViewById(R.id.buttonEditAddress);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> openEditProfile());
        buttonEditAddress.setOnClickListener(v -> openEditAddress());
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String welcomeText = "Bem-vindo, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário") + "!";
            textViewWelcome.setText(welcomeText);
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void openEditAddress() {
        Intent intent = new Intent(getActivity(), EditAddressActivity.class);
        startActivity(intent);
    }
}
