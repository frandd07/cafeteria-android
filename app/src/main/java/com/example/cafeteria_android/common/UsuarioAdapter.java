package com.example.cafeteria_android.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    /** Callbacks que ya tenías */
    public interface OnUsuarioActionListener {
        void onVerificar(int position, @NonNull Usuario usuario);
        void onRechazar(int position, @NonNull Usuario usuario);
    }

    /** Nuevo callback para la selección múltiple */
    public interface OnMultiSelectListener {
        void onSelectionChanged(List<String> selectedIds);
    }

    private List<Usuario> usuarios;
    private final OnUsuarioActionListener actionListener;
    private final OnMultiSelectListener multiSelectListener;

    private final Set<Integer> expandedPositions = new HashSet<>();
    private final Set<Integer> selectedPositions = new HashSet<>();

    public UsuarioAdapter(
            List<Usuario> usuarios,
            OnUsuarioActionListener actionListener,
            OnMultiSelectListener multiSelectListener
    ) {
        this.usuarios = usuarios;
        this.actionListener = actionListener;
        this.multiSelectListener = multiSelectListener;
    }

    @NonNull @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder h, int pos) {
        Usuario u = usuarios.get(pos);
        boolean expanded = expandedPositions.contains(pos);
        boolean selected = selectedPositions.contains(pos);

        // 1) Pintar selección si toca
        if (selected) {
            h.cardRoot.setCardBackgroundColor(
                    ContextCompat.getColor(h.cardRoot.getContext(), R.color.user_selected_yellow)
            );
        } else {
            // 2) Si no está seleccionado, lógica original de colores por estado
            if (!u.isVerificado()) {
                h.cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(h.cardRoot.getContext(), R.color.user_unverified_red));
            } else if (u.isDebe_actualizar_curso()) {
                h.cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(h.cardRoot.getContext(), R.color.user_update_gray));
            } else {
                h.cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(h.cardRoot.getContext(), android.R.color.white));
            }
        }

        int green       = ContextCompat.getColor(h.itemView.getContext(), R.color.successColor);
        int defaultText = ContextCompat.getColor(h.itemView.getContext(), R.color.black);

        // Cabecera
        h.nombre.setText(u.getNombreCompleto());
        h.email .setText(u.getEmail());
        h.ivArrow.setRotation(expanded ? 180f : 0f);

// Expandible
        h.contentLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);

// Mostrar/ocultar botones de acción únicamente si:
//  1) item expandido, 2) no hay selección múltiple, 3) usuario NO verificado
        boolean showActions = expanded
                && selectedPositions.isEmpty()
                && !u.isVerificado();
        h.layoutAccion.setVisibility(showActions ? View.VISIBLE : View.GONE);


        // Listener de clicks:
        h.headerLayout.setOnLongClickListener(v -> {
            toggleSelection(pos);
            return true;
        });
        h.headerLayout.setOnClickListener(v -> {
            if (!selectedPositions.isEmpty()) {
                // Si hay selección activa, el click alterna selección
                toggleSelection(pos);
            } else {
                // Comportamiento original de expandir/contraer
                if (expanded) expandedPositions.remove(pos);
                else           expandedPositions.add(pos);
                notifyItemChanged(pos);
            }
        });

        // Datos extra sólo si está expandido
        if (expanded) {
            h.tipo.setText(u.getTipo());
            String tipoLower = u.getTipo().toLowerCase(Locale.ROOT);
            if (tipoLower.equals("profesor") || tipoLower.equals("personal")) {
                h.layoutCurso.setVisibility(View.GONE);
            } else {
                h.layoutCurso.setVisibility(View.VISIBLE);
                h.curso.setText(
                        (u.getCurso() == null || u.getCurso().isEmpty()) ? "-" : u.getCurso()
                );
            }

            // Verificado vs no verificado
            if (u.isVerificado()) {
                h.verificado.setText("Sí");
                h.verificado.setTextColor(green);
                h.ivVerificado.setColorFilter(green);
            } else {
                h.verificado.setText("No");
                h.verificado.setTextColor(defaultText);
                h.ivVerificado.setColorFilter(defaultText);
                // Botones de acción
                h.btnVerificar.setOnClickListener(x -> actionListener.onVerificar(pos, u));
                h.btnRechazar .setOnClickListener(x -> actionListener.onRechazar(pos, u));
            }
        }
    }

    @Override public int getItemCount() {
        return usuarios != null ? usuarios.size() : 0;
    }

    /** Actualizar lista entera */
    public void actualizarLista(List<Usuario> nuevaLista) {
        this.usuarios = nuevaLista;
        notifyDataSetChanged();
    }

    /** Remover 1 posición (borrado individual) */
    public void removerEn(int position) {
        usuarios.remove(position);
        notifyItemRemoved(position);
    }

    /** Marcar verificado en UI */
    public void marcarVerificadoEn(int position) {
        usuarios.get(position).setVerificado(true);
        notifyItemChanged(position);
    }

    /** Limpia selección múltiple al cerrar ActionMode */
    public void clearSelection() {
        List<Integer> old = new ArrayList<>(selectedPositions);
        selectedPositions.clear();
        for (int p : old) {
            notifyItemChanged(p);
        }
    }

    /** Toggle de selección y notificación al fragment */
    private void toggleSelection(int pos) {
        if (selectedPositions.contains(pos)) selectedPositions.remove(pos);
        else selectedPositions.add(pos);
        notifyItemChanged(pos);
        // Enviamos IDs seleccionados
        List<String> ids = selectedPositions.stream()
                .map(i -> usuarios.get(i).getId())
                .collect(Collectors.toList());
        multiSelectListener.onSelectionChanged(ids);
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardRoot;
        final View headerLayout, contentLayout;
        final TextView nombre, email, tipo, curso, verificado;
        final ImageView ivVerificado, ivArrow;
        final LinearLayout layoutCurso, layoutAccion;
        final Button btnVerificar, btnRechazar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot     = itemView.findViewById(R.id.cardRoot);
            headerLayout = itemView.findViewById(R.id.headerLayout);
            contentLayout= itemView.findViewById(R.id.contentLayout);

            nombre       = itemView.findViewById(R.id.tvNombre);
            email        = itemView.findViewById(R.id.tvEmail);
            tipo         = itemView.findViewById(R.id.tvTipo);
            curso        = itemView.findViewById(R.id.tvCurso);
            verificado   = itemView.findViewById(R.id.tvVerificado);
            ivVerificado = itemView.findViewById(R.id.ivVerificado);
            ivArrow      = itemView.findViewById(R.id.ivExpandIcon);

            layoutCurso  = itemView.findViewById(R.id.layoutCurso);
            layoutAccion = itemView.findViewById(R.id.layoutAccion);

            btnVerificar = itemView.findViewById(R.id.btnAceptar);
            btnRechazar  = itemView.findViewById(R.id.btnRechazar);
        }
    }
}
