package com.example.cafeteria_android.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.Producto;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BottomSheet para crear o editar un producto.
 */
public class CrearProductoBottomSheet extends BottomSheetDialogFragment {
    private final Producto productoExistente;
    private final Runnable onTerminado;

    /** Crear nuevo producto */
    public CrearProductoBottomSheet(@NonNull Runnable onTerminado) {
        this.productoExistente = null;
        this.onTerminado       = onTerminado;
    }

    /** Editar producto existente */
    public CrearProductoBottomSheet(@NonNull Producto productoExistente,
                                    @NonNull Runnable onTerminado) {
        this.productoExistente = productoExistente;
        this.onTerminado       = onTerminado;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottomsheet_crear_producto, null);
        dialog.setContentView(view);

        ImageButton btnCerrar             = view.findViewById(R.id.btnCerrar);
        EditText    inputNombreProducto   = view.findViewById(R.id.inputNombreProducto);
        EditText    inputPrecioProducto   = view.findViewById(R.id.inputPrecioProducto);
        EditText    inputImagenProducto   = view.findViewById(R.id.inputImagenProducto);
        Button      btnCrearProducto      = view.findViewById(R.id.btnCrearProducto);

        // Si estamos en edición, precargamos valores y cambiamos texto
        if (productoExistente != null) {
            inputNombreProducto.setText(productoExistente.getNombre());
            inputPrecioProducto.setText(String.valueOf(productoExistente.getPrecio()));
            inputImagenProducto.setText(productoExistente.getImagen());
            btnCrearProducto  .setText("Actualizar producto");
        }

        btnCerrar.setOnClickListener(v -> dismiss());

        btnCrearProducto.setOnClickListener(v -> {
            String nombre = inputNombreProducto.getText().toString().trim();
            String precioS= inputPrecioProducto.getText().toString().trim();
            String imagen = inputImagenProducto.getText().toString().trim();

            if (nombre.isEmpty() || precioS.isEmpty() || imagen.isEmpty()) {
                Toast.makeText(getContext(),
                        "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double precio;
            try {
                precio = Double.parseDouble(precioS);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(),
                        "Precio no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService api = ApiClient.getClient().create(ApiService.class);
            Map<String,Object> body = new HashMap<>();
            body.put("nombre", nombre);
            body.put("precio", precio);
            body.put("imagen", imagen);

            if (productoExistente == null) {
                // CREAR
                api.crearProducto(body).enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> r) {
                        if (r.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Producto creado", Toast.LENGTH_SHORT).show();
                            onTerminado.run();
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al crear producto", Toast.LENGTH_SHORT).show();
                        }
                        dismiss();
                    }
                    @Override public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // ACTUALIZAR (usa el endpoint PATCH que tengas para editar)
                api.toggleProducto(productoExistente.getId(), body)  // por ejemplo
                        .enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                if (r.isSuccessful()) {
                                    Toast.makeText(getContext(),
                                            "Producto actualizado", Toast.LENGTH_SHORT).show();
                                    onTerminado.run();
                                } else {
                                    Toast.makeText(getContext(),
                                            "Error al actualizar producto", Toast.LENGTH_SHORT).show();
                                }
                                dismiss();
                            }
                            @Override public void onFailure(Call<Void> c, Throwable t) {
                                Toast.makeText(getContext(),
                                        "Error de red", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        return dialog;
    }
}
