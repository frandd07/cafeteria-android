package com.example.cafeteria_android.common;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistorialPedidoAdapter extends RecyclerView.Adapter<HistorialPedidoAdapter.HistViewHolder> {
    private static final String AD_TAG = "HIST-ADAPTER";
    private final List<Pedido> pedidos;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
    private final SimpleDateFormat showFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // Set para rastrear las posiciones expandidas
    private final Set<Integer> expandedPositions = new HashSet<>();

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

            // Configurar los datos
            holder.tvId.setText("Pedido #" + pedido.getId());
            holder.tvFecha.setText(fechaFormateada);
            holder.tvUsuario.setText(nombreCompleto);
            holder.tvTotal.setText(String.format(Locale.getDefault(), "Total: %.2f€", pedido.getTotal()));

            // Verificar si este ítem debe estar expandido
            boolean isExpanded = expandedPositions.contains(position);
            holder.expandableContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            if (isExpanded) {
                holder.rvDetalle.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                holder.rvDetalle.setAdapter(new AdminDetallePedidoAdapter(pedido.getDetallePedido()));
            }

            // Actualizar el icono según el estado de expansión
            holder.ivExpandIcon.setImageResource(
                    isExpanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more
            );

            // Configurar el listener para expandir/contraer
            holder.headerLayout.setOnClickListener(v -> {
                // Cambiar el estado de expansión
                if (isExpanded) {
                    expandedPositions.remove(position);
                } else {
                    expandedPositions.add(position);
                }

                // Notificar al adaptador del cambio para animar la transición
                notifyItemChanged(position);
            });

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
        final LinearLayout expandableContent, headerLayout;
        final RecyclerView rvDetalle;
        final ImageView ivExpandIcon;

        HistViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvHistorialId);
            tvFecha = itemView.findViewById(R.id.tvHistorialFecha);
            tvUsuario = itemView.findViewById(R.id.tvHistorialUsuario);
            tvTotal = itemView.findViewById(R.id.tvHistorialTotal);
            expandableContent = itemView.findViewById(R.id.expandableContent);
            headerLayout = itemView.findViewById(R.id.headerLayout);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
            rvDetalle = itemView.findViewById(R.id.rvHistorialDetalle);

            if (tvId == null || tvFecha == null || tvUsuario == null || tvTotal == null ||
                    expandableContent == null || headerLayout == null || ivExpandIcon == null || rvDetalle == null) {
                throw new IllegalStateException("¡Alguna vista es nula! Revisa los IDs en item_historial_pedido.xml");
            }
            Log.d(AD_TAG, "ViewHolder creado correctamente");
        }
    }
}