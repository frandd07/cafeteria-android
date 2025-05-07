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
    private final Set<Integer> seleccionados = new HashSet<>();

    /** Rellena todos y marca los ya asignados */
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
        h.cb.setOnCheckedChangeListener(null);

        // Nombre + precio
        String txt = String.format(
                Locale.getDefault(),
                "%s  (+%.2f€)",
                ing.getNombre(),
                ing.getPrecio()     // <-- aquí usamos getPrecio()
        );
        h.cb.setText(txt);

        h.cb.setChecked(seleccionados.contains(ing.getId()));
        h.cb.setOnCheckedChangeListener((btn, chk) -> {
            if (chk) seleccionados.add(ing.getId());
            else     seleccionados.remove(ing.getId());
        });
    }

    @Override public int getItemCount() {
        return lista.size();
    }

    /** Ingredientes completos seleccionados */
    @NonNull
    public List<Ingrediente> getSeleccionados() {
        List<Ingrediente> out = new ArrayList<>();
        for (Ingrediente ing : lista) {
            if (seleccionados.contains(ing.getId())) {
                out.add(ing);
            }
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

    public void actualizarLista(@NonNull List<Ingrediente> nuevos) {
        lista.clear();
        lista.addAll(nuevos);
        // opcional: limpiamos la selección si no la necesitas aquí
        seleccionados.clear();
        notifyDataSetChanged();
    }
}
