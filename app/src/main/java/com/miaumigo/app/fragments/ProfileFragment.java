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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.miaumigo.app.EditProfileActivity;
import com.miaumigo.app.EditAddressActivity;
import com.miaumigo.app.MainActivity;
import com.miaumigo.app.R;

public class ProfileFragment extends Fragment {

    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private Button buttonEditProfile;
    private Button buttonEditAddress;
    private Button buttonLogout;
    private ProgressBar progressBar;
    
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initViews(view);
        initFirebase();
        setupClickListeners();
        loadUserData();
        
        return view;
    }

    private void initViews(View view) {
        textViewName = view.findViewById(R.id.textViewName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewPhone = view.findViewById(R.id.textViewPhone);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonEditAddress = view.findViewById(R.id.buttonEditAddress);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> openEditProfile());
        buttonEditAddress.setOnClickListener(v -> openEditAddress());
        buttonLogout.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            textViewName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário");
            textViewEmail.setText(currentUser.getEmail());
            textViewPhone.setText("Telefone não informado");
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

    private void logout() {
        showLoading(true);
        firebaseAuth.signOut();
        showLoading(false);
        
        Toast.makeText(getContext(), "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
