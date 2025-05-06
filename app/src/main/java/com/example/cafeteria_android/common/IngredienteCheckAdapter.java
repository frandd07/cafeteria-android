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

/**
 * Adapter para mostrar una lista de DetalleIngrediente con checkbox.
 * Permite seleccionar varios ingredientes y luego recuperar sus IDs.
 */
public class IngredienteCheckAdapter
        extends RecyclerView.Adapter<IngredienteCheckAdapter.ViewHolder> {

    private List<DetalleIngrediente> ingredientes = new ArrayList<>();
    private final Set<Integer> seleccionados = new HashSet<>();

    public IngredienteCheckAdapter(List<DetalleIngrediente> listaInicial) {
        if (listaInicial != null) {
            ingredientes = listaInicial;
        }
    }

    /** Actualiza la lista completa y refresca la UI */
    public void actualizar(List<DetalleIngrediente> nuevaLista) {
        this.ingredientes = (nuevaLista != null ? nuevaLista : new ArrayList<>());
        seleccionados.clear();  // vaciamos la selección
        notifyDataSetChanged();
    }

    /** Devuelve la lista de IDs de ingredientes marcados */
    public List<Integer> getSeleccionados() {
        return new ArrayList<>(seleccionados);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_check, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetalleIngrediente di = ingredientes.get(position);
        String nombre       = di.getIngredientes().getNombre();
        double precioExtra  = di.getPrecio();               // <-- ahora sí existe
        int idIng           = di.getIngredienteId();

        holder.chk.setText(
                String.format(Locale.getDefault(), "%s  +%.2f€", nombre, precioExtra)
        );

        // Reset listener antes de cambiar el estado
        holder.chk.setOnCheckedChangeListener(null);
        holder.chk.setChecked(seleccionados.contains(idIng));
        holder.chk.setOnCheckedChangeListener((cb, checked) -> {
            if (checked)   seleccionados.add(idIng);
            else           seleccionados.remove(idIng);
        });
    }

    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox chk;
        ViewHolder(@NonNull View v) {
            super(v);
            chk = v.findViewById(R.id.cbIngrediente);
        }
    }
}
