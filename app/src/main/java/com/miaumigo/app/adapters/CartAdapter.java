package com.miaumigo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.R;
import com.miaumigo.app.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onRemoveItem(CartItem item);
        void onUpdateQuantity(CartItem item, int newQuantity);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewQuantity;
        private TextView textViewTotal;
        private TextView buttonRemove;
        private TextView buttonIncrease;
        private TextView buttonDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);

            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(cartItems.get(getAdapterPosition()));
                }
            });

            buttonIncrease.setOnClickListener(v -> {
                if (listener != null) {
                    CartItem item = cartItems.get(getAdapterPosition());
                    listener.onUpdateQuantity(item, item.getQuantity() + 1);
                }
            });

            buttonDecrease.setOnClickListener(v -> {
                if (listener != null) {
                    CartItem item = cartItems.get(getAdapterPosition());
                    if (item.getQuantity() > 1) {
                        listener.onUpdateQuantity(item, item.getQuantity() - 1);
                    } else {
                        listener.onRemoveItem(item);
                    }
                }
            });
        }

        public void bind(CartItem item) {
            textViewName.setText(item.getName());
            textViewPrice.setText(String.format("R$ %.2f", item.getPrice()));
            textViewQuantity.setText(String.valueOf(item.getQuantity()));
            textViewTotal.setText(String.format("R$ %.2f", item.getTotalPrice()));

            // Aqui você carregaria a imagem usando Glide ou Picasso
            imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
        }
    }
}
