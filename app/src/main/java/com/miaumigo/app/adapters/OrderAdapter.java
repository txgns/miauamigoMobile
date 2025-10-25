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
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private SimpleDateFormat dateFormat;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewOrderId;
        private TextView textViewDate;
        private TextView textViewStatus;
        private TextView textViewTotal;
        private TextView textViewItemCount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            textViewItemCount = itemView.findViewById(R.id.textViewItemCount);
        }

        public void bind(Order order) {
            textViewOrderId.setText("Pedido #" + order.getId());
            textViewDate.setText(dateFormat.format(order.getDate()));
            textViewStatus.setText(order.getStatus());
            textViewTotal.setText(String.format("R$ %.2f", order.getTotal()));
            textViewItemCount.setText(order.getItemCount() + " item(s)");

            // Definir cor do status
            int statusColor = getStatusColor(order.getStatus());
            textViewStatus.setTextColor(statusColor);
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pendente":
                    return itemView.getContext().getColor(R.color.warning);
                case "processando":
                    return itemView.getContext().getColor(R.color.primary);
                case "enviado":
                    return itemView.getContext().getColor(R.color.info);
                case "entregue":
                    return itemView.getContext().getColor(R.color.success);
                case "cancelado":
                    return itemView.getContext().getColor(R.color.error);
                default:
                    return itemView.getContext().getColor(R.color.text_secondary);
            }
        }
    }
}
