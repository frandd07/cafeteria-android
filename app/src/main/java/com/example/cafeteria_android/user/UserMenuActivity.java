package com.example.cafeteria_android.user;

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
import com.example.cafeteria_android.auth.LoginActivity;
import com.example.cafeteria_android.user.fragments.CartFragment;
import com.example.cafeteria_android.user.fragments.MenuFragment;
import com.example.cafeteria_android.user.fragments.PerfilFragment;
import com.example.cafeteria_android.user.fragments.UserPedidosFragment;
import com.google.android.material.navigation.NavigationView;

public class UserMenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menu);

        // 1) Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2) Drawer + NavView
        drawerLayout = findViewById(R.id.drawerLayout);
        navView      = findViewById(R.id.navigationView);

        // 2a) Bienvenida en header (reutilizando drawer_header.xml)
        View header = navView.getHeaderView(0);
        TextView tvHeaderTitle = header.findViewById(R.id.navHeaderTitle);
        String nombre = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("userName", "Usuario");
        tvHeaderTitle.setText("Hola 👋🏼, " + nombre);

        // 3) Toggle hamburguesa
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 4) Footer logout
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
            Intent it = new Intent(UserMenuActivity.this, LoginActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
            finish();
        });
        navView.addView(logoutView);

        // 5) Listener de menú
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            selectFragment(item.getItemId());
            return true;
        });

        // 6) Fragmento inicial
        if (savedInstanceState == null) {
            navView.setCheckedItem(R.id.nav_menu);
            selectFragment(R.id.nav_menu);
        }
    }

    private void selectFragment(int id) {
        Fragment f = null;
        String title = getString(R.string.app_name);

        switch (id) {
            case R.id.nav_menu:
                f = new MenuFragment();
                title = "Menú";
                break;
            case R.id.nav_carrito:
                f = new CartFragment();
                title = "Carrito";
                break;
            case R.id.nav_pedidos:
                String uid = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                        .getString("userId", "");
                f = UserPedidosFragment.newInstance(uid);
                title = "Mis pedidos";
                break;
            case R.id.nav_perfil:
                f = new PerfilFragment();
                title = "Perfil";
                break;
        }

        if (f != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedorFragmento, f)
                    .commit();
            getSupportActionBar().setTitle(title);
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
