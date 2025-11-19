package com.miaumigo.app.fragments.vendor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.CreateAnnouncementActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.AnnouncementAdapter;
import com.miaumigo.app.models.Announcement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VendorAnnouncementsFragment extends Fragment {

    private RecyclerView recyclerViewAnnouncements;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreateAnnouncement;
    private AnnouncementAdapter announcementAdapter;
    private List<Announcement> announcementList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        announcementList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_vendor_announcements, container, false);
            
            recyclerViewAnnouncements = view.findViewById(R.id.recyclerViewAnnouncements);
            progressBar = view.findViewById(R.id.progressBar);
            fabCreateAnnouncement = view.findViewById(R.id.fabCreateAnnouncement);
            
            setupRecyclerView();
            setupFab();
            loadAnnouncements();
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("VendorAnnouncementsFragment", "Erro ao criar view", e);
            e.printStackTrace();
            return new View(getContext());
        }
    }

    private void setupRecyclerView() {
        announcementAdapter = new AnnouncementAdapter(announcementList, announcement -> {
            // Abre detalhes do anúncio (se for de outro vendedor, pode entrar em contato)
            if (announcement != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), 
                    com.miaumigo.app.AnnouncementDetailActivity.class);
                intent.putExtra("announcement_id", announcement.getId());
                intent.putExtra("is_owner", false);
                startActivity(intent);
            }
        });
        
        recyclerViewAnnouncements.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAnnouncements.setAdapter(announcementAdapter);
    }

    private void setupFab() {
        fabCreateAnnouncement.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), 
                CreateAnnouncementActivity.class);
            startActivity(intent);
        });
    }

    private void loadAnnouncements() {
        showLoading(true);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showLoading(false);
            return;
        }
        
        // Carrega apenas anúncios de outros vendedores (não os próprios)
        com.google.firebase.database.Query announcementsQuery = FirebaseDatabase.getInstance()
            .getReference("announcements")
            .orderByChild("createdAt");
        
        announcementsQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    announcementList.clear();
                    String currentUserId = currentUser.getUid();
                    
                    for (DataSnapshot announcementSnapshot : snapshot.getChildren()) {
                        Announcement announcement = announcementSnapshot.getValue(Announcement.class);
                        if (announcement != null && !currentUserId.equals(announcement.getVendorId())) {
                            // Mostra apenas anúncios disponíveis ou reservados (não vendidos)
                            if (announcement.getStatus() == null || 
                                announcement.getStatus() == Announcement.AnnouncementStatus.AVAILABLE ||
                                announcement.getStatus() == Announcement.AnnouncementStatus.RESERVED) {
                                announcementList.add(announcement);
                            }
                        }
                    }
                    
                    // Ordena por data (mais recente primeiro)
                    Collections.sort(announcementList, (a1, a2) -> 
                        Long.compare(a2.getCreatedAt(), a1.getCreatedAt()));
                    
                    announcementAdapter.notifyDataSetChanged();
                    showLoading(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showLoading(false);
                    Toast.makeText(getContext(), "Erro ao carregar anúncios: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
    }
}

