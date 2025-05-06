package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafeteria_android.R;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> items;

    public CartAdapter(List<CartItem> items) {
        this.items = items;
    }

    /** Actualiza la lista y refresca el RecyclerView */
    public void setItems(List<CartItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CartItem item = items.get(pos);

        // Carga la imagen del producto
        Glide.with(h.ivImagen.getContext())
                .load(item.getProducto().getImagen())
                .into(h.ivImagen);

        // Nombre del producto
        h.tvNombre.setText(item.getProducto().getNombre());

        // Subtotal
        h.tvSubtotal.setText(String.format(Locale.getDefault(),
                "Subtotal: %.2f€", item.getSubtotal()));

        // Ingredientes extra
        List<DetalleIngrediente> extras = item.getExtras();
        if (extras == null || extras.isEmpty()) {
            h.tvExtras.setVisibility(View.GONE);
        } else {
            h.tvExtras.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (DetalleIngrediente di : extras) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(di.getIngredientes().getNombre())
                        .append(" +")
                        .append(String.format(Locale.getDefault(), "%.2f€", di.getPrecio()));

            }
            h.tvExtras.setText("Extras: " + sb.toString());
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImagen;
        final TextView tvNombre, tvSubtotal, tvExtras;

        ViewHolder(View iv) {
            super(iv);
            ivImagen  = iv.findViewById(R.id.ivProductoImagen);
            tvNombre   = iv.findViewById(R.id.tvNombreProducto);
            tvSubtotal = iv.findViewById(R.id.tvSubtotal);
            tvExtras   = iv.findViewById(R.id.tvExtras);
        }
    }
}
