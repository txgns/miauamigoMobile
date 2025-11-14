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

    private List<Order> orderList;
    private SimpleDateFormat dateFormat;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
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
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
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
            textViewOrderId.setText("Pedido #" + (order.getId() != null ? order.getId().substring(0, Math.min(8, order.getId().length())) : "N/A"));
            textViewDate.setText(dateFormat.format(new Date(order.getCreatedAt())));
            textViewStatus.setText(order.getStatus());
            textViewTotal.setText(String.format("R$ %.2f", order.getTotal()));
            textViewItemCount.setText(order.getTotalQuantity() + " item(s)");

            // Definir background e cor do texto do status
            setStatusStyle(order.getStatus());
        }

        private void setStatusStyle(String status) {
            if (status == null) {
                textViewStatus.setBackgroundResource(R.drawable.status_default);
                textViewStatus.setTextColor(itemView.getContext().getColor(R.color.white));
                return;
            }
            
            // Sempre usar texto branco para melhor contraste
            textViewStatus.setTextColor(itemView.getContext().getColor(R.color.white));
            
            switch (status.toLowerCase()) {
                case "pendente":
                    textViewStatus.setBackgroundResource(R.drawable.status_pendente);
                    break;
                case "processando":
                    textViewStatus.setBackgroundResource(R.drawable.status_processando);
                    break;
                case "enviado":
                    textViewStatus.setBackgroundResource(R.drawable.status_enviado);
                    break;
                case "entregue":
                    textViewStatus.setBackgroundResource(R.drawable.status_entregue);
                    break;
                case "cancelado":
                    textViewStatus.setBackgroundResource(R.drawable.status_cancelado);
                    break;
                default:
                    textViewStatus.setBackgroundResource(R.drawable.status_default);
                    break;
            }
        }
    }
}
