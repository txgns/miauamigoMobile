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
import com.miaumigo.app.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewRating;
        private TextView textViewStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewStock = itemView.findViewById(R.id.textViewStock);
        }

        public void bind(Product product, OnProductClickListener listener) {
            textViewName.setText(product.getName());
            textViewPrice.setText(product.getFormattedPrice());
            textViewRating.setText(String.format("%.1f ⭐", product.getRating()));

            // Set stock status
            if (product.isInStock()) {
                textViewStock.setText("Em estoque");
                textViewStock.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                textViewStock.setText("Fora de estoque");
                textViewStock.setTextColor(itemView.getContext().getColor(R.color.error));
            }

            // Load product image
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}

