package com.example.cafeteria_android.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearIngredienteBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "CrearIngredienteBS";

    private final Runnable onTerminado;
    private EditText etNombre, etPrecio;
    private Button btnCrear;
    private ProgressBar pbLoading;
    private ApiService api;

    public CrearIngredienteBottomSheet(@NonNull Runnable onTerminado) {
        this.onTerminado = onTerminado;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottomsheet_crear_ingrediente, null);
        dialog.setContentView(view);

        etNombre  = view.findViewById(R.id.etIngredienteNombre);
        etPrecio  = view.findViewById(R.id.etIngredientePrecio);
        btnCrear  = view.findViewById(R.id.btnCrearIngrediente);
        pbLoading = view.findViewById(R.id.pbLoading);

        api = ApiClient.getClient().create(ApiService.class);

        btnCrear.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String precioStr = etPrecio.getText().toString().trim();

            if (TextUtils.isEmpty(nombre)) {
                etNombre.setError("Requerido");
                return;
            }
            if (TextUtils.isEmpty(precioStr)) {
                etPrecio.setError("Requerido");
                return;
            }

            double precio;
            try {
                precio = Double.parseDouble(precioStr);
            } catch (NumberFormatException e) {
                etPrecio.setError("Precio inválido");
                return;
            }

            btnCrear.setEnabled(false);
            pbLoading.setVisibility(View.VISIBLE);

            Map<String, Object> body = new HashMap<>();
            body.put("nombre", nombre);
            body.put("precio_extra", precio);

            // Logueamos el body que enviamos
            Log.d(TAG, "Enviando crearIngrediente body: " + body);

            api.crearIngrediente(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    pbLoading.setVisibility(View.GONE);
                    btnCrear.setEnabled(true);

                    if (resp.isSuccessful()) {
                        // Toast de éxito con Toasty
                        Toasty.success(
                                getContext(),
                                "Ingrediente creado",
                                Toast.LENGTH_SHORT,
                                true  // muestra el icono por defecto de Toasty
                        ).show();

                        onTerminado.run();
                        dismiss();
                    }else {
                        // Leemos el cuerpo de error para ver qué dice el servidor
                        String errorMsg = "Error " + resp.code();
                        try {
                            String errBody = resp.errorBody() != null
                                    ? resp.errorBody().string()
                                    : "sin cuerpo de error";
                            errorMsg += ": " + errBody;
                        } catch (IOException e) {
                            Log.e(TAG, "Error leyendo errorBody", e);
                            errorMsg += " (no pude leer body)";
                        }
                        Log.e(TAG, "crearIngrediente fallo: " + errorMsg);
                        Toast.makeText(getContext(),
                                errorMsg,
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    pbLoading.setVisibility(View.GONE);
                    btnCrear.setEnabled(true);

                    // Logueamos la excepción completa
                    Log.e(TAG, "crearIngrediente onFailure", t);
                    Toast.makeText(getContext(),
                            "Error de red: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        return dialog;
    }
}
