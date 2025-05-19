package com.example.cafeteria_android.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.Pedido;
import com.example.cafeteria_android.common.UserPedidoAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPedidosFragment extends Fragment {

    private static final String ARG_USER_ID = "arg_user_id";

    private RecyclerView       rvUserPedidos;
    private SwipeRefreshLayout swipeRefresh;
    private View               emptyView;
    private MaterialButton     btnEmptyAction;
    private UserPedidoAdapter  adapter;
    private List<Pedido>       lista = new ArrayList<>();
    private String             userId;
    private ApiService         apiService;

    public static UserPedidosFragment newInstance(String userId) {
        UserPedidosFragment f = new UserPedidosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        f.setArguments(args);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                       ViewGroup container,
                                       Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_pedidos, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        swipeRefresh   = v.findViewById(R.id.swipeRefresh);
        rvUserPedidos  = v.findViewById(R.id.rvUserPedidos);
        emptyView      = v.findViewById(R.id.emptyView);
        btnEmptyAction = v.findViewById(R.id.btnEmptyAction);

        rvUserPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserPedidoAdapter(lista, pedido -> {
            // Eliminar pedido (solo si está en estado "rechazado")
            apiService.eliminarPedido(pedido.getId())
                    .enqueue(new Callback<Void>() {
                        @Override public void onResponse(Call<Void> call, Response<Void> r) {
                            if (r.isSuccessful()) {
                                Toasty.success(requireContext(),
                                        "Pedido eliminado",
                                        Toasty.LENGTH_SHORT, true).show();
                                cargarPedidos();
                            } else {
                                Toasty.error(requireContext(),
                                        "Error al eliminar pedido",
                                        Toasty.LENGTH_SHORT, true).show();
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {
                            Toasty.error(requireContext(),
                                    "Error de red al eliminar",
                                    Toasty.LENGTH_SHORT, true).show();
                        }
                    });
        });
        rvUserPedidos.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::cargarPedidos);
        btnEmptyAction.setOnClickListener(b -> {
            swipeRefresh.setRefreshing(true);
            cargarPedidos();
        });

        // primera carga
        swipeRefresh.setRefreshing(true);
        cargarPedidos();
    }

    @Override public void onResume() {
        super.onResume();
        cargarPedidos();
    }

    private void cargarPedidos() {
        apiService.obtenerPedidosAdmin("user", userId)
                .enqueue(new Callback<List<Pedido>>() {
                    @Override public void onResponse(Call<List<Pedido>> call,
                                                     Response<List<Pedido>> resp) {
                        swipeRefresh.setRefreshing(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            lista.clear();
                            // Filtrar fuera los pedidos ya recogidos
                            for (Pedido p : resp.body()) {
                                if (!"recogido".equalsIgnoreCase(p.getEstado())) {
                                    lista.add(p);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toasty.error(requireContext(),
                                    "Error al cargar pedidos",
                                    Toasty.LENGTH_SHORT, true).show();
                        }
                        actualizarVista();
                    }
                    @Override public void onFailure(Call<List<Pedido>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toasty.error(requireContext(),
                                "Error de conexión: " + t.getMessage(),
                                Toasty.LENGTH_SHORT, true).show();
                        actualizarVista();
                    }
                });
    }

    private void actualizarVista() {
        boolean hay = !lista.isEmpty();
        rvUserPedidos.setVisibility(hay ? View.VISIBLE : View.GONE);
        swipeRefresh.setVisibility(hay ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(hay ? View.GONE : View.VISIBLE);
    }
}
