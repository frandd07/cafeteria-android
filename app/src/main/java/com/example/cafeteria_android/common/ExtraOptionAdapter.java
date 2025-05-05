package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cafeteria_android.R;

import java.util.ArrayList;
import java.util.List;

public class ExtraOptionAdapter
        extends RecyclerView.Adapter<ExtraOptionAdapter.VH> {

    private final List<DetalleIngrediente> opciones;
    private final List<DetalleIngrediente> seleccionados = new ArrayList<>();

    public ExtraOptionAdapter(List<DetalleIngrediente> initialOpciones) {
        // Creamos una lista mutable a partir de la que nos pasen
        this.opciones = new ArrayList<>(initialOpciones != null
                ? initialOpciones
                : List.of());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_extra_option, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DetalleIngrediente di = opciones.get(position);
        holder.cb.setText(di.getIngredientes().getNombre()
                + " (+" + String.format("%.2f€", di.getPrecio()) + ")");

        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(seleccionados.contains(di));
        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                seleccionados.add(di);
            } else {
                seleccionados.remove(di);
            }
        });
    }

    @Override
    public int getItemCount() {
        return opciones.size();
    }

    /** Actualiza la lista de opciones y limpia selecciones */
    public void updateOptions(List<DetalleIngrediente> nuevasOpciones) {
        opciones.clear();
        if (nuevasOpciones != null) {
            opciones.addAll(nuevasOpciones);
        }
        seleccionados.clear();
        notifyDataSetChanged();
    }

    /** Devuelve los extras marcados */
    public List<DetalleIngrediente> getSeleccionados() {
        return new ArrayList<>(seleccionados);
    }

    static class VH extends RecyclerView.ViewHolder {
        final CheckBox cb;
        VH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbExtra);
        }
    }
}
