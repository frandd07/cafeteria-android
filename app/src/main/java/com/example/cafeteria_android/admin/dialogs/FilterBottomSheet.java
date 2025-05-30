package com.example.cafeteria_android.admin.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.common.EstadoUsuario;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class FilterBottomSheet extends BottomSheetDialogFragment {
    public interface Listener {
        void onFilter(String texto, String tipo, EstadoUsuario estado);
    }

    private final Listener listener;

    public FilterBottomSheet(Listener listener) {
        this.listener = listener;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputLayout         layoutBusqueda = view.findViewById(R.id.layoutBusquedaSheet);
        TextInputEditText       input          = view.findViewById(R.id.inputBusquedaSheet);
        AutoCompleteTextView    spinnerTipo    = view.findViewById(R.id.spinnerTipoSheet);
        AutoCompleteTextView    spinnerEstado  = view.findViewById(R.id.spinnerEstadoSheet);
        MaterialButton          btnClear       = view.findViewById(R.id.btnClearSheet);
        MaterialButton          btnApply       = view.findViewById(R.id.btnApplySheet);

        layoutBusqueda.setHint("Buscar por nombre, email o tipo");

        // Spinner de tipo con layout personalizado para la lista
        String[] tipos = new String[]{"Todos", "Alumno", "Profesor", "Personal"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown,          // tu layout personalizado
                R.id.tvDropdownItem,             // el TextView dentro de ese layout
                tipos
        );
        spinnerTipo.setAdapter(tipoAdapter);
        spinnerTipo.setText("Todos", false);

        // Spinner de estado con layout personalizado
        final String[] labels = new String[]{
                "Todos",
                "No verificados",
                "Deben actualizar",
                "Al día"
        };
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown,
                R.id.tvDropdownItem,
                labels
        );
        spinnerEstado.setAdapter(estadoAdapter);
        spinnerEstado.setText(labels[0], false);

        // Limpiar
        btnClear.setOnClickListener(v -> {
            input.setText("");
            spinnerTipo.setText("Todos", false);
            spinnerEstado.setText(labels[0], false);
            listener.onFilter("", "Todos", EstadoUsuario.TODOS);
            dismiss();
        });

        // Aplicar
        btnApply.setOnClickListener(v -> {
            String texto = input.getText() != null
                    ? input.getText().toString().trim()
                    : "";
            String tipo  = spinnerTipo.getText() != null
                    ? spinnerTipo.getText().toString()
                    : "Todos";
            String selLabel = spinnerEstado.getText() != null
                    ? spinnerEstado.getText().toString()
                    : labels[0];

            // Mapeamos etiqueta → enum
            EstadoUsuario estado = EstadoUsuario.TODOS;
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equalsIgnoreCase(selLabel)) {
                    estado = EstadoUsuario.values()[i];
                    break;
                }
            }

            listener.onFilter(texto, tipo, estado);
            dismiss();
        });
    }
}
