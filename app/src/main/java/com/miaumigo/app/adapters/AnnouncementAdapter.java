package com.miaumigo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.miaumigo.app.R;
import com.miaumigo.app.models.Announcement;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private List<Announcement> announcementList;
    private OnAnnouncementClickListener listener;

    public interface OnAnnouncementClickListener {
        void onAnnouncementClick(Announcement announcement);
    }

    public AnnouncementAdapter(List<Announcement> announcementList, OnAnnouncementClickListener listener) {
        this.announcementList = announcementList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcementList.get(position);
        holder.bind(announcement);
    }

    @Override
    public int getItemCount() {
        return announcementList != null ? announcementList.size() : 0;
    }

    class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewDescription;
        private TextView textViewVendorName;
        private TextView textViewPrice;
        private TextView textViewType;
        private TextView textViewCondition;
        private TextView textViewInStock;

        AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewVendorName = itemView.findViewById(R.id.textViewVendorName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewCondition = itemView.findViewById(R.id.textViewCondition);
            textViewInStock = itemView.findViewById(R.id.textViewInStock);
        }

        void bind(Announcement announcement) {
            textViewProductName.setText(announcement.getProductName() != null ? 
                announcement.getProductName() : "Produto");
            textViewDescription.setText(announcement.getDescription() != null ? 
                announcement.getDescription() : "");
            textViewVendorName.setText(announcement.getVendorName() != null ? 
                announcement.getVendorName() : "Vendedor");
            
            // Formata preço
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            textViewPrice.setText(currencyFormat.format(announcement.getSuggestedPrice()));
            
            // Tipo de anúncio
            String typeText = "";
            switch (announcement.getType()) {
                case SALE:
                    typeText = "Venda";
                    break;
                case TRADE:
                    typeText = "Troca";
                    break;
                case REQUEST:
                    typeText = "Busca";
                    break;
            }
            textViewType.setText(typeText);
            
            // Condição
            textViewCondition.setText(announcement.getCondition() != null ? 
                announcement.getCondition() : "Não especificado");
            
            // Estoque
            if (announcement.isInStock()) {
                textViewInStock.setText("Em estoque");
                textViewInStock.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                textViewInStock.setText("Fora de estoque");
                textViewInStock.setTextColor(itemView.getContext().getColor(R.color.error));
            }
            
            // Carrega imagem
            if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(announcement.getImageUrl())
                    .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAnnouncementClick(announcement);
                }
            });
        }
    }
}

