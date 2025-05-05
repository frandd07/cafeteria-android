package com.example.cafeteria_android.common;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafeteria_android.R;
import com.google.android.material.button.MaterialButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserPedidoAdapter
        extends RecyclerView.Adapter<UserPedidoAdapter.ViewHolder> {

    public interface OnPedidoActionListener {
        void onEliminar(Pedido pedido);
    }

    private final List<Pedido> pedidos;
    private final OnPedidoActionListener listener;

    private static final SimpleDateFormat IN_FMT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private static final SimpleDateFormat OUT_FMT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public UserPedidoAdapter(List<Pedido> pedidos, OnPedidoActionListener listener) {
        this.pedidos  = pedidos;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_pedido_detailed, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Pedido p = pedidos.get(pos);

        // — ID —
        h.tvPedidoId.setText("Pedido #" + p.getId());

        // — ESTADO —
        String est = p.getEstado().toLowerCase(Locale.ROOT);
        h.tvEstado.setText(p.getEstado().toUpperCase(Locale.ROOT));
        int bg;
        switch (est) {
            case "pendiente": bg = Color.parseColor("#FFA000"); break;
            case "aceptado":  bg = Color.parseColor("#1976D2"); break;
            case "listo":     bg = Color.parseColor("#388E3C"); break;
            case "rechazado": bg = Color.parseColor("#D32F2F"); break;
            default:          bg = Color.parseColor("#9E9E9E"); break;
        }
        h.tvEstado.setBackgroundTintList(ColorStateList.valueOf(bg));

        // — FECHA —
        try {
            Date d = IN_FMT.parse(p.getCreadoEn());
            h.tvFecha.setText(OUT_FMT.format(d));
        } catch (ParseException ex) {
            h.tvFecha.setText(p.getCreadoEn());
        }

        // — PAGADO —
        if (p.isPagado()) {
            h.tvPagado.setText("Pagado");
            h.tvPagado.setTextColor(Color.parseColor("#388E3C"));
        } else {
            h.tvPagado.setText("No pagado");
            h.tvPagado.setTextColor(Color.parseColor("#D32F2F"));
        }

        // — TOTAL REcalculado —
        double totalCalc = 0;
        for (DetallePedido dp : p.getDetallePedido()) {
            double linea = dp.getPrecio() * dp.getCantidad();
            for (DetalleIngrediente di : dp.getDetalleIngrediente()) {
                // usamos getPrecio() que mapea precio_extra
                linea += di.getPrecio();
            }
            totalCalc += linea;
        }
        h.tvTotal.setText(String.format(Locale.getDefault(), "%.2f€", totalCalc));

        // — LISTADO INTERNO —
        h.rvDetalle.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        h.rvDetalle.setAdapter(new UserDetallePedidoAdapter(p.getDetallePedido()));

        // — BOTÓN ELIMINAR (solo en rechazado) —
        if ("rechazado".equals(est)) {
            h.btnEliminar.setVisibility(View.VISIBLE);
            h.btnEliminar.setOnClickListener(v -> listener.onEliminar(p));
        } else {
            h.btnEliminar.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() {
        return pedidos != null ? pedidos.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPedidoId, tvEstado, tvFecha, tvPagado, tvTotal;
        final RecyclerView rvDetalle;
        final MaterialButton btnEliminar;

        ViewHolder(@NonNull View iv) {
            super(iv);
            tvPedidoId  = iv.findViewById(R.id.tvPedidoId);
            tvEstado    = iv.findViewById(R.id.tvEstado);
            tvFecha     = iv.findViewById(R.id.tvFecha);
            tvPagado    = iv.findViewById(R.id.tvPagado);
            tvTotal     = iv.findViewById(R.id.tvTotal);
            rvDetalle   = iv.findViewById(R.id.rvDetallePedido);
            btnEliminar = iv.findViewById(R.id.btnEliminar);
        }
    }
}
