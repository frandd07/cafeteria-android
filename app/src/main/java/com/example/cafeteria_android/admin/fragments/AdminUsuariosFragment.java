package com.example.cafeteria_android.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.dialogs.FilterBottomSheet;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.Usuario;
import com.example.cafeteria_android.common.UsuarioAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsuariosFragment extends Fragment implements FilterBottomSheet.Listener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabFilter;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private ApiService apiService;
    private UsuarioAdapter adapter;
    private List<Usuario> listaUsuarios = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_usuarios, container, false);
        recyclerView = v.findViewById(R.id.recyclerUsuarios);
        fabFilter    = v.findViewById(R.id.fabFilter);
        progressBar  = v.findViewById(R.id.progressBar);
        emptyView    = v.findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        apiService = ApiClient.getClient().create(ApiService.class);

        adapter = new UsuarioAdapter(listaUsuarios, new UsuarioAdapter.OnUsuarioActionListener() {
            @Override
            public void onVerificar(int pos, @NonNull Usuario u) {
                apiService.verificarUsuario(u.getId())
                        .enqueue(genericCallback(pos, "Usuario verificado"));
            }
            @Override
            public void onRechazar(int pos, @NonNull Usuario u) {
                apiService.rechazarUsuario(u.getId())
                        .enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                if (r.isSuccessful()) {
                                    Toasty.info(getContext(),
                                            "Usuario rechazado", Toasty.LENGTH_SHORT, true).show();
                                    adapter.removerEn(pos);
                                } else {
                                    Toasty.error(getContext(),
                                            "Error al rechazar", Toasty.LENGTH_SHORT, true).show();
                                }
                            }
                            @Override public void onFailure(Call<Void> c, Throwable t) {
                                Toasty.error(getContext(),
                                        "Error de red", Toasty.LENGTH_SHORT, true).show();
                            }
                        });
            }
            private Callback<Void> genericCallback(int pos, String msg) {
                return new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (r.isSuccessful()) {
                            Toasty.success(getContext(),
                                    msg, Toasty.LENGTH_SHORT, true).show();
                            adapter.marcarVerificadoEn(pos);
                        } else {
                            Toasty.error(getContext(),
                                    "Error en la acción", Toasty.LENGTH_SHORT, true).show();
                        }
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Toasty.error(getContext(),
                                "Error de red", Toasty.LENGTH_SHORT, true).show();
                    }
                };
            }
        });
        recyclerView.setAdapter(adapter);

        // Carga inicial y filtro
        loadUsuarios("", "Todos");
        fabFilter.setOnClickListener(x ->
                new FilterBottomSheet(this)
                        .show(getChildFragmentManager(), "filter_sheet")
        );
        return v;
    }

    @Override
    public void onFilter(String texto, String tipo) {
        loadUsuarios(texto, tipo);
    }

    private void loadUsuarios(String texto, String tipoSeleccionado) {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        String tipoLower = tipoSeleccionado.toLowerCase();
        Call<List<Usuario>> call = tipoLower.equals("todos")
                ? apiService.obtenerUsuarios()
                : apiService.obtenerUsuariosFiltrado(tipoLower);

        call.enqueue(new Callback<List<Usuario>>() {
            @Override public void onResponse(Call<List<Usuario>> c, Response<List<Usuario>> r) {
                progressBar.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    listaUsuarios.clear();
                    for (Usuario u : r.body()) {
                        if (u.getNombreCompleto()
                                .toLowerCase()
                                .contains(texto.toLowerCase())) {
                            listaUsuarios.add(u);
                        }
                    }
                    adapter.actualizarLista(listaUsuarios);
                } else {
                    Toasty.error(getContext(),
                            "Error al cargar usuarios", Toasty.LENGTH_SHORT, true).show();
                }
                emptyView.setVisibility(listaUsuarios.isEmpty()
                        ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<List<Usuario>> c, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toasty.error(getContext(),
                        "Error de conexión", Toasty.LENGTH_SHORT, true).show();
                emptyView.setVisibility(listaUsuarios.isEmpty()
                        ? View.VISIBLE : View.GONE);
            }
        });
    }
}
