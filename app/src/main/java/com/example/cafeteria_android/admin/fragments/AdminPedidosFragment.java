package com.example.cafeteria_android.admin.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.dialogs.AdminFilterBottomSheet;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.AdminPedidoAdapter;
import com.example.cafeteria_android.common.Pedido;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPedidosFragment extends Fragment
        implements AdminFilterBottomSheet.Listener {

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

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
                    @Override public void onAceptar(Pedido pedido) {
                        actualizarEstado(pedido.getId(), "aceptado");
                    }
                    @Override public void onRechazar(Pedido pedido) {
                        actualizarEstado(pedido.getId(), "rechazado");
                    }
                    @Override public void onMarcarListo(Pedido pedido) {
                        actualizarEstado(pedido.getId(), "listo");
                    }
                    @Override public void onMarcarRecogido(Pedido pedido) {
                        actualizarEstado(pedido.getId(), "recogido");
                    }
                    @Override public void onMarcarPagado(Pedido pedido, boolean pagado) {
                        actualizarPagado(pedido.getId(), pagado);
                    }
                }
        );
        rvAdminPedidos.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::cargarPedidos);

        // Abrir el BottomSheet específico de admin
        view.findViewById(R.id.fabFilter)
                .setOnClickListener(v ->
                        new AdminFilterBottomSheet(this)
                                .show(getChildFragmentManager(), "admin_filter_sheet")
                );

        swipeRefresh.setRefreshing(true);
        cargarPedidos();
    }

    @Override public void onResume() {
        super.onResume();
        handler.postDelayed(refresher, REFRESH_INTERVAL_MS);
    }
    @Override public void onPause() {
        super.onPause();
        handler.removeCallbacks(refresher);
    }

    private void cargarPedidos() {
        apiService.obtenerPedidosAdmin("admin", null)
                .enqueue(new Callback<List<Pedido>>() {
                    @Override public void onResponse(Call<List<Pedido>> c, Response<List<Pedido>> r) {
                        swipeRefresh.setRefreshing(false);
                        if (r.isSuccessful() && r.body() != null) {
                            listaPedidos.clear();
                            listaPedidos.addAll(r.body());
                            // Al cargar, aplica filtro inicial: oculta recogidos/rechazados
                            listaFiltrada.clear();
                            for (Pedido p : listaPedidos) {
                                String est = p.getEstado().toLowerCase(Locale.ROOT);
                                if (!est.equals("recogido") && !est.equals("rechazado")) {
                                    listaFiltrada.add(p);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toasty.error(
                                    getContext(),
                                    "Error al cargar pedidos",
                                    Toast.LENGTH_SHORT, true
                            ).show();
                        }
                        actualizarVista();
                    }
                    @Override public void onFailure(Call<List<Pedido>> c, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toasty.error(
                                getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT, true
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
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    Toasty.success(getContext(),
                            "Pedido " + nuevoEstado,
                            Toast.LENGTH_SHORT, true
                    ).show();
                    cargarPedidos();
                } else {
                    Toasty.error(getContext(),
                            "Error al actualizar",
                            Toast.LENGTH_SHORT, true
                    ).show();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                Toasty.error(getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT, true
                ).show();
            }
        });
    }

    private void actualizarPagado(int pedidoId, boolean pagado) {
        apiService.actualizarPedido(
                pedidoId,
                Collections.singletonMap("pagado", pagado)
        ).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                swipeRefresh.setRefreshing(false);
                if (r.isSuccessful()) {
                    if (pagado) {
                        Toasty.success(getContext(),
                                "Pedido marcado como pagado",
                                Toast.LENGTH_SHORT, true
                        ).show();
                    } else {
                        Toasty.warning(getContext(),
                                "Pago desmarcado",
                                Toast.LENGTH_SHORT, true
                        ).show();
                    }
                    cargarPedidos();
                } else {
                    Toasty.error(getContext(),
                            "Error al actualizar pago",
                            Toast.LENGTH_SHORT, true
                    ).show();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toasty.error(getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT, true
                ).show();
            }
        });
    }

    /** Callback del BottomSheet de admin */
    @Override
    public void onAdminFilter(String texto, String estadoPedido) {
        filtrarPedidosAdmin(texto, estadoPedido);
    }

    /** Lógica de filtrado según texto y estado */
    private void filtrarPedidosAdmin(String texto, String estadoPedido) {
        listaFiltrada.clear();
        String lowerText = texto.toLowerCase(Locale.ROOT);

        for (Pedido p : listaPedidos) {
            String estadoRaw = p.getEstado().toLowerCase(Locale.ROOT);

            // Nunca mostrar recogidos ni rechazados
            if (estadoRaw.equals("recogido") || estadoRaw.equals("rechazado")) {
                continue;
            }

            // 1) Filtrar por texto (ID exacto o parte de nombre)
            boolean matchText;
            if (!texto.isEmpty() && TextUtils.isDigitsOnly(texto)) {
                matchText = String.valueOf(p.getId()).equals(texto);
            } else {
                String nombre = p.getUsuario()
                        .getNombreCompleto()
                        .toLowerCase(Locale.ROOT);
                matchText = nombre.contains(lowerText);
            }

            // 2) Filtrar por estado seleccionado
            boolean matchEstado = estadoPedido.equals("Todos")
                    || estadoRaw.equals(estadoPedido.toLowerCase(Locale.ROOT));

            if (matchText && matchEstado) {
                listaFiltrada.add(p);
            }
        }

        adapter.notifyDataSetChanged();
        actualizarVista();
    }


    private void actualizarVista() {
        boolean hay = !listaFiltrada.isEmpty();
        swipeRefresh.setVisibility(hay ? View.VISIBLE : View.GONE);
        emptyView.  setVisibility(hay ? View.GONE    : View.VISIBLE);
    }
}
