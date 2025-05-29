package com.example.cafeteria_android.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.Ingrediente;
import com.example.cafeteria_android.common.IngredientePrecioAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarIngredientesBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCTO_ID = "arg_producto_id";

    private int productoId;
    private final Runnable onTerminado;

    private ApiService api;
    private ProgressBar pbCargando;
    private RecyclerView rvIngredientes;
    private Button btnGuardarIngredientes;
    private IngredientePrecioAdapter adapter;

    public EditarIngredientesBottomSheet(int productoId, @NonNull Runnable onTerminado) {
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCTO_ID, productoId);
        setArguments(args);
        this.onTerminado = onTerminado;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        productoId = requireArguments().getInt(ARG_PRODUCTO_ID);
        api = ApiClient.getClient().create(ApiService.class);

        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottomsheet_editar_ingredientes, null);
        dialog.setContentView(view);

        pbCargando             = view.findViewById(R.id.pbCargando);
        rvIngredientes         = view.findViewById(R.id.recyclerIngredientes);
        btnGuardarIngredientes = view.findViewById(R.id.btnGuardarIngredientes);

        rvIngredientes.setLayoutManager(new LinearLayoutManager(getContext()));
        cargarIngredientes();

        btnGuardarIngredientes.setOnClickListener(v -> guardarCambios());
        return dialog;
    }

    private void cargarIngredientes() {
        pbCargando.setVisibility(View.VISIBLE);
        api.getIngredientesProducto(productoId)
                .enqueue(new Callback<List<Ingrediente>>() {
                    @Override
                    public void onResponse(Call<List<Ingrediente>> call,
                                           Response<List<Ingrediente>> resp) {
                        pbCargando.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<Ingrediente> lista = resp.body();

                            // Construir mapa de precios iniciales
                            Map<Integer, Double> preciosIniciales = new HashMap<>();
                            for (Ingrediente ing : lista) {
                                preciosIniciales.put(ing.getId(), ing.getPrecio());
                            }

                            // Crear adapter con listener de precio y eliminación
                            adapter = new IngredientePrecioAdapter(
                                    lista,
                                    preciosIniciales,
                                    (ingredienteId, position) -> {
                                        // Mostrar loader
                                        pbCargando.setVisibility(View.VISIBLE);
                                        // Llamar al DELETE
                                        api.deleteIngrediente(ingredienteId)
                                                .enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> respDel) {
                                                        pbCargando.setVisibility(View.GONE);
                                                        if (respDel.isSuccessful()) {
                                                            adapter.removeAt(position);
                                                            Toast.makeText(getContext(),
                                                                    "Ingrediente eliminado", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getContext(),
                                                                    "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        pbCargando.setVisibility(View.GONE);
                                                        Toast.makeText(getContext(),
                                                                "Error de red eliminando", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                            );

                            rvIngredientes.setAdapter(adapter);
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar ingredientes", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Ingrediente>> call, Throwable t) {
                        pbCargando.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Error de red al cargar ingredientes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarCambios() {
        Map<Integer, Double> precios = adapter.getPreciosActualizados();
        btnGuardarIngredientes.setEnabled(false);
        pbCargando.setVisibility(View.VISIBLE);

        api.actualizarPreciosIngrediente(productoId, precios)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> resp) {
                        pbCargando.setVisibility(View.GONE);
                        btnGuardarIngredientes.setEnabled(true);
                        if (resp.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Precios actualizados", Toast.LENGTH_SHORT).show();
                            onTerminado.run();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        pbCargando.setVisibility(View.GONE);
                        btnGuardarIngredientes.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Error de red al guardar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
