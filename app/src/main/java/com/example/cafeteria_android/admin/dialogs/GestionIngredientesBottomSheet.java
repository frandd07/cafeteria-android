package com.example.cafeteria_android.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.Ingrediente;
import com.example.cafeteria_android.common.IngredienteAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionIngredientesBottomSheet extends BottomSheetDialogFragment {

    private final Runnable onTerminado;
    private RecyclerView       rvIngredientes;
    private ProgressBar        pbCargando;
    private IngredienteAdapter adapter;
    private ApiService         api;

    public GestionIngredientesBottomSheet(@NonNull Runnable onTerminado) {
        this.onTerminado = onTerminado;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottomsheet_gestion_ingredientes, null);
        dialog.setContentView(view);

        rvIngredientes = view.findViewById(R.id.rvIngredientes);
        pbCargando      = view.findViewById(R.id.pbCargando);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddIngrediente);

        adapter = new IngredienteAdapter();
        rvIngredientes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIngredientes.setAdapter(adapter);

        api = ApiClient.getClient().create(ApiService.class);
        loadIngredientes();

        fabAdd.setOnClickListener(v -> {
            // Al crear uno nuevo: recarga la lista y notifica al fragment padre
            new CrearIngredienteBottomSheet(() -> {
                loadIngredientes();
                onTerminado.run();
            }).show(getChildFragmentManager(), "crear_ingrediente");
        });

        return dialog;
    }

    private void loadIngredientes() {
        pbCargando.setVisibility(View.VISIBLE);
        api.getIngredientes().enqueue(new Callback<List<Ingrediente>>() {
            @Override
            public void onResponse(Call<List<Ingrediente>> call,
                                   Response<List<Ingrediente>> response) {
                pbCargando.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.actualizarLista(response.body());
                } else {
                    Toast.makeText(getContext(),
                            "Error al cargar ingredientes",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ingrediente>> call, Throwable t) {
                pbCargando.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Error de red al cargar ingredientes",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
