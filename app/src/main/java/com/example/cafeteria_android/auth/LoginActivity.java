package com.example.cafeteria_android.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.AdminActivity;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.api.ErrorResponse;
import com.example.cafeteria_android.api.LoginResponse;
import com.example.cafeteria_android.user.UserMenuActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginButton, registerButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput    = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton   = findViewById(R.id.loginButton);
        registerButton= findViewById(R.id.registerButton);

        apiService = ApiClient.getClient().create(ApiService.class);

        loginButton.setOnClickListener(v -> {
            String email    = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toasty.warning(this,
                        "Por favor rellena todos los campos",
                        Toast.LENGTH_SHORT,
                        true).show();
                return;
            }
            loginUser(email, password);
        });

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.loginUser(body)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call,
                                           Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // LOGIN OK
                            LoginResponse loginResponse = response.body();
                            String userId = loginResponse.perfil.id;
                            String tipo   = loginResponse.perfil.tipo;
                            String nombre = loginResponse.perfil.nombre;
                            String token  = loginResponse.getAccessToken();

                            Log.d("LOGIN", "Login exitoso. userId: " + userId +
                                    ", tipo: " + tipo +
                                    ", nombre: " + nombre +
                                    ", token: " + token);

                            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("userId",       userId)
                                    .putString("rol",          tipo)
                                    .putString("userName",     nombre)
                                    .putString("access_token", token)
                                    .apply();

                            Toasty.success(
                                    LoginActivity.this,
                                    "¡Bienvenido " + nombre + "!",
                                    Toast.LENGTH_SHORT,
                                    true
                            ).show();

                            if ("admin".equals(tipo)) {
                                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, UserMenuActivity.class));
                            }
                            finish();

                        } else {
                            // ERROR: intentamos parsear body de error
                            String mensaje;
                            try {
                                String json = response.errorBody().string();
                                ErrorResponse err = new Gson()
                                        .fromJson(json, ErrorResponse.class);
                                mensaje = err.error != null
                                        ? err.error
                                        : "Error desconocido";
                            } catch (Exception e) {
                                mensaje = "Error desconocido";
                            }

                            String lower = mensaje.toLowerCase();
                            if (lower.contains("not confirmed") || lower.contains("confirm")) {
                                mensaje = "Debes verificar tu cuenta. Revisa tu correo y haz clic en el enlace de confirmación.";
                            }
                            else if (response.code() == 403) {
                                mensaje = "Tu cuenta aún no ha sido verificada por el administrador.";
                            }

                            if (response.code() == 403) {
                                // Cuenta no verificada
                                Toasty.warning(
                                        LoginActivity.this,
                                        mensaje,
                                        Toast.LENGTH_LONG,
                                        true
                                ).show();
                            } else {
                                // Otros errores (credenciales, 400, 500…)
                                Toasty.error(
                                        LoginActivity.this,
                                        mensaje,
                                        Toast.LENGTH_SHORT,
                                        true
                                ).show();
                            }

                            Log.e("LOGIN",
                                    "Error en login: HTTP " + response.code() +
                                            " → " + mensaje);
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toasty.error(
                                LoginActivity.this,
                                "Error de conexión: " + t.getMessage(),
                                Toast.LENGTH_SHORT,
                                true
                        ).show();
                        Log.e("LOGIN", "Fallo al conectar con backend", t);
                    }
                });
    }
}
