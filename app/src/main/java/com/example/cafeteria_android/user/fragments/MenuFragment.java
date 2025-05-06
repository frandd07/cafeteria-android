package com.example.cafeteria_android.user.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.FavoritoId;
import com.example.cafeteria_android.common.Producto;
import com.example.cafeteria_android.common.ProductoAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private ApiService apiService;
    private String usuarioId;
    private SwitchMaterial switchFavoritos;

    public MenuFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        recyclerView = view.findViewById(R.id.recyclerProductos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Switch para filtrar favoritos
        switchFavoritos = view.findViewById(R.id.switchFavoritos);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Obtenemos el userId guardado en login
        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        usuarioId = prefs.getString("userId", null);

        if (usuarioId == null) {
            Toast.makeText(getContext(), "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
        } else {
            cargarProductosConFavoritos();
        }

        // Escuchar cambios en el Switch para aplicar el filtro de favoritos
        switchFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> cargarProductosConFavoritos());

        return view;
    }

    private void cargarProductosConFavoritos() {
        apiService.obtenerFavoritos(usuarioId).enqueue(new Callback<List<FavoritoId>>() {
            @Override
            public void onResponse(Call<List<FavoritoId>> call, Response<List<FavoritoId>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Integer> idsFavoritos = new HashSet<>();
                    for (FavoritoId f : response.body()) {
                        idsFavoritos.add(f.getProducto_id());
                    }

                    apiService.obtenerProductos().enqueue(new Callback<List<Producto>>() {
                        @Override
                        public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Producto> productos = response.body();

                                // Filtramos productos si el switch está activado
                                if (switchFavoritos.isChecked()) {
                                    productos.removeIf(producto -> !idsFavoritos.contains(producto.getId()));
                                }

                                // Marcar como favorito si está en la lista de favoritos
                                for (Producto p : productos) {
                                    if (idsFavoritos.contains(p.getId())) {
                                        p.setFavorito(true);
                                    }
                                }

                                adapter = new ProductoAdapter(productos, usuarioId, producto -> {
                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.contenedorFragmento,
                                                    ProductDetailFragment.newInstance(producto))
                                            .addToBackStack(null)
                                            .commit();
                                });

                                recyclerView.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Producto>> call, Throwable t) {
                            Toast.makeText(getContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Error al obtener favoritos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FavoritoId>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
