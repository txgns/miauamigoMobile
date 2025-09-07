package com.miaumigo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.miaumigo.app.R;
import com.miaumigo.app.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onQuantityChanged(CartItem cartItem, int newQuantity);
        void onRemoveItem(CartItem cartItem);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewQuantity;
        private MaterialButton buttonDecrease;
        private MaterialButton buttonIncrease;
        private MaterialButton buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }

        public void bind(CartItem cartItem, OnCartItemClickListener listener) {
            textViewName.setText(cartItem.getProductName());
            textViewPrice.setText(cartItem.getFormattedTotalPrice());
            textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));

            // Load product image
            if (cartItem.getProductImage() != null && !cartItem.getProductImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(cartItem.getProductImage())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            // Quantity buttons
            buttonDecrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() - 1;
                if (newQuantity >= 0) {
                    listener.onQuantityChanged(cartItem, newQuantity);
                }
            });

            buttonIncrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() + 1;
                listener.onQuantityChanged(cartItem, newQuantity);
            });

            // Remove button
            buttonRemove.setOnClickListener(v -> {
                listener.onRemoveItem(cartItem);
            });
        }
    }
}

