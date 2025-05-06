package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngredienteCheckAdapter
        extends RecyclerView.Adapter<IngredienteCheckAdapter.VH> {

    private final List<Ingrediente> lista = new ArrayList<>();
    private final Set<Integer> seleccionados = new HashSet<>();

    public void setData(@NonNull List<Ingrediente> all, @NonNull List<Integer> assignedIds) {
        lista.clear();
        lista.addAll(all);
        seleccionados.clear();
        seleccionados.addAll(assignedIds);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_check, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Ingrediente ing = lista.get(pos);
        h.cb.setText(ing.getNombre() + String.format(" (+%.2f€)", ing.getPrecio()));
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(seleccionados.contains(ing.getId()));
        h.cb.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) seleccionados.add(ing.getId());
            else           seleccionados.remove(ing.getId());
        });
    }

    @Override public int getItemCount() {
        return lista.size();
    }

    /** IDs seleccionados para enviar al servidor */
    @NonNull
    public List<Integer> getSeleccionados() {
        return new ArrayList<>(seleccionados);
    }

    static class VH extends RecyclerView.ViewHolder {
        final CheckBox cb;
        VH(View v) {
            super(v);
            cb = v.findViewById(R.id.cbIngrediente);
        }
    }
}
