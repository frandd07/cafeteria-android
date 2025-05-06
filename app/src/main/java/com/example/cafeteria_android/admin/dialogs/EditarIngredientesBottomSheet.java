package com.example.cafeteria_android.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.DetalleIngrediente;
import com.example.cafeteria_android.common.Ingrediente;
import com.example.cafeteria_android.common.IngredienteAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarIngredientesBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCTO_ID = "arg_producto_id";
    private final Runnable onTerminado;
    private int productoId;

    private ProgressBar pb;
    private RecyclerView rv;
    private Button btnGuardar;
    private IngredienteAdapter adapter;
    private ApiService api;

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

        BottomSheetDialog dlg = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottomsheet_ingredientes, null);
        dlg.setContentView(view);

        pb         = view.findViewById(R.id.pbCargandoIngredientes);
        rv         = view.findViewById(R.id.rvIngredientes);
        btnGuardar = view.findViewById(R.id.btnGuardarIngredientes);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IngredienteAdapter();
        rv.setAdapter(adapter);

        cargarDatos();

        btnGuardar.setOnClickListener(v -> guardarAsignacion());
        return dlg;
    }

    private void cargarDatos() {
        pb.setVisibility(View.VISIBLE);

        // 1️⃣ Primero obtenemos TODOS los ingredientes
        api.getIngredientes().enqueue(new Callback<List<Ingrediente>>() {
            @Override public void onResponse(Call<List<Ingrediente>> c1, Response<List<Ingrediente>> r1) {
                if (!r1.isSuccessful() || r1.body() == null) {
                    pb.setVisibility(View.GONE);
                    return;
                }
                List<Ingrediente> todos = r1.body();

                // 2️⃣ Ahora pedimos los asignados a este producto
                api.obtenerIngredientesProducto(productoId)
                        .enqueue(new Callback<List<DetalleIngrediente>>() {
                            @Override public void onResponse(Call<List<DetalleIngrediente>> c2,
                                                             Response<List<DetalleIngrediente>> r2) {
                                pb.setVisibility(View.GONE);
                                // Extraemos solo los IDs
                                Set<Integer> asignados = new HashSet<>();
                                if (r2.isSuccessful() && r2.body() != null) {
                                    for (DetalleIngrediente di : r2.body()) {
                                        asignados.add(di.getIngredienteId());
                                    }
                                }
                                // 3️⃣ Inicializamos el adapter con todo y seleccionados
                                adapter.setData(todos, new ArrayList<>(asignados));
                            }
                            @Override public void onFailure(Call<List<DetalleIngrediente>> c2, Throwable t) {
                                pb.setVisibility(View.GONE);
                            }
                        });
            }
            @Override public void onFailure(Call<List<Ingrediente>> c1, Throwable t) {
                pb.setVisibility(View.GONE);
            }
        });
    }

    private void guardarAsignacion() {
        List<Ingrediente> sel = adapter.getSeleccionados();
        List<Map<String,Object>> body = new ArrayList<>();
        for (Ingrediente ing : sel) {
            Map<String,Object> m = new HashMap<>();
            m.put("ingrediente_id", ing.getId());
            body.add(m);
        }
        api.asignarIngredientes(productoId, body)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (r.isSuccessful()) {
                            onTerminado.run();
                            dismiss();
                        }
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {}
                });
    }
}
