package com.miaumigo.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.R;
import com.miaumigo.app.model.Pet;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<Pet> petList;

    public PetAdapter(List<Pet> petList) {
        this.petList = petList;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.petName.setText(pet.getName());
        holder.petBreed.setText(pet.getBreed());
        holder.petAge.setText(pet.getAge());
        holder.petLocation.setText(pet.getLocation());
        holder.petImage.setImageResource(pet.getImageResource());
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView petImage;
        TextView petName, petBreed, petAge, petLocation;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.pet_image);
            petName = itemView.findViewById(R.id.pet_name);
            petBreed = itemView.findViewById(R.id.pet_breed);
            petAge = itemView.findViewById(R.id.pet_age);
            petLocation = itemView.findViewById(R.id.pet_location);
        }
    }
}
