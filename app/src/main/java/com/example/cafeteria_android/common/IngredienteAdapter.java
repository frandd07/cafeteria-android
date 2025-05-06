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
import java.util.Locale;
import java.util.Set;

public class IngredienteAdapter
        extends RecyclerView.Adapter<IngredienteAdapter.VH> {

    private final List<Ingrediente> lista = new ArrayList<>();
    private final Set<Integer> selected = new HashSet<>();

    /** Recibe todos los ingredientes y los IDs asignados */
    public void setData(@NonNull List<Ingrediente> all, @NonNull List<Integer> assignedIds) {
        lista.clear();
        lista.addAll(all);
        selected.clear();
        selected.addAll(assignedIds);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_ingrediente_check, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Ingrediente ing = lista.get(pos);
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setText(ing.getNombre());
        h.cb.setChecked(selected.contains(ing.getId()));
        h.cb.setOnCheckedChangeListener((btn, chk) -> {
            if (chk) selected.add(ing.getId());
            else selected.remove(ing.getId());
        });
    }

    @Override public int getItemCount() {
        return lista.size();
    }

    /** Devuelve objetos completos de los seleccionados */
    @NonNull
    public List<Ingrediente> getSeleccionados() {
        List<Ingrediente> out = new ArrayList<>();
        for (Ingrediente ing : lista) {
            if (selected.contains(ing.getId())) out.add(ing);
        }
        return out;
    }

    static class VH extends RecyclerView.ViewHolder {
        final CheckBox cb;
        VH(View v) {
            super(v);
            cb = v.findViewById(R.id.cbIngrediente);
        }
    }
}
