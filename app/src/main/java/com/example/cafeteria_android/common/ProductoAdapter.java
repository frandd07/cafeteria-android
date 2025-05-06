package com.example.cafeteria_android.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private final String usuarioId;
    private final OnProductoClickListener listener;
    private final ApiService apiService = ApiClient.getClient().create(ApiService.class);

    public interface OnProductoClickListener {
        void onAñadirAlCarrito(Producto producto);
    }

    public ProductoAdapter(List<Producto> listaProductos, String usuarioId, OnProductoClickListener listener) {
        this.listaProductos = listaProductos;
        this.usuarioId = usuarioId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        Context context = holder.itemView.getContext();

        // Nombre y precio base del producto
        holder.productoNombre.setText(producto.getNombre());
        holder.productoPrecio.setText(
                String.format(Locale.getDefault(), "%.2f €", producto.getPrecio())
        );

        // Carga de imagen
        Glide.with(context)
                .load(producto.getImagen())
                .placeholder(R.drawable.ic_delete)
                .error(R.drawable.ic_delete)
                .into(holder.productoImagen);

        // Favorito
        holder.btnFavorito.setImageResource(
                producto.isFavorito() ? R.drawable.ic_fill_star : R.drawable.ic_star
        );
        holder.btnFavorito.setOnClickListener(v -> {
            boolean nuevoEstado = !producto.isFavorito();
            producto.setFavorito(nuevoEstado);
            holder.btnFavorito.setImageResource(
                    nuevoEstado ? R.drawable.ic_fill_star : R.drawable.ic_star
            );
            Map<String, Object> body = new HashMap<>();
            body.put("usuario_id", usuarioId);
            body.put("producto_id", producto.getId());

            if (nuevoEstado) {
                apiService.añadirAFavoritos(body).enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (r.isSuccessful()) Toasty.success(context, "Añadido a favoritos", Toasty.LENGTH_SHORT, true).show();
                        else Toasty.error(context, "Error al añadir favorito", Toasty.LENGTH_SHORT, true).show();
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Toasty.error(context, "Error de red", Toasty.LENGTH_SHORT, true).show();
                    }
                });
            } else {
                apiService.eliminarDeFavoritos(body).enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (r.isSuccessful()) Toasty.success(context, "Eliminado de favoritos", Toasty.LENGTH_SHORT, true).show();
                        else Toasty.error(context, "Error al eliminar favorito", Toasty.LENGTH_SHORT, true).show();
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Toasty.error(context, "Error de red", Toasty.LENGTH_SHORT, true).show();
                    }
                });
            }
        });

        // Añadir al carrito
        holder.btnAñadirCarrito.setOnClickListener(v ->
                listener.onAñadirAlCarrito(producto)
        );
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    public void actualizarLista(List<Producto> nuevaLista) {
        this.listaProductos = nuevaLista;
        notifyDataSetChanged();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView productoNombre, productoPrecio;
        ImageView productoImagen;
        MaterialButton btnAñadirCarrito;
        ImageButton btnFavorito;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            productoNombre   = itemView.findViewById(R.id.productoNombre);
            productoPrecio   = itemView.findViewById(R.id.productoPrecio);
            productoImagen   = itemView.findViewById(R.id.productoImagen);
            btnAñadirCarrito = itemView.findViewById(R.id.btnAñadirCarrito);
            btnFavorito      = itemView.findViewById(R.id.btnFavorito);
        }
    }
}
