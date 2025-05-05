package com.example.cafeteria_android.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.Producto;
import com.example.cafeteria_android.common.ProductoAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private ApiService apiService;

    public MenuFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        recyclerView = view.findViewById(R.id.recyclerProductos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        apiService = ApiClient.getClient().create(ApiService.class);
        cargarProductos();

        return view;
    }

    private void cargarProductos() {
        apiService.obtenerProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Producto> productos = response.body(); // ✅ aquí se define la variable correctamente

                    adapter = new ProductoAdapter(productos, producto -> {
                        // abrimos el detalle en el mismo contenedor
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
    }

}
