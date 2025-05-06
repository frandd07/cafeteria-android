package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cafeteria_android.R;

import java.util.List;
import java.util.Locale;

public class AdminDetallePedidoAdapter
        extends RecyclerView.Adapter<AdminDetallePedidoAdapter.ViewHolder> {

    private List<DetallePedido> detalles;

    public AdminDetallePedidoAdapter(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_detalle_pedido, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        DetallePedido dp = detalles.get(pos);

        // Nombre del producto y cantidad
        String nombre = dp.getProductos().getNombre();
        h.tvProductoCantidad.setText(String.format(Locale.getDefault(), "%s ×%d", nombre, dp.getCantidad()));

        // Precio de la línea (precio unitario * cantidad + sum extras)
        double subtotal = dp.getPrecio() * dp.getCantidad();
        double extras = 0;
        for (DetalleIngrediente di : dp.getDetalleIngrediente()) {
            extras += di.getPrecio();
        }
        h.tvPrecioLinea.setText(String.format(Locale.getDefault(), "%.2f€", subtotal + extras));

        // Lista de ingredientes extra
        StringBuilder ingText = new StringBuilder();
        for (DetalleIngrediente di : dp.getDetalleIngrediente()) {
            ingText.append(di.getIngredientes().getNombre())
                    .append(" +")
                    .append(String.format(Locale.getDefault(), "%.2f€", di.getPrecio()))
                    .append("\n");
        }
        // quitamos la última línea vacía
        String listaIng = ingText.toString().trim();
        h.tvIngredientes.setText(listaIng);
    }

    @Override
    public int getItemCount() {
        return detalles != null ? detalles.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductoCantidad, tvPrecioLinea, tvIngredientes;

        ViewHolder(View iv) {
            super(iv);
            tvProductoCantidad = iv.findViewById(R.id.tvProductoCantidad);
            tvPrecioLinea      = iv.findViewById(R.id.tvPrecioLinea);
            tvIngredientes     = iv.findViewById(R.id.tvIngredientes);
        }
    }
}
