package com.example.cafeteria_android.common;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredienteAsignacionAdapter
        extends RecyclerView.Adapter<IngredienteAsignacionAdapter.VH> {

    public interface OnDataChanged {
        void onSelectionOrPriceChanged();
    }

    private final List<Ingrediente> ingredientes;
    private final Map<Integer, Boolean> seleccion = new HashMap<>();
    private final Map<Integer, Double> precios     = new HashMap<>();
    private final OnDataChanged listener;

    public IngredienteAsignacionAdapter(
            @NonNull List<Ingrediente> datos,
            @NonNull List<Integer> assignedIds,
            @NonNull OnDataChanged listener
    ) {
        this.ingredientes = datos;
        this.listener     = listener;
        for (Ingrediente i : datos) {
            int id = i.getId();
            seleccion.put(id, assignedIds.contains(id));
            precios.put(id, i.getPrecio());
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_asignacion, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Ingrediente ing = ingredientes.get(pos);
        int id = ing.getId();

        h.cb.setText(ing.getNombre());
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(seleccion.get(id));
        h.cb.setOnCheckedChangeListener((btn, isChecked) -> {
            seleccion.put(id, isChecked);
            listener.onSelectionOrPriceChanged();
        });

        h.etPrecio.setOnEditorActionListener(null);
        h.etPrecio.setText(String.format("%.2f", precios.get(id)));
        if (h.watcher != null) h.etPrecio.removeTextChangedListener(h.watcher);
        h.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                double p = 0;
                try { p = Double.parseDouble(s.toString()); }
                catch (NumberFormatException ignored) {}
                precios.put(id, p);
                listener.onSelectionOrPriceChanged();
            }
        };
        h.etPrecio.addTextChangedListener(h.watcher);
    }

    @Override public int getItemCount() {
        return ingredientes.size();
    }

    /** Devuelve sólo los seleccionados */
    public List<Integer> getSeleccionados() {
        List<Integer> out = new java.util.ArrayList<>();
        for (Map.Entry<Integer, Boolean> e : seleccion.entrySet()) {
            if (e.getValue()) out.add(e.getKey());
        }
        return out;
    }

    /** Mapa id→precio actual */
    public Map<Integer, Double> getPreciosActualizados() {
        return new HashMap<>(precios);
    }

    static class VH extends RecyclerView.ViewHolder {
        final CheckBox cb;
        final EditText etPrecio;
        TextWatcher watcher;
        VH(View v) {
            super(v);
            cb = v.findViewById(R.id.cbIngrediente);
            etPrecio = v.findViewById(R.id.etPrecioIngrediente);
        }
    }
}
