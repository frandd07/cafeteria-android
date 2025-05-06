package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetallePedidoAdapter
        extends RecyclerView.Adapter<UserDetallePedidoAdapter.ViewHolder> {

    private final List<DetallePedido> detalles;
    private final ApiService apiService = ApiClient.getClient().create(ApiService.class);

    public UserDetallePedidoAdapter(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detalle_pedido, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        DetallePedido dp = detalles.get(pos);

        // 1) Nombre × cantidad
        String nombre = dp.getProductos().getNombre();
        h.tvProductoCantidad.setText(
                String.format(Locale.getDefault(), "%s ×%d", nombre, dp.getCantidad())
        );

        // 2) Precio base (unitario × cantidad)
        double lineaBase = dp.getPrecio() * dp.getCantidad();

        // 3) Limpiar el TextView de ingredientes antes de cargar
        h.tvIngredientes.setText("");

        // 4) Obtener extras oficiales del producto
        apiService.obtenerExtrasProducto(dp.getProductoId())
                .enqueue(new Callback<List<DetalleIngrediente>>() {
                    @Override
                    public void onResponse(Call<List<DetalleIngrediente>> call,
                                           Response<List<DetalleIngrediente>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            double extrasSum = 0;
                            StringBuilder sb = new StringBuilder();

                            // Para cada extra que el usuario seleccionó en este pedido...
                            for (DetalleIngrediente elegido : dp.getDetalleIngrediente()) {
                                int selId = elegido.getIngredienteId();
                                // buscar el precio real entre los extras oficiales
                                for (DetalleIngrediente extraOficial : resp.body()) {
                                    if (extraOficial.getIngredienteId() == selId) {
                                        double precioExtra = extraOficial.getPrecio();  // ← USAMOS getPrecio()
                                        extrasSum += precioExtra;
                                        sb.append(extraOficial.getIngredientes().getNombre())
                                                .append(" +")
                                                .append(String.format(Locale.getDefault(),
                                                        "%.2f€", precioExtra))
                                                .append("\n");
                                        break;
                                    }
                                }
                            }

                            // 5) Actualizamos el subtotal (base + extras)
                            double subtotal = lineaBase + extrasSum;
                            h.tvIngredientes.setText(sb.toString().trim());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DetalleIngrediente>> call, Throwable t) {
                        // fallo de red: dejamos sólo el precio base
                    }
                });
    }

    @Override
    public int getItemCount() {
        return detalles != null ? detalles.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvProductoCantidad, tvIngredientes;
        ViewHolder(View iv) {
            super(iv);
            tvProductoCantidad = iv.findViewById(R.id.tvProductoCantidad);
            tvIngredientes     = iv.findViewById(R.id.tvIngredientes);
        }
    }
}
