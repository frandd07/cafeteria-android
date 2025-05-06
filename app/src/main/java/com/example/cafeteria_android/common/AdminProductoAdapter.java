package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafeteria_android.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para la vista de administrador de productos.
 */
public class AdminProductoAdapter extends RecyclerView.Adapter<AdminProductoAdapter.ViewHolder> {

    public interface OnProductoActionListener {
        void onToggleHabilitado(@NonNull Producto producto, boolean habilitado);
        void onEditar(@NonNull Producto producto);
        void onEliminar(@NonNull Producto producto);
    }

    private final List<Producto> lista = new ArrayList<>();
    private final OnProductoActionListener listener;

    public AdminProductoAdapter(@NonNull OnProductoActionListener listener) {
        this.listener = listener;
    }

    /**
     * Actualiza la lista de productos y refresca el RecyclerView.
     */
    public void actualizarLista(@NonNull List<Producto> nuevos) {
        lista.clear();
        lista.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_producto, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Producto p = lista.get(pos);

        h.tvNombre.setText(p.getNombre());
        h.tvPrecio.setText(String.format("%.2f €", p.getPrecio()));
        Glide.with(h.ivImagen.getContext())
                .load(p.getImagen())
                .placeholder(R.drawable.ic_delete)
                .error(R.drawable.ic_delete)
                .into(h.ivImagen);

        // Switch habilitado/deshabilitado
        h.swHabilitado.setChecked(p.isHabilitado());
        h.swHabilitado.setOnCheckedChangeListener((btn, isChecked) -> {
            if (p.isHabilitado() != isChecked) {
                listener.onToggleHabilitado(p, isChecked);
            }
        });

        // Botones de editar y eliminar
        h.btnEditar.setOnClickListener(v -> listener.onEditar(p));
        h.btnEliminar.setOnClickListener(v -> listener.onEliminar(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final de.hdodenhof.circleimageview.CircleImageView ivImagen;
        final TextView tvNombre, tvPrecio;
        final SwitchMaterial swHabilitado;
        final ImageButton btnEditar, btnEliminar;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImagen     = v.findViewById(R.id.ivProductoAdminImagen);
            tvNombre     = v.findViewById(R.id.tvProductoAdminNombre);
            tvPrecio     = v.findViewById(R.id.tvProductoAdminPrecio);
            swHabilitado = v.findViewById(R.id.swProductoAdminHabilitado);
            btnEditar    = v.findViewById(R.id.btnProductoAdminEditar);
            btnEliminar  = v.findViewById(R.id.btnProductoAdminEliminar);
        }
    }
}
