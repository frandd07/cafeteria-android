package com.example.cafeteria_android.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.dialogs.CrearProductoBottomSheet;
import com.example.cafeteria_android.admin.dialogs.EditarIngredientesBottomSheet;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.AdminProductoAdapter;
import com.example.cafeteria_android.common.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMenuFragment extends Fragment {

    private RecyclerView         rvProductos;
    private FloatingActionButton btnNuevoProducto;
    private ApiService           api;
    private AdminProductoAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_menu, container, false);

        rvProductos      = v.findViewById(R.id.recyclerAdminProductos);
        btnNuevoProducto = v.findViewById(R.id.btnNuevoProducto);
        api              = ApiClient.getClient().create(ApiService.class);

        adapter = new AdminProductoAdapter(new AdminProductoAdapter.OnProductoActionListener() {
            @Override
            public void onToggleHabilitado(@NonNull Producto producto, boolean habilitado) {
                Map<String,Object> body = Collections.singletonMap("habilitado", habilitado);
                api.toggleProducto(producto.getId(), body)
                        .enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                Toast.makeText(getContext(),
                                        habilitado ? "Producto habilitado" : "Producto deshabilitado",
                                        Toast.LENGTH_SHORT).show();
                                cargarProductos();
                            }
                            @Override public void onFailure(Call<Void> c, Throwable t) {
                                Toast.makeText(getContext(),
                                        "Error de red al cambiar estado",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onGestionarIngredientes(@NonNull Producto producto) {
                new EditarIngredientesBottomSheet(
                        producto.getId(),
                        AdminMenuFragment.this::cargarProductos
                ).show(getChildFragmentManager(), "editar_ingredientes");
            }

            @Override
            public void onEliminar(@NonNull Producto producto) {
                api.eliminarProducto(producto.getId())
                        .enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                if (r.isSuccessful()) {
                                    Toast.makeText(getContext(),
                                            "Producto eliminado",
                                            Toast.LENGTH_SHORT).show();
                                    cargarProductos();
                                } else {
                                    Toast.makeText(getContext(),
                                            "Error al eliminar producto",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(Call<Void> c, Throwable t) {
                                Toast.makeText(getContext(),
                                        "Error de red al eliminar",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        rvProductos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvProductos.setAdapter(adapter);

        btnNuevoProducto.setOnClickListener(__ ->
                new CrearProductoBottomSheet(this::cargarProductos)
                        .show(getChildFragmentManager(), "crear_producto")
        );

        cargarProductos();
        return v;
    }

    private void cargarProductos() {
        api.getProductosAdmin()
                .enqueue(new Callback<List<Producto>>() {
                    @Override public void onResponse(Call<List<Producto>> call,
                                                     Response<List<Producto>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            adapter.actualizarLista(resp.body());
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar productos",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<List<Producto>> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Error de red al cargar productos",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
