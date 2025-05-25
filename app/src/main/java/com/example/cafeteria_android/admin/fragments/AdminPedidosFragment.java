package com.example.cafeteria_android.admin.fragments;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.dialogs.FilterBottomSheet;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.EstadoUsuario;
import com.example.cafeteria_android.common.AdminPedidoAdapter;
import com.example.cafeteria_android.common.Pedido;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPedidosFragment extends Fragment
        implements FilterBottomSheet.Listener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvAdminPedidos;
    private View emptyView;
    private AdminPedidoAdapter adapter;
    private List<Pedido> listaPedidos = new ArrayList<>();
    private List<Pedido> listaFiltrada = new ArrayList<>();
    private ApiService apiService;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL_MS = 30_000;

    private final Runnable refresher = new Runnable() {
        @Override
        public void run() {
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(true);
                cargarPedidos();
                handler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_pedidos,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService     = ApiClient.getClient().create(ApiService.class);
        swipeRefresh   = view.findViewById(R.id.swipeRefresh);
        rvAdminPedidos = view.findViewById(R.id.rvAdminPedidos);
        emptyView      = view.findViewById(R.id.emptyView);

        rvAdminPedidos.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        adapter = new AdminPedidoAdapter(
                listaFiltrada,
                new AdminPedidoAdapter.OnActionListener() {
                    @Override
                    public void onAceptar(Pedido pedido) {
                        actualizarEstado(pedido.getId(), "aceptado");
                    }
                    @Override
                    public void onRechazar(Pedido pedido) {
                        actualizarEstado(pedido.getId(), "rechazado");
                    }
                    @Override
                    public void onMarcarListo(Pedido pedido) {
                        actualizarEstado(pedido.getId(), "listo");
                    }
                    @Override
                    public void onMarcarRecogido(Pedido pedido) {
                        apiService.eliminarPedido(pedido.getId())
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> c, Response<Void> r) {
                                        if (r.isSuccessful()) {
                                            // Pedido eliminado correctamente
                                            Toasty.success(
                                                    getContext(),
                                                    "Pedido completado",
                                                    Toast.LENGTH_SHORT,
                                                    true
                                            ).show();
                                            cargarPedidos();
                                        } else {
                                            // Error al eliminar
                                            Toasty.error(
                                                    getContext(),
                                                    "Error al eliminar pedido",
                                                    Toast.LENGTH_SHORT,
                                                    true
                                            ).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> c, Throwable t) {
                                        // Error de red u otro
                                        Toasty.error(
                                                getContext(),
                                                "Error: " + t.getMessage(),
                                                Toast.LENGTH_SHORT,
                                                true
                                        ).show();
                                    }
                                });
                    }

                    @Override
                    public void onMarcarPagado(Pedido pedido,
                                               boolean pagado) {
                        actualizarPagado(pedido.getId(), pagado);
                    }
                }
        );
        rvAdminPedidos.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::cargarPedidos);

        view.findViewById(R.id.fabFilter)
                .setOnClickListener(v ->
                        new FilterBottomSheet(this)
                                .show(getChildFragmentManager(),
                                        "filter_sheet")
                );

        swipeRefresh.setRefreshing(true);
        cargarPedidos();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(refresher, REFRESH_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refresher);
    }

    private void cargarPedidos() {
        apiService.obtenerPedidosAdmin("admin", null)
                .enqueue(new Callback<List<Pedido>>() {
                    @Override
                    public void onResponse(Call<List<Pedido>> c, Response<List<Pedido>> r) {
                        swipeRefresh.setRefreshing(false);
                        if (r.isSuccessful() && r.body() != null) {
                            listaPedidos.clear();
                            listaPedidos.addAll(r.body());

                            // Filtrar aquí: solo añadir a listaFiltrada los que NO sean "recogido"
                            listaFiltrada.clear();
                            for (Pedido p : listaPedidos) {
                                if (!"recogido".equalsIgnoreCase(p.getEstado())) {
                                    listaFiltrada.add(p);
                                }
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Toasty.error(
                                    getContext(),
                                    "Error al cargar pedidos",
                                    Toast.LENGTH_SHORT,
                                    true  // muestra el icono de error
                            ).show();
                        }

                        actualizarVista();
                    }

                    @Override
                    public void onFailure(Call<List<Pedido>> c, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toasty.error(
                                getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT,
                                true
                        ).show();
                        actualizarVista();
                    }

                });
    }


    private void actualizarEstado(int pedidoId, String nuevoEstado) {
        apiService.actualizarPedido(
                pedidoId,
                Collections.singletonMap("estado", nuevoEstado)
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    Toasty.success(
                            getContext(),
                            "Pedido " + nuevoEstado,
                            Toast.LENGTH_SHORT,
                            true  // muestra el icono de éxito
                    ).show();
                    cargarPedidos();
                } else {
                    Toasty.error(
                            getContext(),
                            "Error al actualizar",
                            Toast.LENGTH_SHORT,
                            true  // muestra el icono de error
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> c, Throwable t) {
                Toasty.error(
                        getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT,
                        true  // muestra el icono de error
                ).show();
            }

        });
    }

    private void actualizarPagado(int pedidoId, boolean pagado) {
        apiService.actualizarPedido(
                pedidoId,
                Collections.singletonMap("pagado", pagado)
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c,
                                   Response<Void> r) {
                swipeRefresh.setRefreshing(false);
                if (r.isSuccessful()) {
                    if (pagado) {
                        Toasty.success(getContext(),
                                        "Pedido marcado como pagado",
                                        Toast.LENGTH_SHORT,
                                        true)
                                .show();
                    } else {
                        Toasty.warning(getContext(),
                                        "Pago desmarcado",
                                        Toast.LENGTH_SHORT,
                                        true)
                                .show();
                    }
                    cargarPedidos();
                } else {
                    Toasty.error(getContext(),
                                    "Error al actualizar pago",
                                    Toast.LENGTH_SHORT,
                                    true)
                            .show();
                }

            }
            @Override
            public void onFailure(Call<Void> c, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toasty.error(
                        getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT,
                        true  // muestra el icono de error
                ).show();
            }

        });
    }

    // Sobrecarga para la interfaz antigua de dos parámetros
    public void onFilter(String texto, String estado) {
        listaFiltrada.clear();
        if (!TextUtils.isEmpty(texto) && TextUtils.isDigitsOnly(texto)) {
            int buscado = Integer.parseInt(texto);
            for (Pedido p : listaPedidos) {
                if (p.getId() == buscado) listaFiltrada.add(p);
            }
        } else {
            String lower = texto.toLowerCase();
            for (Pedido p : listaPedidos) {
                boolean coincideTexto =
                        p.getUsuario().getNombreCompleto()
                                .toLowerCase().contains(lower)
                                || p.getUsuario().getEmail()
                                .toLowerCase().contains(lower)
                                || String.valueOf(p.getId()).contains(lower);
                boolean coincideEstado =
                        TextUtils.isEmpty(estado)
                                || estado.equalsIgnoreCase("Todos")
                                || p.getEstado().equalsIgnoreCase(estado);

                if (coincideTexto && coincideEstado) {
                    listaFiltrada.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
        actualizarVista();
    }

    // Nuevo método de la interfaz con 3 parámetros, delega al antiguo
    @Override
    public void onFilter(String texto,
                         String tipo,
                         EstadoUsuario estado) {
        onFilter(texto, tipo);
    }

    private void actualizarVista() {
        boolean hay = !listaFiltrada.isEmpty();
        swipeRefresh.setVisibility(hay ? View.VISIBLE : View.GONE);
        emptyView.  setVisibility(hay ? View.GONE    : View.VISIBLE);
    }
}
