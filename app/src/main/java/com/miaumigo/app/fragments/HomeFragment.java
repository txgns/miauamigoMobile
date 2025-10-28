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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.EditProfileActivity;
import com.miaumigo.app.EditAddressActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.models.User;

public class HomeFragment extends Fragment {

    private TextView textViewWelcome;
    private Button buttonEditProfile;
    private Button buttonEditAddress;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

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
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> openEditProfile());
        buttonEditAddress.setOnClickListener(v -> openEditAddress());
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Buscar dados completos do usuário do Realtime Database
            databaseReference.child("users").child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                                    String welcomeText = "Bem-vindo, " + user.getName() + "!";
                                    textViewWelcome.setText(welcomeText);
                                } else {
                                    setDefaultWelcome(currentUser);
                                }
                            } else {
                                setDefaultWelcome(currentUser);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            setDefaultWelcome(currentUser);
                        }
                    });
        }
    }
    
    private void setDefaultWelcome(FirebaseUser currentUser) {
        String userName = "Usuário";
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            userName = currentUser.getDisplayName();
        }
        String welcomeText = "Bem-vindo, " + userName + "!";
        textViewWelcome.setText(welcomeText);
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
