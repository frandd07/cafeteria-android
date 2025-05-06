package com.example.cafeteria_android.admin.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.AdminPedidoAdapter;
import com.example.cafeteria_android.common.Pedido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPedidosFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvAdminPedidos;
    private View emptyView;
    private AdminPedidoAdapter adapter;
    private List<Pedido> listaPedidos = new ArrayList<>();
    private ApiService apiService;

    // Refresco periódico cada 30 s
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL_MS = 30_000;
    private final Runnable refresher = new Runnable() {
        @Override
        public void run() {
            swipeRefresh.setRefreshing(true);
            cargarPedidos();
            handler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService     = ApiClient.getClient().create(ApiService.class);
        swipeRefresh   = view.findViewById(R.id.swipeRefresh);
        rvAdminPedidos = view.findViewById(R.id.rvAdminPedidos);
        emptyView      = view.findViewById(R.id.emptyView);

        // RecyclerView + Adapter
        rvAdminPedidos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminPedidoAdapter(listaPedidos, new AdminPedidoAdapter.OnActionListener() {
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
                                    Toast.makeText(getContext(),
                                            "Pedido recogido y eliminado",
                                            Toast.LENGTH_SHORT).show();
                                    cargarPedidos();
                                } else {
                                    Toast.makeText(getContext(),
                                            "Error al eliminar pedido",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> c, Throwable t) {
                                Toast.makeText(getContext(),
                                        "Error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            @Override
            public void onMarcarPagado(Pedido pedido, boolean pagado) {
                actualizarPagado(pedido.getId(), pagado);
            }
        });
        rvAdminPedidos.setAdapter(adapter);

        // Pull-to-refresh
        swipeRefresh.setOnRefreshListener(this::cargarPedidos);

        // Primera carga
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
                    public void onResponse(Call<List<Pedido>> call,
                                           Response<List<Pedido>> resp) {
                        swipeRefresh.setRefreshing(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            listaPedidos.clear();
                            listaPedidos.addAll(resp.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar pedidos",
                                    Toast.LENGTH_SHORT).show();
                        }
                        actualizarVista();
                    }
                    @Override
                    public void onFailure(Call<List<Pedido>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        actualizarVista();
                    }
                });
    }

    private void actualizarEstado(int pedidoId, String nuevoEstado) {
        apiService.actualizarPedido(pedidoId,
                        Collections.singletonMap("estado", nuevoEstado))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        if (r.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Pedido " + nuevoEstado,
                                    Toast.LENGTH_SHORT).show();
                            cargarPedidos();
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al actualizar",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> c, Throwable t) {
                        Toast.makeText(getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarPagado(int pedidoId, boolean pagado) {
        apiService.actualizarPedido(pedidoId,
                        Collections.singletonMap("pagado", pagado))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> resp) {
                        swipeRefresh.setRefreshing(false);
                        if (resp.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    pagado
                                            ? "Pedido marcado como pagado"
                                            : "Pago desmarcado",
                                    Toast.LENGTH_SHORT).show();
                            cargarPedidos();
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al actualizar pago",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Muestra u oculta lista vs. mensaje vacío */
    private void actualizarVista() {
        boolean hay = !listaPedidos.isEmpty();
        swipeRefresh.setVisibility(hay ? View.VISIBLE : View.GONE);
        emptyView   .setVisibility(hay ? View.GONE    : View.VISIBLE);
    }
}
