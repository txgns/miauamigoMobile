package com.miaumigo.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.model.Pet;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPets;
    private PetAdapter petAdapter;
    private List<Pet> petList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // TODO: Adicionar lógica para diferenciar a view de cliente e vendedor

        initializeViews();
        setupRecyclerView();
        loadPetData();
    }

    private void initializeViews() {
        recyclerViewPets = findViewById(R.id.recyclerViewPets);
    }

    private void setupRecyclerView() {
        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList);
        recyclerViewPets.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPets.setAdapter(petAdapter);
    }

    private void loadPetData() {
        // Dados de exemplo
        petList.add(new Pet("Rex", "Macho", "2 anos", R.drawable.ic_dog_placeholder));
        petList.add(new Pet("Mimi", "Fêmea", "1 ano", R.drawable.ic_cat_placeholder));
        petList.add(new Pet("Bolinha", "Macho", "3 meses", R.drawable.ic_dog_placeholder_2));
        petList.add(new Pet("Luna", "Fêmea", "5 anos", R.drawable.ic_cat_placeholder_2));

        petAdapter.notifyDataSetChanged();
    }

    // Adapter para o RecyclerView
    private class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

        private List<Pet> petList;

        public PetAdapter(List<Pet> petList) {
            this.petList = petList;
        }

        @NonNull
        @Override
        public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_card, parent, false);
            return new PetViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
            Pet pet = petList.get(position);
            holder.bind(pet);
        }

        @Override
        public int getItemCount() {
            return petList.size();
        }

        // ViewHolder para cada item da lista
        class PetViewHolder extends RecyclerView.ViewHolder {

            private ImageView imageViewPet;
            private TextView textViewPetName;
            private TextView textViewPetGender;
            private TextView textViewPetAge;
            private ImageView imageViewPetGender;

            public PetViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewPet = itemView.findViewById(R.id.imageViewPet);
                textViewPetName = itemView.findViewById(R.id.textViewPetName);
                textViewPetGender = itemView.findViewById(R.id.textViewPetGender);
                textViewPetAge = itemView.findViewById(R.id.textViewPetAge);
                imageViewPetGender = itemView.findViewById(R.id.imageViewPetGender);
            }

            public void bind(Pet pet) {
                textViewPetName.setText(pet.getName());
                textViewPetGender.setText(pet.getGender());
                textViewPetAge.setText(pet.getAge());
                imageViewPet.setImageResource(pet.getImageResource());

                if ("Macho".equals(pet.getGender())) {
                    imageViewPetGender.setImageResource(R.drawable.ic_male);
                } else {
                    imageViewPetGender.setImageResource(R.drawable.ic_female);
                }
            }
        }
    }
}
