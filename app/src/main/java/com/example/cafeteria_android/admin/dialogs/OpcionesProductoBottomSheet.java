package com.example.cafeteria_android.admin.dialogs;

import static java.security.AccessController.getContext;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.common.Producto;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.Consumer;

public class OpcionesProductoBottomSheet extends BottomSheetDialogFragment {
    private final Producto producto;
    private final Consumer<String> onAccionSeleccionada; // "editar", "eliminar", "ingredientes"

    public OpcionesProductoBottomSheet(Producto producto, Consumer<String> onAccionSeleccionada) {
        this.producto = producto;
        this.onAccionSeleccionada = onAccionSeleccionada;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottomsheet_opciones_producto, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnEditar).setOnClickListener(v -> {
            onAccionSeleccionada.accept("editar");
            dismiss();
        });

        view.findViewById(R.id.btnEliminar).setOnClickListener(v -> {
            onAccionSeleccionada.accept("eliminar");
            dismiss();
        });

        view.findViewById(R.id.btnIngredientes).setOnClickListener(v -> {
            onAccionSeleccionada.accept("ingredientes");
            dismiss();
        });

        return dialog;
    }
}
