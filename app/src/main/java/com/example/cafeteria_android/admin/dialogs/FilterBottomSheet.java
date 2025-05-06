package com.example.cafeteria_android.admin.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cafeteria_android.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * BottomSheet para filtrar la lista de pedidos por texto (nombre, email o nº pedido) y estado.
 */
public class FilterBottomSheet extends BottomSheetDialogFragment {
    public interface Listener { void onFilter(String texto, String tipo); }

    private final Listener listener;
    private final boolean includeOrderNumber;

    public FilterBottomSheet(Listener listener, boolean includeOrderNumber) {
        this.listener           = listener;
        this.includeOrderNumber = includeOrderNumber;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputLayout     layoutBusqueda = view.findViewById(R.id.layoutBusquedaSheet);
        TextInputEditText   input          = view.findViewById(R.id.inputBusquedaSheet);
        AutoCompleteTextView spinner       = view.findViewById(R.id.spinnerTipoSheet);
        MaterialButton      btnClear       = view.findViewById(R.id.btnClearSheet);
        MaterialButton      btnApply       = view.findViewById(R.id.btnApplySheet);

        // Ajustar hint dinámicamente
        String hint = includeOrderNumber
                ? "Buscar por nombre, email o nº pedido"
                : "Buscar por nombre o email";
        layoutBusqueda.setHint(hint);

        // Spinner de estados
        String[] estados = new String[]{"Todos", "Pendiente", "Aceptado", "Listo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                estados
        );
        spinner.setAdapter(adapter);

        // Limpiar
        btnClear.setOnClickListener(v -> {
            input.setText("");
            spinner.setText("Todos", false);
            listener.onFilter("", "");
            dismiss();
        });

        // Aplicar
        btnApply.setOnClickListener(v -> {
            String texto = input.getText() != null
                    ? input.getText().toString().trim()
                    : "";
            String tipo  = spinner.getText() != null
                    ? spinner.getText().toString()
                    : "";
            listener.onFilter(texto, tipo);
            dismiss();
        });
    }
}
