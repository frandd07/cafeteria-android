package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminPedidoAdapter
        extends RecyclerView.Adapter<AdminPedidoAdapter.ViewHolder> {

    public interface OnActionListener {
        void onAceptar(Pedido pedido);
        void onRechazar(Pedido pedido);
        void onMarcarListo(Pedido pedido);
        void onMarcarRecogido(Pedido pedido);
        void onMarcarPagado(Pedido pedido, boolean pagado);
    }

    private final List<Pedido> lista;
    private final OnActionListener listener;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public AdminPedidoAdapter(List<Pedido> lista, OnActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pedido, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Pedido p = lista.get(pos);
        String estado = p.getEstado().toLowerCase(Locale.ROOT);
        boolean expanded = expandedPositions.contains(pos);

        // toggle content visibility
        h.contentLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);
        h.actionsLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);

        // bind header
        h.tvPedidoId.setText("Pedido #" + p.getId());
        h.tvEstado  .setText(p.getEstado().toUpperCase(Locale.ROOT));
        h.tvAlumno  .setText(p.getUsuario().getNombreCompleto() + " – " + p.getUsuario().getCurso());
        h.tvTotal   .setText(String.format(Locale.getDefault(), "Total: %.2f€", p.getTotal()));

        // clean and bind recreo
        String receroRaw = p.getRecreo();
        String receroClean = receroRaw
                .replace("_", " ")
                .replace("€", "")
                .trim();
        h.tvRecero.setText("Recreo: " + receroClean);

        // header click toggles expansion
        h.headerLayout.setOnClickListener(v -> {
            if (expanded) expandedPositions.remove(pos);
            else           expandedPositions.add(pos);
            notifyItemChanged(pos);
        });

        // if expanded bind inner views
        if (expanded) {
            // switch pagado
            h.switchPagado.setOnCheckedChangeListener(null);
            h.switchPagado.setChecked(p.isPagado());
            h.switchPagado.setOnCheckedChangeListener((btn, chk) ->
                    listener.onMarcarPagado(p, chk)
            );

            // detalle pedido
            AdminDetallePedidoAdapter detalleAdapter =
                    new AdminDetallePedidoAdapter(p.getDetallePedido());
            h.recyclerDetalle.setLayoutManager(
                    new LinearLayoutManager(h.itemView.getContext()));
            h.recyclerDetalle.setAdapter(detalleAdapter);

            // reset buttons
            h.btnAceptar .setVisibility(View.GONE);
            h.btnRechazar.setVisibility(View.GONE);
            h.btnListo   .setVisibility(View.GONE);
            h.btnRecogido.setVisibility(View.GONE);

            switch (estado) {
                case "pendiente":
                    h.btnAceptar .setVisibility(View.VISIBLE);
                    h.btnAceptar .setOnClickListener(v2 -> listener.onAceptar(p));
                    h.btnRechazar.setVisibility(View.VISIBLE);
                    h.btnRechazar.setOnClickListener(v2 -> listener.onRechazar(p));
                    break;
                case "aceptado":
                    h.btnListo.setVisibility(View.VISIBLE);
                    h.btnListo.setOnClickListener(v2 -> listener.onMarcarListo(p));
                    break;
                case "listo":
                    if (p.isPagado()) {
                        h.btnRecogido.setVisibility(View.VISIBLE);
                        h.btnRecogido.setOnClickListener(v2 -> listener.onMarcarRecogido(p));
                    }
                    break;
            }
        }
    }

    @Override public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View headerLayout, contentLayout, actionsLayout;
        final TextView tvPedidoId, tvEstado, tvAlumno, tvTotal, tvRecero;
        final RecyclerView recyclerDetalle;
        final MaterialButton btnAceptar, btnRechazar, btnListo, btnRecogido;
        final SwitchMaterial switchPagado;

        ViewHolder(View itemView) {
            super(itemView);
            headerLayout   = itemView.findViewById(R.id.headerLayout);
            contentLayout  = itemView.findViewById(R.id.contentLayout);
            actionsLayout  = itemView.findViewById(R.id.actionsLayout);
            tvPedidoId     = itemView.findViewById(R.id.tvPedidoId);
            tvEstado       = itemView.findViewById(R.id.tvEstado);
            tvAlumno       = itemView.findViewById(R.id.tvAlumno);
            tvTotal        = itemView.findViewById(R.id.tvTotal);
            tvRecero       = itemView.findViewById(R.id.tvRecero);
            recyclerDetalle= itemView.findViewById(R.id.recyclerDetallePedido);
            btnAceptar     = itemView.findViewById(R.id.btnAceptar);
            btnRechazar    = itemView.findViewById(R.id.btnRechazar);
            btnListo       = itemView.findViewById(R.id.btnListo);
            btnRecogido    = itemView.findViewById(R.id.btnRecogido);
            switchPagado   = itemView.findViewById(R.id.switchPagado);
        }
    }
}
