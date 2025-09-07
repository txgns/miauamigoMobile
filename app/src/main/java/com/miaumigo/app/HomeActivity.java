package com.miaumigo.app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.adapter.PetAdapter;
import com.miaumigo.app.model.Pet;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView profileImage;
    private EditText searchBar;
    private RecyclerView recyclerViewPets;
    private PetAdapter petAdapter;
    private List<Pet> petList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profileImage = findViewById(R.id.profile_image);
        searchBar = findViewById(R.id.search_bar);
        recyclerViewPets = findViewById(R.id.recyclerViewPets);

        // Setup RecyclerView
        recyclerViewPets.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        // TODO: Populate petList from a data source (e.g., Firestore)
        addDummyData(); // Using dummy data for now
        petAdapter = new PetAdapter(petList);
        recyclerViewPets.setAdapter(petAdapter);
    }

    private void addDummyData() {
        petList.add(new Pet("Milo", "Gato", "2 anos", "São Paulo, SP", R.drawable.ic_cat_placeholder));
        petList.add(new Pet("Rex", "Cachorro", "1 ano", "Rio de Janeiro, RJ", R.drawable.ic_dog_placeholder));
        petList.add(new Pet("Luna", "Gato", "3 meses", "Belo Horizonte, MG", R.drawable.ic_cat_placeholder_2));
        petList.add(new Pet("Buddy", "Cachorro", "5 anos", "Porto Alegre, RS", R.drawable.ic_dog_placeholder_2));
    }
}
