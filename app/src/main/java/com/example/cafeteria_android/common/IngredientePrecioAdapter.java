package com.example.cafeteria_android.common;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientePrecioAdapter
        extends RecyclerView.Adapter<IngredientePrecioAdapter.VH> {

    public interface OnPrecioChangedListener {
        void onPrecioChanged(int ingredienteId, double nuevoPrecio);
    }

    private final List<Ingrediente> lista;
    private final Map<Integer, Double> precios = new HashMap<>();
    private final OnPrecioChangedListener listener;

    public IngredientePrecioAdapter(@NonNull List<Ingrediente> datos,
                                    @NonNull OnPrecioChangedListener listener) {
        this.lista = datos;
        this.listener = listener;
        for (Ingrediente i : datos) {
            precios.put(i.getId(), i.getPrecio());
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_precio, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Ingrediente ing = lista.get(pos);
        h.tvNombre.setText(ing.getNombre());
        h.etPrecio.setText(String.format("%.2f", precios.get(ing.getId())));

        if (h.watcher != null) {
            h.etPrecio.removeTextChangedListener(h.watcher);
        }
        h.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                double precio = 0;
                try { precio = Double.parseDouble(s.toString()); }
                catch (NumberFormatException ignored) {}
                precios.put(ing.getId(), precio);
                listener.onPrecioChanged(ing.getId(), precio);
            }
        };
        h.etPrecio.addTextChangedListener(h.watcher);
    }

    @Override public int getItemCount() {
        return lista.size();
    }

    /** Copia del mapa id→precio actualizado */
    @NonNull
    public Map<Integer, Double> getPreciosActualizados() {
        return new HashMap<>(precios);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvNombre;
        final EditText etPrecio;
        TextWatcher watcher;

        VH(View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvIngredienteNombre);
            etPrecio = v.findViewById(R.id.etIngredientePrecio);
        }
    }
}
