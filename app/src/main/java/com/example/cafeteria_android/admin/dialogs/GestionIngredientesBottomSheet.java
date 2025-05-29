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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionIngredientesBottomSheet extends BottomSheetDialogFragment {

    private final Runnable onTerminado;
    private ApiService api;
    private ProgressBar pb;
    private RecyclerView rv;
    private Button btnGuardar;
    private FloatingActionButton fabAdd;
    private IngredientePrecioAdapter adapter;

    public GestionIngredientesBottomSheet(@NonNull Runnable onTerminado) {
        this.onTerminado = onTerminado;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        api = ApiClient.getClient().create(ApiService.class);

        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottomsheet_gestion_ingredientes, null);
        dialog.setContentView(view);

        pb        = view.findViewById(R.id.pbCargando);
        rv        = view.findViewById(R.id.rvIngredientes);
        btnGuardar= view.findViewById(R.id.btnGuardarPrecios);
        fabAdd    = view.findViewById(R.id.fabAddIngrediente);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        loadIngredientes();

        fabAdd.setOnClickListener(v ->
                new CrearIngredienteBottomSheet(() -> {
                    loadIngredientes();
                    onTerminado.run();
                }).show(getChildFragmentManager(), "crear_ingrediente")
        );

        btnGuardar.setOnClickListener(v -> saveAllPrices());

        return dialog;
    }

    private void loadIngredientes() {
        pb.setVisibility(View.VISIBLE);
        api.getIngredientes()
                .enqueue(new Callback<List<Ingrediente>>() {
                    @Override
                    public void onResponse(Call<List<Ingrediente>> call, Response<List<Ingrediente>> resp) {
                        pb.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null) {
                            adapter = new IngredientePrecioAdapter(
                                    resp.body(),
                                    (id, price) -> { /* no-op or immediate UI feedback */ }
                            );
                            rv.setAdapter(adapter);
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar ingredientes", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Ingrediente>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Error de red al cargar ingredientes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveAllPrices() {
        Map<Integer, Double> precios = adapter.getPreciosActualizados();
        if (precios.isEmpty()) {
            Toast.makeText(getContext(), "No hay cambios que guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        pb.setVisibility(View.VISIBLE);

        // Send one PATCH per ingredient; track completions
        AtomicInteger counter = new AtomicInteger(precios.size());
        HashMap<Integer, Boolean> results = new HashMap<>();

        for (Map.Entry<Integer, Double> e : precios.entrySet()) {
            int id = e.getKey();
            double price = e.getValue();
            Map<String,Object> body = new HashMap<>();
            body.put("precio_extra", price);

            api.actualizarIngrediente(id, body)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {
                            results.put(id, r.isSuccessful());
                            checkDone(counter.decrementAndGet(), results);
                        }
                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {
                            results.put(id, false);
                            checkDone(counter.decrementAndGet(), results);
                        }
                    });
        }
    }

    private void checkDone(int remaining, Map<Integer, Boolean> results) {
        if (remaining > 0) return;

        pb.setVisibility(View.GONE);
        btnGuardar.setEnabled(true);

        boolean allOk = !results.containsValue(false);
        if (allOk) {
            Toasty.success(getContext(), "Precios guardados", Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(getContext(), "Algunos fallaron", Toast.LENGTH_SHORT, true).show();
        }

        if (allOk) {
            onTerminado.run();
            dismiss();
        }
    }
}
