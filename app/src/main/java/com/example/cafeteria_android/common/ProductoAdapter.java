package com.example.cafeteria_android.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafeteria_android.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onAñadirAlCarrito(Producto producto);
    }

    public ProductoAdapter(List<Producto> listaProductos, OnProductoClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        Context context = holder.itemView.getContext();

        holder.productoNombre.setText(producto.getNombre());
        holder.productoPrecio.setText(String.format(Locale.getDefault(), "%.2f €", producto.getPrecio()));

        // ✅ Cargar imagen desde API con Glide
        Glide.with(context)
                .load(producto.getImagen()) // ← aquí viene la URL desde la API
                .placeholder(R.drawable.ic_delete) // opcional, mientras carga
                .error(R.drawable.ic_delete) // opcional, si falla la carga
                .into(holder.productoImagen);

        holder.btnAñadirCarrito.setOnClickListener(v -> listener.onAñadirAlCarrito(producto));
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {

        TextView productoNombre, productoPrecio;
        ImageView productoImagen;
        MaterialButton btnAñadirCarrito;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            productoNombre = itemView.findViewById(R.id.productoNombre);
            productoPrecio = itemView.findViewById(R.id.productoPrecio);
            productoImagen = itemView.findViewById(R.id.productoImagen);
            btnAñadirCarrito = itemView.findViewById(R.id.btnAñadirCarrito);
        }
    }
}
