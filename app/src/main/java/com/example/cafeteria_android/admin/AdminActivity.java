package com.example.cafeteria_android.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.fragments.AdminHistorialPedidosFragment;
import com.example.cafeteria_android.admin.fragments.AdminMenuFragment;
import com.example.cafeteria_android.admin.fragments.AdminPedidosFragment;
import com.example.cafeteria_android.admin.fragments.AdminUsuariosFragment;
import com.example.cafeteria_android.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;


public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

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

        // 5) Listener de clicks en el menú (cambia fragmento y título)
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            selectFragment(item.getItemId());
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
