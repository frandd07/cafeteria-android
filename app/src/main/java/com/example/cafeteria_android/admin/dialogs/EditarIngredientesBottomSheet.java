package com.example.cafeteria_android.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.DetalleIngrediente;
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

/**
 * BottomSheet para asignar/desasignar ingredientes a un producto.
 */
public class EditarIngredientesBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_PRODUCTO_ID = "arg_producto_id";
    private final Runnable onTerminado;
    private int productoId;

    private ProgressBar pbCargando;
    private RecyclerView rvIngredientes;
    private Button btnGuardarIngredientes;
    private ImageButton btnCerrarIngredientes;
    private IngredienteCheckAdapter adapter;
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

        // --- referencias al layout ---
        btnCerrarIngredientes   = view.findViewById(R.id.btnCerrarIngredientes);
        pbCargando              = view.findViewById(R.id.pbCargandoIngredientes);
        rvIngredientes          = view.findViewById(R.id.rvIngredientes);
        btnGuardarIngredientes  = view.findViewById(R.id.btnGuardarIngredientes);

        // cerrar:
        btnCerrarIngredientes.setOnClickListener(v -> dismiss());

        // configurar RecyclerView
        rvIngredientes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IngredienteCheckAdapter(new ArrayList<>());
        rvIngredientes.setAdapter(adapter);

        cargarIngredientesDisponibles();

        btnGuardarIngredientes.setOnClickListener(v -> guardarAsignacion());

        return dlg;
    }

    private void cargarIngredientesDisponibles() {
        pbCargando.setVisibility(View.VISIBLE);
        api.obtenerIngredientesProducto(productoId).enqueue(new Callback<List<DetalleIngrediente>>() {
            @Override public void onResponse(Call<List<DetalleIngrediente>> c, Response<List<DetalleIngrediente>> r) {
                pbCargando.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    // convertimos DetalleIngrediente para usar getPrecio() si es necesario
                    adapter.actualizar(r.body());
                }
            }
            @Override public void onFailure(Call<List<DetalleIngrediente>> c, Throwable t) {
                pbCargando.setVisibility(View.GONE);
            }
        });
    }

    private void guardarAsignacion() {
        List<Integer> seleccion = adapter.getSeleccionados(); // IDs de ingredientes
        List<Map<String,Object>> body = new ArrayList<>();
        for (int id : seleccion) {
            Map<String,Object> m = new HashMap<>();
            m.put("ingrediente_id", id);
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
                    @Override public void onFailure(Call<Void> c, Throwable t) { /* no-op */ }
                });
    }
}
