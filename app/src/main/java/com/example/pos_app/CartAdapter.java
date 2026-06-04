package com.example.pos_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos_app.data.CartItem;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onRemoveItem(int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
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
        holder.tvName.setText(item.product.name);
        String currencySymbol = CurrencyUtils.getCurrencySymbol(holder.itemView.getContext());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%s%.2f x %d", currencySymbol, item.product.price, item.quantity));
        holder.tvTotal.setText(CurrencyUtils.formatAmount(holder.itemView.getContext(), item.getTotalPrice()));

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvTotal;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_cart_item_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_item_price);
            tvTotal = itemView.findViewById(R.id.tv_cart_item_total);
            btnRemove = itemView.findViewById(R.id.btn_remove_cart);
        }
    }
}
