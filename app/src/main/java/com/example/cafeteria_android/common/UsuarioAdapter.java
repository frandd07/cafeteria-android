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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    public interface OnUsuarioActionListener {
        void onVerificar(int position, @NonNull Usuario usuario);
        void onRechazar(int position, @NonNull Usuario usuario);
    }

    private List<Usuario> usuarios;
    private final OnUsuarioActionListener listener;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public UsuarioAdapter(List<Usuario> usuarios, OnUsuarioActionListener listener) {
        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder h, int pos) {
        Usuario usuario = usuarios.get(pos);
        boolean expanded = expandedPositions.contains(pos);

        // --- Colorear fondo del CardView según estado ---
        if (!usuario.isVerificado()) {
            // No verificados → rojo claro
            h.cardRoot.setCardBackgroundColor(
                    ContextCompat.getColor(h.cardRoot.getContext(), R.color.user_unverified_red));
        } else if (usuario.isDebe_actualizar_curso()) {
            // Deben actualizar → gris claro
            h.cardRoot.setCardBackgroundColor(
                    ContextCompat.getColor(h.cardRoot.getContext(), R.color.user_update_gray));
        } else {
            // Resto → blanco
            h.cardRoot.setCardBackgroundColor(
                    ContextCompat.getColor(h.cardRoot.getContext(), android.R.color.white));
        }
        // ------------------------------------------------------

        int green       = ContextCompat.getColor(h.itemView.getContext(), R.color.successColor);
        int defaultText = ContextCompat.getColor(h.itemView.getContext(), R.color.black);

        // Cabecera
        h.nombre.setText(usuario.getNombreCompleto());
        h.email .setText(usuario.getEmail());

        // Rotar flecha
        h.ivArrow.setRotation(expanded ? 180f : 0f);

        // Contenido expandible
        h.contentLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);
        h.layoutAccion .setVisibility(expanded ? View.VISIBLE : View.GONE);

        // Toggle al hacer click en la cabecera
        h.headerLayout.setOnClickListener(v -> {
            if (expanded) expandedPositions.remove(pos);
            else           expandedPositions.add(pos);
            notifyItemChanged(pos);
        });

        // Si está expandido, cargamos los datos adicionales
        if (expanded) {
            h.tipo.setText(usuario.getTipo());

            String tipoLower = usuario.getTipo().toLowerCase(Locale.ROOT);
            if (tipoLower.equals("profesor") || tipoLower.equals("personal")) {
                h.layoutCurso.setVisibility(View.GONE);
            } else {
                h.layoutCurso.setVisibility(View.VISIBLE);
                h.curso.setText(
                        usuario.getCurso() == null || usuario.getCurso().isEmpty()
                                ? "-"
                                : usuario.getCurso()
                );
            }

            if (usuario.isVerificado()) {
                h.verificado.setText("Sí");
                h.verificado.setTextColor(green);
                h.ivVerificado.setColorFilter(green);
                h.layoutAccion.setVisibility(View.GONE);
            } else {
                h.verificado.setText("No");
                h.verificado.setTextColor(defaultText);
                h.ivVerificado.setColorFilter(defaultText);
                h.layoutAccion.setVisibility(View.VISIBLE);

                h.btnVerificar.setOnClickListener(v -> listener.onVerificar(pos, usuario));
                h.btnRechazar .setOnClickListener(v -> listener.onRechazar(pos, usuario));
            }
        }
    }

    @Override public int getItemCount() {
        return usuarios != null ? usuarios.size() : 0;
    }

    public void actualizarLista(List<Usuario> nuevaLista) {
        this.usuarios = nuevaLista;
        notifyDataSetChanged();
    }

    public void removerEn(int position) {
        usuarios.remove(position);
        notifyItemRemoved(position);
    }

    public void marcarVerificadoEn(int position) {
        usuarios.get(position).setVerificado(true);
        notifyItemChanged(position);
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
