package com.example.cafeteria_android.common;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientePrecioAdapter
        extends RecyclerView.Adapter<IngredientePrecioAdapter.VH> {

    /** Callback para cuando quieren eliminar un ingrediente */
    public interface OnDeleteListener {
        void onDelete(int ingredienteId, int position);
    }

    private final List<Ingrediente> ingredientes = new ArrayList<>();
    private final Map<Integer, Double> precios = new HashMap<>();
    private final OnDeleteListener deleteListener;

    public IngredientePrecioAdapter(
            List<Ingrediente> listaInicial,
            Map<Integer, Double> preciosIniciales,
            OnDeleteListener deleteListener
    ) {
        this.deleteListener = deleteListener;
        // copia defensiva
        this.ingredientes.addAll(listaInicial);
        this.precios.putAll(preciosIniciales);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_precio, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Ingrediente ing = ingredientes.get(position);
        holder.tvNombre.setText(ing.getNombre());
        holder.etPrecio.setText(String.valueOf(precios.get(ing.getId())));

        // TextWatcher para ir actualizando el mapa de precios
        if (holder.watcher != null) {
            holder.etPrecio.removeTextChangedListener(holder.watcher);
        }
        holder.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                try {
                    double val = Double.parseDouble(s.toString());
                    precios.put(ing.getId(), val);
                } catch (NumberFormatException e) {
                    // no válido, lo ignoramos
                }
            }
        };
        holder.etPrecio.addTextChangedListener(holder.watcher);

        // Botón de eliminar
        holder.btnEliminar.setOnClickListener(v ->
                deleteListener.onDelete(ing.getId(), holder.getAdapterPosition())
        );
    }

    @Override public int getItemCount() {
        return ingredientes.size();
    }

    /** Permite al BottomSheet obtener la tabla actualizada de precios */
    public Map<Integer, Double> getPreciosActualizados() {
        return new HashMap<>(precios);
    }

    /**
     * Elimina la fila en la posición dada y notifica al RecyclerView.
     * Llama a este método después de que la llamada DELETE al servidor haya sido exitosa.
     */
    public void removeAt(int position) {
        int id = ingredientes.get(position).getId();
        ingredientes.remove(position);
        precios.remove(id);
        notifyItemRemoved(position);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvNombre;
        final EditText etPrecio;
        final ImageButton btnEliminar;
        TextWatcher watcher;

        VH(View v) {
            super(v);
            tvNombre    = v.findViewById(R.id.tvIngredienteNombre);
            etPrecio    = v.findViewById(R.id.etIngredientePrecio);
            btnEliminar = v.findViewById(R.id.btnEliminarIngrediente);
        }
    }
}
