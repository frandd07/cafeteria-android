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

import java.util.List;
import java.util.Locale;

public class AdminPedidoAdapter
        extends RecyclerView.Adapter<AdminPedidoAdapter.ViewHolder> {

    public interface OnActionListener {
        void onAceptar(Pedido pedido);
        void onRechazar(Pedido pedido);
        void onMarcarListo(Pedido pedido);
        void onMarcarRecogido(Pedido pedido);
        void onMarcarPagado(Pedido pedido, boolean pagado);  // <-- NUEVO
    }

    private final List<Pedido> lista;
    private final OnActionListener listener;

    public AdminPedidoAdapter(List<Pedido> lista, OnActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pedido, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Pedido p = lista.get(pos);
        String estado = p.getEstado().toLowerCase(Locale.ROOT);

        // Cabecera
        h.tvPedidoId.setText("Pedido #" + p.getId());
        h.tvEstado.setText(p.getEstado().toUpperCase(Locale.ROOT));
        h.tvAlumno.setText(p.getUsuario().getNombreCompleto() + " – " + p.getUsuario().getCurso());
        h.tvTotal.setText(String.format(Locale.getDefault(), "Total: %.2f€", p.getTotal()));

        // SwitchPagado: inicializar y listener
        h.switchPagado.setOnCheckedChangeListener(null); // evitar recursividad
        h.switchPagado.setChecked(p.isPagado());
        h.switchPagado.setOnCheckedChangeListener((button, isChecked) -> {
            listener.onMarcarPagado(p, isChecked);
        });

        // Detalle interno
        AdminDetallePedidoAdapter detalleAdapter =
                new AdminDetallePedidoAdapter(p.getDetallePedido());
        h.recyclerDetalle.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        h.recyclerDetalle.setAdapter(detalleAdapter);

        // Ocultar todos los botones inicialmente
        h.btnAceptar.setVisibility(View.GONE);
        h.btnRechazar.setVisibility(View.GONE);
        h.btnListo.setVisibility(View.GONE);
        h.btnRecogido.setVisibility(View.GONE);

        // Mostrar/activar según estado (y pagado)
        switch (estado) {
            case "pendiente":
                h.btnAceptar.setVisibility(View.VISIBLE);
                h.btnAceptar.setOnClickListener(v -> listener.onAceptar(p));

                h.btnRechazar.setVisibility(View.VISIBLE);
                h.btnRechazar.setOnClickListener(v -> listener.onRechazar(p));
                break;

            case "aceptado":
                h.btnListo.setVisibility(View.VISIBLE);
                h.btnListo.setOnClickListener(v -> listener.onMarcarListo(p));
                break;

            case "listo":
                if (p.isPagado()) {
                    h.btnRecogido.setVisibility(View.VISIBLE);
                    h.btnRecogido.setOnClickListener(v -> listener.onMarcarRecogido(p));
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPedidoId, tvEstado, tvAlumno, tvTotal;
        final RecyclerView recyclerDetalle;
        final MaterialButton btnAceptar, btnRechazar, btnListo, btnRecogido;
        final SwitchMaterial switchPagado;  // <-- NUEVO

        ViewHolder(View itemView) {
            super(itemView);
            tvPedidoId      = itemView.findViewById(R.id.tvPedidoId);
            tvEstado        = itemView.findViewById(R.id.tvEstado);
            tvAlumno        = itemView.findViewById(R.id.tvAlumno);
            tvTotal         = itemView.findViewById(R.id.tvTotal);
            recyclerDetalle = itemView.findViewById(R.id.recyclerDetallePedido);
            btnAceptar      = itemView.findViewById(R.id.btnAceptar);
            btnRechazar     = itemView.findViewById(R.id.btnRechazar);
            btnListo        = itemView.findViewById(R.id.btnListo);
            btnRecogido     = itemView.findViewById(R.id.btnRecogido);
            switchPagado    = itemView.findViewById(R.id.switchPagado);  // <-- VINCULAR
        }
    }
}
