package com.example.cafeteria_android.common;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialPedidoAdapter extends RecyclerView.Adapter<HistorialPedidoAdapter.HistViewHolder> {
    private static final String AD_TAG = "HIST-ADAPTER";
    private final List<Pedido> pedidos;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
    private final SimpleDateFormat showFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public HistorialPedidoAdapter(List<Pedido> pedidos) {
        this.pedidos = pedidos != null ? pedidos : new ArrayList<>();
        Log.d(AD_TAG, "Adapter creado con " + this.pedidos.size() + " elementos");
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(AD_TAG, "Adapter attached to RecyclerView");
    }

    @NonNull
    @Override
    public HistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(AD_TAG, "onCreateViewHolder");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial_pedido, parent, false);
        return new HistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        Log.d(AD_TAG, "onBindViewHolder pos=" + position + " id=" + pedido.getId());

        try {
            Date fecha = isoFormat.parse(pedido.getCreadoEn());
            String fechaFormateada = fecha != null ? showFormat.format(fecha) : pedido.getCreadoEn();

            Usuario usuario = pedido.getUsuario();
            String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido1();

            holder.tvId.setText("Pedido #" + pedido.getId());
            holder.tvFecha.setText(fechaFormateada);
            holder.tvUsuario.setText(nombreCompleto);
            holder.tvTotal.setText(String.format(Locale.getDefault(), "Total: %.2f€", pedido.getTotal()));

            Log.d(AD_TAG, "→ Bind exitoso de pedido " + pedido.getId());
        } catch (Exception e) {
            Log.e(AD_TAG, "Error binding pedido en posición " + position + ": " + e.getMessage(), e);
            holder.tvId.setText("Pedido #" + pedido.getId());
            holder.tvTotal.setText(String.format(Locale.getDefault(), "Total: %.2f€", pedido.getTotal()));
        }
    }

    @Override
    public int getItemCount() {
        int size = pedidos.size();
        Log.d(AD_TAG, "getItemCount = " + size);
        return size;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull HistViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int pos = holder.getAdapterPosition();
        Log.d(AD_TAG, "ViewHolder ATTACHED pos=" + pos);
    }

    static class HistViewHolder extends RecyclerView.ViewHolder {
        final TextView tvId, tvFecha, tvUsuario, tvTotal;

        HistViewHolder(View itemView) {
            super(itemView);
            tvId      = itemView.findViewById(R.id.tvHistorialId);
            tvFecha   = itemView.findViewById(R.id.tvHistorialFecha);
            tvUsuario = itemView.findViewById(R.id.tvHistorialUsuario);
            tvTotal   = itemView.findViewById(R.id.tvHistorialTotal);

            if (tvId == null || tvFecha == null || tvUsuario == null || tvTotal == null) {
                throw new IllegalStateException("¡Alguna vista es nula! Revisa los IDs en item_historial_pedido.xml");
            }
            Log.d(AD_TAG, "ViewHolder creado correctamente");
        }
    }
}