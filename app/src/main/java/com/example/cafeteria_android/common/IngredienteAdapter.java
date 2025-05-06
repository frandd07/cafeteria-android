package com.example.cafeteria_android.common;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafeteria_android.R;
import com.example.cafeteria_android.common.Ingrediente;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngredienteAdapter
        extends RecyclerView.Adapter<IngredienteAdapter.VH> {

    private final List<Ingrediente> lista = new ArrayList<>();
    private final Set<Integer> seleccionados = new HashSet<>();

    public void setData(List<Ingrediente> all, List<Integer> assignedIds) {
        lista.clear();
        lista.addAll(all);
        seleccionados.clear();
        seleccionados.addAll(assignedIds);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_ingrediente_check, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Ingrediente ing = lista.get(pos);
        h.cb.setText(ing.getNombre());
        h.cb.setChecked(seleccionados.contains(ing.getId()));
        h.cb.setOnCheckedChangeListener((b, chk) -> {
            if (chk) seleccionados.add(ing.getId());
            else       seleccionados.remove(ing.getId());
        });
    }

    @Override public int getItemCount() { return lista.size(); }

    /** Devuelve los objetos completos de los seleccionados */
    public List<Ingrediente> getSeleccionados() {
        List<Ingrediente> sel = new ArrayList<>();
        for (Ingrediente ing : lista) {
            if (seleccionados.contains(ing.getId())) sel.add(ing);
        }
        return sel;
    }

    static class VH extends RecyclerView.ViewHolder {
        final CheckBox cb;
        VH(View v) {
            super(v);
            cb = v.findViewById(R.id.cbIngrediente);
        }
    }
}
