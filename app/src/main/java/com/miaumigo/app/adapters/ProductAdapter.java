package com.miaumigo.app.adapters;

import android.content.Context;
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
import com.google.android.material.button.MaterialButton;
import android.widget.RatingBar;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductActionListener listener;
    private Context context;
    private boolean hideCartButton;

    public interface OnProductActionListener {
        void onProductClick(Product product);
        void onAddToCart(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductActionListener listener) {
        this.productList = productList;
        this.listener = listener;
        this.hideCartButton = false;
    }

    public ProductAdapter(List<Product> productList, OnProductActionListener listener, boolean hideCartButton) {
        this.productList = productList;
        this.listener = listener;
        this.hideCartButton = hideCartButton;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        context = parent.getContext();
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewRating;
        private TextView textViewStock;
        private TextView textViewBrand;
        private TextView textViewVendor;
        private RatingBar ratingBar;
        private MaterialButton buttonAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewStock = itemView.findViewById(R.id.textViewStock);
            textViewBrand = itemView.findViewById(R.id.textViewBrand);
            textViewVendor = itemView.findViewById(R.id.textViewVendor);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onProductClick(productList.get(position));
                }
            });

            if (buttonAddToCart != null) {
                buttonAddToCart.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onAddToCart(productList.get(position));
                    }
                });
            }
        }

        public void bind(Product product) {
            if (textViewName != null) {
                textViewName.setText(product.getName() != null ? product.getName() : "");
            }
            if (textViewPrice != null) {
                textViewPrice.setText(String.format("R$ %.2f", product.getPrice()));
            }
            if (textViewBrand != null) {
                textViewBrand.setText(product.getBrand() != null && !product.getBrand().isEmpty() ?
                    product.getBrand() : context.getString(R.string.label_generic_brand));
            }
            if (textViewVendor != null) {
                textViewVendor.setText(product.getVendorName() != null && !product.getVendorName().isEmpty() ?
                    product.getVendorName() : context.getString(R.string.label_generic_vendor));
            }

            if (textViewRating != null) {
                String ratingText = String.format("%.1f", product.getRating());
                if (product.getSalesCount() > 0) {
                    ratingText = ratingText + " (" + product.getSalesCount() + ")";
                }
                textViewRating.setText(ratingText);
            }

            if (ratingBar != null) {
                ratingBar.setRating((float) product.getRating());
            }

            if (textViewStock != null) {
                textViewStock.setText(product.isInStock() ? 
                    context.getString(R.string.in_stock) : context.getString(R.string.out_of_stock));
                textViewStock.setTextColor(product.isInStock() ? 
                    itemView.getContext().getColor(R.color.success) : 
                    itemView.getContext().getColor(R.color.error));
            }

            if (buttonAddToCart != null) {
                if (hideCartButton) {
                    buttonAddToCart.setVisibility(View.GONE);
                } else {
                    buttonAddToCart.setVisibility(View.VISIBLE);
                    buttonAddToCart.setEnabled(product.isInStock());
                    buttonAddToCart.setAlpha(product.isInStock() ? 1f : 0.5f);
                }
            }

            // Carrega a imagem do produto
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
            }
        }
    }
}
