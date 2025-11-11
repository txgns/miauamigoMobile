package com.miaumigo.app.fragments.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.miaumigo.app.R;
import com.miaumigo.app.models.User;
import com.miaumigo.app.utils.EncryptionManager;

public class VendorProfileFragment extends Fragment {

    private TextView textViewVendorName;
    private TextView textViewVendorEmail;
    private TextView textViewVendorPhone;
    private Button buttonEditProfile;
    
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_profile, container, false);
        
        initViews(view);
        initFirebase();
        setupClickListeners();
        loadUserData();
        
        return view;
    }

    private void initViews(View view) {
        textViewVendorName = view.findViewById(R.id.textViewVendorName);
        textViewVendorEmail = view.findViewById(R.id.textViewVendorEmail);
        textViewVendorPhone = view.findViewById(R.id.textViewVendorPhone);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReference.child("users").child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    EncryptionManager encryptionManager = EncryptionManager.getInstance(requireContext());
                                    user.setName(encryptionManager.decrypt(user.getName()));
                                    user.setPhone(encryptionManager.decrypt(user.getPhone()));
                                    displayUserData(user, currentUser);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Erro ao carregar dados: " + error.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void displayUserData(User user, FirebaseUser firebaseUser) {
        textViewVendorName.setText(user.getName() != null ? user.getName() : "Vendedor");
        textViewVendorEmail.setText(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
        textViewVendorPhone.setText(user.getPhone() != null ? user.getPhone() : "Não informado");
    }
}

