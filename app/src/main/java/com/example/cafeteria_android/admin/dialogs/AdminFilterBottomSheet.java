package com.example.cafeteria_android.admin.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cafeteria_android.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class AdminFilterBottomSheet extends BottomSheetDialogFragment {
    public interface Listener {
        /**
         * @param texto        texto libre para buscar ID o nombre
         * @param estadoPedido estado seleccionado ("Todos", "Pendiente", "Aceptado", "Listo")
         */
        void onAdminFilter(String texto, String estadoPedido);
    }

    private final Listener listener;

    public AdminFilterBottomSheet(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.bottom_sheet_admin_filter,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText    inputBusqueda = view.findViewById(R.id.inputBusquedaAdmin);
        AutoCompleteTextView spinnerEstado = view.findViewById(R.id.spinnerEstadoAdmin);
        MaterialButton       btnClear      = view.findViewById(R.id.btnClearAdmin);
        MaterialButton       btnApply      = view.findViewById(R.id.btnApplyAdmin);

        // Opciones de estado del pedido
        final String[] estados = new String[]{
                "Todos",
                "Pendiente",
                "Aceptado",
                "Listo"
        };
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown,
                R.id.tvDropdownItem,
                estados
        );
        spinnerEstado.setAdapter(estadoAdapter);
        spinnerEstado.setText(estados[0], false); // "Todos" por defecto

        // Para que al tocar el campo se abra el dropdown
        spinnerEstado.setOnClickListener(v -> spinnerEstado.showDropDown());

        // Botón Limpiar: reset y callback inmediato
        btnClear.setOnClickListener(v -> {
            inputBusqueda.setText("");
            spinnerEstado.setText(estados[0], false);
            listener.onAdminFilter("", estados[0]);
            dismiss();
        });

        // Botón Aplicar: lee valores y manda al listener
        btnApply.setOnClickListener(v -> {
            String texto = inputBusqueda.getText() != null
                    ? inputBusqueda.getText().toString().trim()
                    : "";
            String estadoSel = spinnerEstado.getText() != null
                    ? spinnerEstado.getText().toString()
                    : estados[0];

            listener.onAdminFilter(texto, estadoSel);
            dismiss();
        });
    }
}
