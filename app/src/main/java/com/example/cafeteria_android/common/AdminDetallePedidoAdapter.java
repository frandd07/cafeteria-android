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
import java.util.stream.Collectors;

public class AdminDetallePedidoAdapter
        extends RecyclerView.Adapter<AdminDetallePedidoAdapter.ViewHolder> {

    private final List<DetallePedido> detalles;

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
        h.tvProductoCantidad.setText(
                String.format(Locale.getDefault(), "%s ×%d", nombre, dp.getCantidad())
        );

        // Precio de la línea: dp.getPrecio() YA incluye extras
        double linePrice = dp.getPrecio() * dp.getCantidad();
        h.tvPrecioLinea.setText(
                String.format(Locale.getDefault(), "%.2f€", linePrice)
        );

        // Mostrar solo lista de nombres de extras
        List<DetalleIngrediente> extras = dp.getDetalleIngrediente();
        if (extras != null && !extras.isEmpty()) {
            String ingList = extras.stream()
                    .map(di -> di.getIngredientes().getNombre())
                    .collect(Collectors.joining(", "));
            h.tvIngredientes.setText(ingList);
        } else {
            h.tvIngredientes.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return detalles != null ? detalles.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvProductoCantidad, tvPrecioLinea, tvIngredientes;

        ViewHolder(View iv) {
            super(iv);
            tvProductoCantidad = iv.findViewById(R.id.tvProductoCantidad);
            tvPrecioLinea      = iv.findViewById(R.id.tvPrecioLinea);
            tvIngredientes     = iv.findViewById(R.id.tvIngredientes);
        }
    }
}
