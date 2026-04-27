package com.example.pos_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos_app.data.Product;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Product> lowStockProducts;

    public NotificationAdapter(List<Product> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Product product = lowStockProducts.get(position);
        holder.tvName.setText(product.name);
        
        if (product.quantity == 0) {
            holder.tvStatus.setText("Out of Stock");
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
            holder.tvQuantity.setText("Empty");
        } else {
            holder.tvStatus.setText("Low Stock");
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            holder.tvQuantity.setText(product.quantity + " left");
        }
    }

    @Override
    public int getItemCount() {
        return lowStockProducts.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvQuantity;
        View statusIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_notif_product_name);
            tvStatus = itemView.findViewById(R.id.tv_notif_status);
            tvQuantity = itemView.findViewById(R.id.tv_notif_quantity);
            statusIndicator = itemView.findViewById(R.id.v_status_indicator);
        }
    }
}
