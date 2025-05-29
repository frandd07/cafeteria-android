package com.example.cafeteria_android.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.api.RegisterRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nombreInput, apellido1Input, apellido2Input, emailInput, passwordInput;
    private TextInputLayout cursoLayout;
    private Spinner rolSpinner, cursoSpinner;
    private MaterialButton registerButton, loginRedirectButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Referencias a vistas
        nombreInput         = findViewById(R.id.nombreInput);
        apellido1Input      = findViewById(R.id.apellido1Input);
        apellido2Input      = findViewById(R.id.apellido2Input);
        emailInput          = findViewById(R.id.emailInput);
        passwordInput       = findViewById(R.id.passwordInput);
        rolSpinner          = findViewById(R.id.rolSpinner);
        cursoSpinner        = findViewById(R.id.cursoSpinner);
        cursoLayout         = findViewById(R.id.cursoLayout);
        registerButton      = findViewById(R.id.registerButton);
        loginRedirectButton = findViewById(R.id.loginRedirectButton);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Spinner de rol (display en forma "Alumno", "Profesor", "Personal")
        String[] roles = {"Alumno", "Profesor", "Personal"};
        ArrayAdapter<String> rolAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rolSpinner.setAdapter(rolAdapter);

        // Spinner de cursos (usa el arreglo definido en res/values/arrays.xml)
        ArrayAdapter<CharSequence> cursoAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.cursos_array,
                android.R.layout.simple_spinner_item
        );
        cursoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cursoSpinner.setAdapter(cursoAdapter);

        // Mostrar/ocultar curso según rol seleccionado
        rolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String displayTipo = parent.getItemAtPosition(position).toString();
                cursoLayout.setVisibility(
                        displayTipo.equals("Alumno") ? View.VISIBLE : View.GONE
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada que hacer
            }
        });

        registerButton.setOnClickListener(v -> {
            String nombre    = nombreInput.getText().toString().trim();
            String apellido1 = apellido1Input.getText().toString().trim();
            String apellido2 = apellido2Input.getText().toString().trim();
            String email     = emailInput.getText().toString().trim();
            String password  = passwordInput.getText().toString().trim();

            // obtenemos el valor mostrado y la versión en minúsculas para enviar
            String displayTipo = rolSpinner.getSelectedItem().toString();        // "Alumno"|"Profesor"|"Personal"
            String tipo        = displayTipo.toLowerCase(Locale.ROOT);           // "alumno"|"profesor"|"personal"

            String curso = cursoLayout.getVisibility() == View.VISIBLE
                    ? cursoSpinner.getSelectedItem().toString()
                    : "";

            // Validaciones básicas
            if (nombre.isEmpty() ||
                    apellido1.isEmpty() ||
                    email.isEmpty() ||
                    password.isEmpty() ||
                    displayTipo.isEmpty())
            {
                Toast.makeText(
                        this,
                        "Completa todos los campos obligatorios",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Si es alumno, curso es obligatorio
            if (displayTipo.equals("Alumno") && curso.isEmpty()) {
                Toast.makeText(
                        this,
                        "El curso es obligatorio para alumnos",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Creación del request con tipo en minúsculas
            RegisterRequest request = new RegisterRequest(
                    nombre, apellido1, apellido2,
                    email, password,
                    tipo,  // enviado como "alumno"|"profesor"|"personal"
                    curso
            );

            apiService.registerUser(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toasty.success(
                                RegisterActivity.this,
                                "Registro exitoso",
                                Toasty.LENGTH_SHORT,
                                true
                        ).show();
                        finish(); // Regresa al LoginActivity
                    } else {
                        Toasty.error(
                                RegisterActivity.this,
                                "Error al registrar",
                                Toasty.LENGTH_SHORT,
                                true
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Error de conexión",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });

        // Redirigir a login
        loginRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
