package com.example.cafeteria_android.common;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafeteria_android.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductoAdapter extends RecyclerView.Adapter<AdminProductoAdapter.ViewHolder> {

    public interface OnProductoActionListener {
        void onToggleHabilitado(@NonNull Producto producto, boolean habilitado);
        void onGestionarIngredientes(@NonNull Producto producto);
        void onEliminar(@NonNull Producto producto);
    }

    private final List<Producto> lista = new ArrayList<>();
    private final OnProductoActionListener listener;

    public AdminProductoAdapter(@NonNull OnProductoActionListener listener) {
        this.listener = listener;
    }

    /** Permite acceder al listado interno para actualizaciones parciales. */
    public List<Producto> getLista() {
        return lista;
    }

    public void actualizarLista(@NonNull List<Producto> nuevos) {
        lista.clear();
        lista.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_producto, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Producto p = lista.get(pos);
        h.tvNombre.setText(p.getNombre());
        h.tvPrecio.setText(String.format(Locale.getDefault(), "%.2f €", p.getPrecio()));
        Glide.with(h.ivImagen.getContext())
                .load(p.getImagen())
                .placeholder(R.drawable.ic_delete)
                .error(R.drawable.ic_delete)
                .into(h.ivImagen);

        // Toggle habilitado/deshabilitado
        h.swHabilitado.setOnCheckedChangeListener(null);
        h.swHabilitado.setChecked(p.isHabilitado());
        h.swHabilitado.setOnCheckedChangeListener((btn, isChecked) -> {
            if (p.isHabilitado() != isChecked) {
                listener.onToggleHabilitado(p, isChecked);
            }
        });

        // Menú de opciones “⋮”
        h.btnOpciones.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_admin_producto, popup.getMenu());

            // Aplica tipografía personalizada
            Typeface font = ResourcesCompat.getFont(v.getContext(), R.font.space_grotesk);
            for (int i = 0; i < popup.getMenu().size(); i++) {
                MenuItem mi = popup.getMenu().getItem(i);
                SpannableString s = new SpannableString(mi.getTitle());
                s.setSpan(new CustomTypefaceSpan(font), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mi.setTitle(s);
            }

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_eliminar:
                        View dlgView = LayoutInflater.from(v.getContext())
                                .inflate(R.layout.dialog_eliminar_producto, null, false);
                        AlertDialog dlg = new MaterialAlertDialogBuilder(
                                v.getContext(),
                                com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
                        )
                                .setView(dlgView)
                                .setCancelable(true)
                                .create();
                        MaterialButton btnCancel = dlgView.findViewById(R.id.btnCancel);
                        MaterialButton btnDelete = dlgView.findViewById(R.id.btnDelete);
                        TextView tvMessage      = dlgView.findViewById(R.id.tvCustomMessage);

                        tvMessage.setText("¿Seguro que deseas eliminar “" + p.getNombre() + "”?");
                        btnCancel.setOnClickListener(x -> dlg.dismiss());
                        btnDelete.setOnClickListener(x -> {
                            listener.onEliminar(p);
                            dlg.dismiss();
                        });
                        dlg.show();
                        return true;

                    case R.id.action_ingredientes:
                        listener.onGestionarIngredientes(p);
                        return true;

                    default:
                        return false;
                }
            });

            popup.show();
        });
    }

    @Override public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final de.hdodenhof.circleimageview.CircleImageView ivImagen;
        final TextView tvNombre, tvPrecio;
        final SwitchMaterial swHabilitado;
        final ImageButton btnOpciones;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImagen     = v.findViewById(R.id.ivProductoAdminImagen);
            tvNombre     = v.findViewById(R.id.tvProductoAdminNombre);
            tvPrecio     = v.findViewById(R.id.tvProductoAdminPrecio);
            swHabilitado = v.findViewById(R.id.swProductoAdminHabilitado);
            btnOpciones  = v.findViewById(R.id.btnOpciones);
        }
    }
}
