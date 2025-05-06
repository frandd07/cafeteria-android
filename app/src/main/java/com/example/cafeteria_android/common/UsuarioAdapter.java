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

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    public interface OnUsuarioActionListener {
        void onVerificar(int position, @NonNull Usuario usuario);
        void onRechazar (int position, @NonNull Usuario usuario);
    }

    private List<Usuario> usuarios;
    private final OnUsuarioActionListener listener;

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
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);
        int green       = ContextCompat.getColor(holder.itemView.getContext(), R.color.successColor);
        int defaultText = ContextCompat.getColor(holder.itemView.getContext(), R.color.black);

        holder.nombre.setText(usuario.getNombreCompleto());
        holder.email .setText(usuario.getEmail());
        holder.tipo  .setText(usuario.getTipo());

        // Curso
        if ("profesor".equalsIgnoreCase(usuario.getTipo())) {
            holder.layoutCurso.setVisibility(View.GONE);
        } else {
            holder.layoutCurso.setVisibility(View.VISIBLE);
            holder.curso.setText(usuario.getCurso() == null || usuario.getCurso().isEmpty()
                    ? "-" : usuario.getCurso());
        }

        // Verificación
        if (usuario.isVerificado()) {
            holder.verificado.setText("Sí");
            holder.verificado.setTextColor(green);
            holder.ivVerificado.setColorFilter(green);
            holder.layoutAccion.setVisibility(View.GONE);
        } else {
            holder.verificado.setText("No");
            holder.verificado.setTextColor(defaultText);
            holder.ivVerificado.setColorFilter(defaultText);
            holder.layoutAccion.setVisibility(View.VISIBLE);

            holder.btnVerificar.setOnClickListener(v ->
                    listener.onVerificar(position, usuario)
            );
            holder.btnRechazar.setOnClickListener(v ->
                    listener.onRechazar(position, usuario)
            );
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
        TextView nombre, email, tipo, curso, verificado;
        ImageView ivVerificado;
        LinearLayout layoutCurso, layoutAccion;
        Button btnVerificar, btnRechazar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre        = itemView.findViewById(R.id.tvNombre);
            email         = itemView.findViewById(R.id.tvEmail);
            tipo          = itemView.findViewById(R.id.tvTipo);
            curso         = itemView.findViewById(R.id.tvCurso);
            verificado    = itemView.findViewById(R.id.tvVerificado);
            ivVerificado  = itemView.findViewById(R.id.ivVerificado);
            layoutCurso   = itemView.findViewById(R.id.layoutCurso);
            layoutAccion  = itemView.findViewById(R.id.layoutAccion);
            btnVerificar  = itemView.findViewById(R.id.btnAceptar);
            btnRechazar   = itemView.findViewById(R.id.btnRechazar);
        }
    }
}
