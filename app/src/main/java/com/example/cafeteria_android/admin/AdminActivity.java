package com.example.cafeteria_android.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.admin.fragments.AdminHistorialPedidosFragment;
import com.example.cafeteria_android.admin.fragments.AdminMenuFragment;
import com.example.cafeteria_android.admin.fragments.AdminPedidosFragment;
import com.example.cafeteria_android.admin.fragments.AdminUsuariosFragment;
import com.example.cafeteria_android.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Inicializar Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);

        // 1) Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2) Drawer + NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navView     = findViewById(R.id.navigationView);

        // 2a) Mostrar nombre en el header
        View header = navView.getHeaderView(0);
        TextView tvTitle = header.findViewById(R.id.navHeaderTitle);
        String nombre = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("userName", "Usuario");
        tvTitle.setText("Hola \uD83D\uDC4B\uD83C\uDFFC, " + nombre);

        // 3) Toggle (hamburguesa)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 4) Inyectar footer de logout
        View logoutView = getLayoutInflater()
                .inflate(R.layout.item_logout_footer, navView, false);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        logoutView.setLayoutParams(lp);
        logoutView.setOnClickListener(v -> {
            // Limpia sesión y vuelve al login
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        navView.addView(logoutView);

        // 5) Listener de clicks en el menú
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.nav_admin_nuevo_curso) {
                showConfirmDialog();
            } else {
                selectFragment(id);
            }
            return true;
        });

        // 6) Fragmento inicial
        if (savedInstanceState == null) {
            selectFragment(R.id.nav_admin_usuarios);
            navView.setCheckedItem(R.id.nav_admin_usuarios);
        }
    }

    private void selectFragment(int itemId) {
        Fragment selectedFragment = null;
        String title = "Panel Admin";

        switch (itemId) {
            case R.id.nav_admin_usuarios:
                selectedFragment = new AdminUsuariosFragment();
                title = "Usuarios";
                break;
            case R.id.nav_admin_menu:
                selectedFragment = new AdminMenuFragment();
                title = "Menú";
                break;
            case R.id.nav_admin_pedidos:
                selectedFragment = new AdminPedidosFragment();
                title = "Pedidos";
                break;
            case R.id.nav_admin_historial:
                selectedFragment = new AdminHistorialPedidosFragment();
                title = "Historial";
                break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("¿Iniciar un nuevo curso escolar?")
                .setMessage("Esto hará que todos los alumnos deban actualizar su curso.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> iniciarNuevoCursoEnServidor())
                .show();
    }

    private void iniciarNuevoCursoEnServidor() {
        // Recupera token
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String token = prefs.getString("access_token", "");

        // Llamada Retrofit
        Call<Void> call = apiService.iniciarNuevoCurso("Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(
                            AdminActivity.this,
                            "Nuevo curso activado 🎓",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            AdminActivity.this,
                            "Error al activar curso: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(
                        AdminActivity.this,
                        "Error de red: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
