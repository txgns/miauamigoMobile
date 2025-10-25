package com.miaumigo.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.R;
import com.miaumigo.app.models.Product;
import com.miaumigo.app.utils.ProductManager;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;
    private ProductManager productManager;
    private Context context;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        context = parent.getContext();
        productManager = ProductManager.getInstance(context);
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
        private ImageView imageViewFavorite;
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewRating;
        private TextView textViewStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewStock = itemView.findViewById(R.id.textViewStock);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(productList.get(getAdapterPosition()));
                }
            });

            if (imageViewFavorite != null) {
                imageViewFavorite.setOnClickListener(v -> {
                    Product product = productList.get(getAdapterPosition());
                    productManager.toggleFavorite(product.getId());
                    updateFavoriteIcon(product.getId());
                    Toast.makeText(context, productManager.isFavorite(product.getId()) ? 
                        "Adicionado aos favoritos" : "Removido dos favoritos", Toast.LENGTH_SHORT).show();
                });
            }
        }

        public void bind(Product product) {
            textViewName.setText(product.getName());
            textViewPrice.setText(String.format("R$ %.2f", product.getPrice()));
            textViewRating.setText(String.format("%.1f ⭐", product.getRating()));
            textViewStock.setText(product.isInStock() ? "Em estoque" : "Fora de estoque");
            textViewStock.setTextColor(product.isInStock() ? 
                itemView.getContext().getColor(R.color.success) : 
                itemView.getContext().getColor(R.color.error));

            // Aqui você carregaria a imagem usando Glide ou Picasso
            // Por enquanto, usando um placeholder
            imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
            
            // Atualizar ícone de favorito
            updateFavoriteIcon(product.getId());
        }

        private void updateFavoriteIcon(String productId) {
            if (imageViewFavorite != null) {
                boolean isFavorite = productManager.isFavorite(productId);
                imageViewFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
                imageViewFavorite.setColorFilter(isFavorite ? 
                    itemView.getContext().getColor(R.color.error) : 
                    itemView.getContext().getColor(R.color.text_secondary));
            }
        }
    }
}
