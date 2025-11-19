package com.miaumigo.app.fragments.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.AnnouncementDetailActivity;
import com.miaumigo.app.CreateAnnouncementActivity;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.AnnouncementAdapter;
import com.miaumigo.app.models.Announcement;
import com.miaumigo.app.utils.AnnouncementManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VendorMyAnnouncementsFragment extends Fragment {

    private RecyclerView recyclerViewAnnouncements;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreateAnnouncement;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AnnouncementAdapter announcementAdapter;
    private List<Announcement> announcementList;
    private FirebaseUser currentUser;
    private AnnouncementManager announcementManager;
    private DatabaseReference announcementsReference;
    private ValueEventListener announcementsListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        announcementList = new ArrayList<>();
        announcementManager = AnnouncementManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_vendor_my_announcements, container, false);
            
            recyclerViewAnnouncements = view.findViewById(R.id.recyclerViewAnnouncements);
            progressBar = view.findViewById(R.id.progressBar);
            fabCreateAnnouncement = view.findViewById(R.id.fabCreateAnnouncement);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
            
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(this::loadAnnouncements);
            }
            
            setupRecyclerView();
            setupFab();
            loadAnnouncements();
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("VendorMyAnnouncementsFragment", "Erro ao criar view", e);
            e.printStackTrace();
            return new View(getContext());
        }
    }

    private void setupRecyclerView() {
        announcementAdapter = new AnnouncementAdapter(announcementList, announcement -> {
            // Abre detalhes do anúncio com opções de editar/excluir
            if (announcement != null && currentUser != null && 
                currentUser.getUid().equals(announcement.getVendorId())) {
                Intent intent = new Intent(getContext(), AnnouncementDetailActivity.class);
                intent.putExtra("announcement_id", announcement.getId());
                intent.putExtra("is_owner", true);
                startActivity(intent);
            }
        });
        
        recyclerViewAnnouncements.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAnnouncements.setAdapter(announcementAdapter);
    }

    private void setupFab() {
        fabCreateAnnouncement.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateAnnouncementActivity.class);
            intent.putExtra("edit_mode", false);
            startActivity(intent);
        });
    }

    private void loadAnnouncements() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        
        if (announcementsReference == null) {
            announcementsReference = FirebaseDatabase.getInstance().getReference("announcements");
        }
        
        if (announcementsListener != null) {
            announcementsReference.removeEventListener(announcementsListener);
        }

        com.google.firebase.database.Query announcementsQuery = announcementsReference
            .orderByChild("vendorId")
            .equalTo(currentUser.getUid());
        
        announcementsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                announcementList.clear();
                
                for (DataSnapshot announcementSnapshot : snapshot.getChildren()) {
                    Announcement announcement = announcementSnapshot.getValue(Announcement.class);
                    if (announcement != null) {
                        announcementList.add(announcement);
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
        };
        
        announcementsQuery.addValueEventListener(announcementsListener);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (swipeRefreshLayout != null && !show) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (announcementsReference != null && announcementsListener != null) {
            announcementsReference.removeEventListener(announcementsListener);
        }
    }
}

