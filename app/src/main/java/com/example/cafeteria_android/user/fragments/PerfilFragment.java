package com.example.cafeteria_android.user.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.Usuario;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private EditText etNombre, etEmail, etCurso;
    private Button btnGuardar;
    private ApiService apiService;
    private String userId, token;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNombre  = view.findViewById(R.id.etNombre);
        etEmail   = view.findViewById(R.id.etEmail);
        etCurso   = view.findViewById(R.id.etCurso);
        btnGuardar= view.findViewById(R.id.btnGuardar);

        apiService = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("APP_PREFS", getContext().MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        token  = prefs.getString("access_token", "");

        cargarPerfil();
        btnGuardar.setOnClickListener(v -> actualizarPerfil());
    }

    private void cargarPerfil() {
        String bearer = "Bearer " + token;
        apiService.getUsuarioPorId(userId, bearer)
                .enqueue(new Callback<Usuario>() {
                    @Override
                    public void onResponse(Call<Usuario> call,
                                           Response<Usuario> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Usuario u = response.body();
                            // Nombre y email siempre sólo-lectura
                            etNombre.setText(u.getNombre() + " " + u.getApellido1());
                            etEmail.setText(u.getEmail());
                            etNombre.setEnabled(false);
                            etEmail.setEnabled(false);

                            // Curso editable sólo si toca
                            etCurso.setText(u.getCurso());
                            boolean puede = u.isDebe_actualizar_curso();
                            etCurso.setEnabled(puede);
                            etCurso.setInputType(puede
                                    ? InputType.TYPE_CLASS_TEXT
                                    : InputType.TYPE_NULL
                            );
                            btnGuardar.setEnabled(puede);

                            if (!puede) {
                                Toasty.info(getContext(),
                                        "No puedes editar tu curso hasta el próximo ciclo",
                                        Toasty.LENGTH_LONG,
                                        true).show();
                            }

                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar perfil: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Usuario> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Error de red al cargar perfil: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void actualizarPerfil() {
        String nuevoCurso = etCurso.getText().toString().trim();
        if (nuevoCurso.isEmpty()) {
            etCurso.setError("Indica tu curso");
            return;
        }

        Map<String,Object> updates = new HashMap<>();
        updates.put("curso", nuevoCurso);
        updates.put("debe_actualizar_curso", false);
        updates.put("verificado", true);

        String bearer = "Bearer " + token;
        apiService.updateUsuario(userId, bearer, updates)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call,
                                           Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Curso actualizado 👍",
                                    Toast.LENGTH_SHORT).show();
                            etCurso.setEnabled(false);
                            btnGuardar.setEnabled(false);
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al guardar: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Error de red al guardar: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
