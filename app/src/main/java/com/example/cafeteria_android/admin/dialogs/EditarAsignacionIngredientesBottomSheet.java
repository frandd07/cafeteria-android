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
import com.example.cafeteria_android.common.DetalleIngrediente;
import com.example.cafeteria_android.common.Ingrediente;
import com.example.cafeteria_android.common.IngredienteCheckAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarAsignacionIngredientesBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCTO_ID = "arg_producto_id";

    private int productoId;
    private final Runnable onTerminado;

    private ApiService api;
    private ProgressBar pb;
    private RecyclerView rv;
    private Button btnGuardar;
    private IngredienteCheckAdapter adapter;

    public EditarAsignacionIngredientesBottomSheet(int productoId, @NonNull Runnable onTerminado) {
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
                .inflate(R.layout.bottomsheet_editar_asignacion_ingredientes, null);
        dlg.setContentView(view);

        pb        = view.findViewById(R.id.pbAsignar);
        rv        = view.findViewById(R.id.rvAsignarIngredientes);
        btnGuardar= view.findViewById(R.id.btnGuardarAsignacion);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IngredienteCheckAdapter();
        rv.setAdapter(adapter);

        cargarDatos();

        btnGuardar.setOnClickListener(v -> guardarAsignacion());
        return dlg;
    }

    private void cargarDatos() {
        pb.setVisibility(View.VISIBLE);

        // 1) Todos los ingredientes
        api.getIngredientes().enqueue(new Callback<List<Ingrediente>>() {
            @Override public void onResponse(Call<List<Ingrediente>> c1, Response<List<Ingrediente>> r1) {
                if (!r1.isSuccessful() || r1.body() == null) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al cargar ingredientes", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Ingrediente> all = r1.body();

                // 2) IDs ya asignados
                api.obtenerIngredientesProducto(productoId)
                        .enqueue(new Callback<List<DetalleIngrediente>>() {
                            @Override public void onResponse(Call<List<DetalleIngrediente>> c2, Response<List<DetalleIngrediente>> r2) {
                                pb.setVisibility(View.GONE);
                                if (r2.isSuccessful() && r2.body() != null) {
                                    List<Integer> assignedIds = new ArrayList<>();
                                    for (DetalleIngrediente di : r2.body()) {
                                        assignedIds.add(di.getIngredienteId());
                                    }
                                    adapter.setData(all, assignedIds);
                                } else {
                                    Toast.makeText(getContext(), "Error al cargar asignados", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(Call<List<DetalleIngrediente>> c2, Throwable t) {
                                pb.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            @Override public void onFailure(Call<List<Ingrediente>> c1, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarAsignacion() {
        List<Integer> sel = adapter.getSeleccionados();
        List<Map<String,Object>> body = new ArrayList<>();
        for (int id : sel) {
            Map<String,Object> m = new HashMap<>();
            m.put("ingrediente_id", id);
            body.add(m);
        }

        btnGuardar.setEnabled(false);
        pb.setVisibility(View.VISIBLE);

        api.asignarIngredientes(productoId, body)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        pb.setVisibility(View.GONE);
                        btnGuardar.setEnabled(true);
                        if (r.isSuccessful()) {
                            Toast.makeText(getContext(), "Asignación guardada", Toast.LENGTH_SHORT).show();
                            onTerminado.run();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        pb.setVisibility(View.GONE);
                        btnGuardar.setEnabled(true);
                        Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
