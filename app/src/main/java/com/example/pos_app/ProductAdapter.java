package com.example.pos_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos_app.data.Product;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;
    private boolean isExpanded = false;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (products == null || position >= products.size()) return;
        Product product = products.get(position);
        
        // Use null checks to prevent crashes if IDs are missing from the layout
        if (holder.tvName != null) {
            holder.tvName.setText(product.name != null ? product.name : "Unknown");
        }
        if (holder.tvStock != null) {
            holder.tvStock.setText(String.format(Locale.getDefault(), "Stock: %d", product.quantity));
        }
        if (holder.tvPrice != null) {
            holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.price));
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (products == null) return 0;
        if (isExpanded) {
            return products.size();
        } else {
            return Math.min(products.size(), 5);
        }
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStock, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvStock = itemView.findViewById(R.id.tv_product_stock);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
