package com.example.cafeteria_android.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.api.RegisterRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nombreInput, apellido1Input, apellido2Input, emailInput, passwordInput, cursoInput;
    private TextInputLayout cursoLayout;
    private Spinner rolSpinner;
    private MaterialButton registerButton, loginRedirectButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Referencias a vistas
        nombreInput = findViewById(R.id.nombreInput);
        apellido1Input = findViewById(R.id.apellido1Input);
        apellido2Input = findViewById(R.id.apellido2Input);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        cursoInput = findViewById(R.id.cursoInput);
        cursoLayout = findViewById(R.id.cursoLayout);
        rolSpinner = findViewById(R.id.rolSpinner);
        registerButton = findViewById(R.id.registerButton);
        loginRedirectButton = findViewById(R.id.loginRedirectButton);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Configurar Spinner para el rol
        String[] roles = {"alumno", "profesor", "personal"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rolSpinner.setAdapter(adapter);

        // Listener para cambiar visibilidad del campo curso según el rol seleccionado
        rolSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("alumno")) {
                    cursoLayout.setVisibility(View.VISIBLE);
                } else {
                    cursoLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Nada que hacer aquí
            }
        });

        registerButton.setOnClickListener(v -> {
            String nombre = nombreInput.getText().toString().trim();
            String apellido1 = apellido1Input.getText().toString().trim();
            String apellido2 = apellido2Input.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String tipo = rolSpinner.getSelectedItem().toString();
            String curso = cursoInput.getText() != null ? cursoInput.getText().toString().trim() : "";

            if (nombre.isEmpty() || apellido1.isEmpty() || email.isEmpty() || password.isEmpty() || tipo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tipo.equals("alumno") && curso.isEmpty()) {
                Toast.makeText(this, "El curso es obligatorio para alumnos", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest request = new RegisterRequest(nombre, apellido1, apellido2, email, password, tipo, curso);

            apiService.registerUser(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        finish(); // Regresa al LoginActivity
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Botón para redirigir al login
        loginRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
