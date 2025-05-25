package com.example.cafeteria_android.admin.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.dialogs.CrearProductoBottomSheet;
import com.example.cafeteria_android.admin.dialogs.EditarAsignacionIngredientesBottomSheet;
import com.example.cafeteria_android.admin.dialogs.GestionIngredientesBottomSheet;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.AdminProductoAdapter;
import com.example.cafeteria_android.common.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMenuFragment extends Fragment {

    private RecyclerView         rvProductos;
    private ApiService           api;
    private AdminProductoAdapter adapter;
    private SpeedDialView        speedDial;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_menu, container, false);

        rvProductos = v.findViewById(R.id.recyclerAdminProductos);
        api         = ApiClient.getClient().create(ApiService.class);
        adapter     = new AdminProductoAdapter(new AdminProductoAdapter.OnProductoActionListener() {
            @Override
            public void onToggleHabilitado(@NonNull Producto producto, boolean habilitado) {
                Map<String, Object> body = Collections.singletonMap("habilitado", habilitado);
                api.toggleProducto(producto.getId(), body)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> c, Response<Void> r) {
                                if (habilitado) {
                                    Toasty.success(
                                            getContext(),
                                            "Producto habilitado",
                                            Toast.LENGTH_SHORT,
                                            true  // muestra el icono de éxito
                                    ).show();
                                } else {
                                    Toasty.warning(
                                            getContext(),
                                            "Producto deshabilitado",
                                            Toast.LENGTH_SHORT,
                                            true  // muestra el icono de advertencia
                                    ).show();
                                }
                                cargarProductos();
                            }

                            @Override
                            public void onFailure(Call<Void> c, Throwable t) {
                                Toasty.error(
                                        getContext(),
                                        "Error de red al cambiar estado",
                                        Toast.LENGTH_SHORT,
                                        true  // muestra el icono de error
                                ).show();
                            }

                        });
            }

            @Override
            public void onGestionarIngredientes(@NonNull Producto producto) {
                // Ahora abre el BottomSheet de asignación de ingredientes
                new EditarAsignacionIngredientesBottomSheet(
                        producto.getId(),
                        AdminMenuFragment.this::cargarProductos
                ).show(getChildFragmentManager(), "asignar_ingredientes");
            }

            @Override
            public void onEliminar(@NonNull Producto producto) {
                api.eliminarProducto(producto.getId())
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> c, Response<Void> r) {
                                if (r.isSuccessful()) {
                                    Toasty.success(
                                            getContext(),
                                            "Producto eliminado",
                                            Toast.LENGTH_SHORT,
                                            true  // muestra el icono de éxito
                                    ).show();
                                    cargarProductos();
                                } else {
                                    Toasty.error(
                                            getContext(),
                                            "Error al eliminar producto",
                                            Toast.LENGTH_SHORT,
                                            true  // muestra el icono de error
                                    ).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> c, Throwable t) {
                                Toasty.error(
                                        getContext(),
                                        "Error de red al eliminar",
                                        Toast.LENGTH_SHORT,
                                        true  // muestra el icono de error
                                ).show();
                            }

                        });
            }
        });

        rvProductos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvProductos.setAdapter(adapter);

        speedDial = v.findViewById(R.id.speedDial);
// FAB principal
        speedDial.setMainFabClosedBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.main_color)
        );
        speedDial.setMainFabOpenedBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.main_color)
        );
        speedDial.getMainFab().setImageTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                )
        );

// Acción “Añadir producto”
        speedDial.addActionItem(new SpeedDialActionItem.Builder(
                        R.id.fab_add_product,
                        R.drawable.ic_add
                )
                        .setLabel("Añadir producto")
                        .setFabBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.main_color)
                        )
                        .setFabImageTintColor(
                                ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                        .create()
        );

// Acción “Ingredientes”
        speedDial.addActionItem(new SpeedDialActionItem.Builder(
                        R.id.fab_manage_ingredients,
                        R.drawable.ic_kitchen
                )
                        .setLabel("Ingredientes")
                        .setFabBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.main_color)
                        )
                        .setFabImageTintColor(
                                ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                        .create()
        );
        speedDial.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.fab_add_product:
                    new CrearProductoBottomSheet(this::cargarProductos)
                            .show(getChildFragmentManager(), "crear_producto");
                    speedDial.close();
                    return true;
                case R.id.fab_manage_ingredients:
                    // mantiene el global si quieres
                    new GestionIngredientesBottomSheet(this::cargarProductos)
                            .show(getChildFragmentManager(), "gestionar_ingredientes");
                    speedDial.close();
                    return true;
                default:
                    return false;
            }
        });

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
                            Toasty.error(
                                    getContext(),
                                    "Error al cargar productos",
                                    Toast.LENGTH_SHORT,
                                    true  // muestra el icono de error
                            ).show();
                        }

                    }
                    @Override
                    public void onFailure(Call<List<Producto>> call, Throwable t) {
                        Toasty.error(
                                getContext(),
                                "Error de red al cargar productos",
                                Toast.LENGTH_SHORT,
                                true  // muestra el icono de error
                        ).show();
                    }

                });
    }
}
