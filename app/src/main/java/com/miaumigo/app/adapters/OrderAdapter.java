package com.miaumigo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miaumigo.app.R;
import com.miaumigo.app.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewOrderNumber;
        private TextView textViewOrderDate;
        private TextView textViewOrderStatus;
        private TextView textViewOrderTotal;
        private TextView textViewItemCount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderNumber = itemView.findViewById(R.id.textViewOrderNumber);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewOrderTotal = itemView.findViewById(R.id.textViewOrderTotal);
            textViewItemCount = itemView.findViewById(R.id.textViewItemCount);
        }

        public void bind(Order order) {
            textViewOrderNumber.setText("Pedido #" + order.getId().substring(0, 8));
            textViewOrderTotal.setText(order.getFormattedTotalAmount());
            textViewItemCount.setText(order.getTotalItems() + " item(s)");
            textViewOrderStatus.setText(order.getStatusDisplayName());

            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(order.getOrderDate()));
            textViewOrderDate.setText(formattedDate);

            // Set status color
            int statusColor = getStatusColor(order.getStatus());
            textViewOrderStatus.setTextColor(statusColor);
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return itemView.getContext().getColor(R.color.warning);
                case "processing":
                    return itemView.getContext().getColor(R.color.info);
                case "shipped":
                    return itemView.getContext().getColor(R.color.primary);
                case "delivered":
                    return itemView.getContext().getColor(R.color.success);
                case "cancelled":
                    return itemView.getContext().getColor(R.color.error);
                default:
                    return itemView.getContext().getColor(R.color.text_secondary);
            }
        }
    }
}

