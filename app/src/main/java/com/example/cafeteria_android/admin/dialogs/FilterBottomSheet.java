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

public class FilterBottomSheet extends BottomSheetDialogFragment {
    public interface Listener {
        void onFilter(String texto, String tipo);
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

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        TextInputEditText input     = view.findViewById(R.id.inputBusquedaSheet);
        AutoCompleteTextView spinner= view.findViewById(R.id.spinnerTipoSheet);
        MaterialButton btnClear     = view.findViewById(R.id.btnClearSheet);
        MaterialButton btnApply     = view.findViewById(R.id.btnApplySheet);

        // Rellenar spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Todos", "Alumnos", "Profesor", "Personal"}
        );
        spinner.setAdapter(adapter);

        btnClear.setOnClickListener(v -> {
            input.setText("");
            spinner.setText("Todos", false);
        });

        btnApply.setOnClickListener(v -> {
            String texto = input.getText().toString();
            String tipo  = spinner.getText().toString();
            listener.onFilter(texto, tipo);
            dismiss();
        });
    }
}
