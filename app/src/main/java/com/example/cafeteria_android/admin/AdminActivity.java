package com.example.cafeteria_android.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.function.Consumer;

import es.dmoral.toasty.Toasty;
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

        apiService = ApiClient.getClient().create(ApiService.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView      = findViewById(R.id.navigationView);

        View header = navView.getHeaderView(0);
        TextView tvTitle = header.findViewById(R.id.navHeaderTitle);
        String nombre = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("userName", "Usuario");
        tvTitle.setText("Hola 👋, " + nombre);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View logoutView = getLayoutInflater()
                .inflate(R.layout.item_logout_footer, navView, false);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        logoutView.setLayoutParams(lp);
        logoutView.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        navView.addView(logoutView);

        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            if (item.getItemId() == R.id.nav_admin_nuevo_curso) {
                showConfirmDialog();
            } else {
                selectFragment(item.getItemId());
            }
            return true;
        });

        if (savedInstanceState == null) {
            selectFragment(R.id.nav_admin_usuarios);
            navView.setCheckedItem(R.id.nav_admin_usuarios);
        }
    }

    private void selectFragment(int itemId) {
        Fragment frag = null;
        String title = "Panel Admin";
        switch (itemId) {
            case R.id.nav_admin_usuarios:
                frag = new AdminUsuariosFragment(); title = "Usuarios"; break;
            case R.id.nav_admin_menu:
                frag = new AdminMenuFragment(); title = "Menú"; break;
            case R.id.nav_admin_pedidos:
                frag = new AdminPedidosFragment(); title = "Pedidos"; break;
            case R.id.nav_admin_historial:
                frag = new AdminHistorialPedidosFragment(); title = "Historial"; break;
        }
        if (frag != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }
    }

    private void showConfirmDialog() {
        // Inflate custom card layout
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_new_course, null);

        TextView tvTitle      = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage    = dialogView.findViewById(R.id.tvMessage);
        ProgressBar progress   = dialogView.findViewById(R.id.progressBar);
        MaterialButton btnCancel   = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnContinue = dialogView.findViewById(R.id.btnContinue);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnContinue.setOnClickListener(v -> {
            btnCancel.setEnabled(false);
            btnContinue.setEnabled(false);
            progress.setVisibility(View.VISIBLE);

            iniciarNuevoCursoEnServidor(
                    () -> {
                        dialog.dismiss();
                        Toasty.success(this,
                                "Curso iniciado correctamente",
                                Toasty.LENGTH_SHORT, true).show();
                    },
                    errorMsg -> {
                        progress.setVisibility(View.GONE);
                        btnCancel.setEnabled(true);
                        btnContinue.setEnabled(true);
                        Toasty.error(this,
                                "Error: " + errorMsg,
                                Toasty.LENGTH_LONG, true).show();
                    }
            );
        });

        dialog.show();
    }

    private void iniciarNuevoCursoEnServidor(Runnable onSuccess, Consumer<String> onError) {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String token = prefs.getString("access_token", "");
        Call<Void> call = apiService.iniciarNuevoCurso("Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) onSuccess.run();
                else onError.accept("Código " + r.code());
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                onError.accept(t.getMessage());
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
