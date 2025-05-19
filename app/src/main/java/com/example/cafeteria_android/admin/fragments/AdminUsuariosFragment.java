package com.example.cafeteria_android.admin.fragments;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.dialogs.FilterBottomSheet;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.EstadoUsuario;
import com.example.cafeteria_android.common.Usuario;
import com.example.cafeteria_android.common.UsuarioAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsuariosFragment extends Fragment
        implements FilterBottomSheet.Listener {

    private RecyclerView      recyclerView;
    private FloatingActionButton fabFilter;
    private ProgressBar       progressBar;
    private LinearLayout      emptyView;
    private ApiService        apiService;
    private UsuarioAdapter    adapter;
    private List<Usuario>     listaUsuarios = new ArrayList<>();

    // **Nuevo**: guardamos el estado actual del filtro
    private EstadoUsuario filtroEstado = EstadoUsuario.TODOS;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_usuarios,
                container, false);
        recyclerView = v.findViewById(R.id.recyclerUsuarios);
        fabFilter    = v.findViewById(R.id.fabFilter);
        progressBar  = v.findViewById(R.id.progressBar);
        emptyView    = v.findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        apiService = ApiClient.getClient().create(ApiService.class);

        adapter = new UsuarioAdapter(
                listaUsuarios,
                new UsuarioAdapter.OnUsuarioActionListener() {
                    @Override public void onVerificar(int pos,
                                                      @NonNull Usuario u) {
                        apiService.verificarUsuario(u.getId())
                                .enqueue(genericCallback(
                                        pos, "Usuario verificado"
                                ));
                    }
                    @Override public void onRechazar(int pos,
                                                     @NonNull Usuario u) {
                        // tu lógica de rechazo…
                    }
                    private Callback<Void> genericCallback(
                            int pos, String msg
                    ) {
                        return new Callback<Void>() {
                            @Override public void onResponse(
                                    Call<Void> c, Response<Void> r
                            ) {
                                if (r.isSuccessful()) {
                                    Toasty.success(
                                            getContext(), msg,
                                            Toasty.LENGTH_SHORT, true
                                    ).show();
                                    adapter.marcarVerificadoEn(pos);
                                } else {
                                    Toasty.error(
                                            getContext(),
                                            "Error en la acción",
                                            Toasty.LENGTH_SHORT, true
                                    ).show();
                                }
                            }
                            @Override public void onFailure(
                                    Call<Void> c, Throwable t
                            ) {
                                Toasty.error(
                                        getContext(),
                                        "Error de red",
                                        Toasty.LENGTH_SHORT, true
                                ).show();
                            }
                        };
                    }
                }
        );
        recyclerView.setAdapter(adapter);
        attachSwipeToDelete();

        // **Ahora** abrimos el BottomSheet sin el boolean extra
        fabFilter.setOnClickListener(x ->
                new FilterBottomSheet(this)
                        .show(getChildFragmentManager(), "filter_sheet")
        );

        // Carga inicial con filtros por defecto
        loadUsuarios("", "Todos");
        return v;
    }

    /**
     * Recibe texto, tipo y ESTADO.
     */
    @Override
    public void onFilter(String texto,
                         String tipo,
                         EstadoUsuario estado) {
        // guardamos el estado y recargamos
        this.filtroEstado = estado;
        loadUsuarios(texto, tipo);
    }

    private void loadUsuarios(String texto,
                              String tipoSeleccionado) {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        String tipoLower = tipoSeleccionado.toLowerCase();
        Call<List<Usuario>> call = tipoLower.equals("todos")
                ? apiService.obtenerUsuarios()
                : apiService.obtenerUsuariosFiltrado(tipoLower);

        call.enqueue(new Callback<List<Usuario>>() {
            @Override public void onResponse(
                    Call<List<Usuario>> c,
                    Response<List<Usuario>> r
            ) {
                progressBar.setVisibility(View.GONE);
                listaUsuarios.clear();

                if (r.isSuccessful() && r.body() != null) {
                    for (Usuario u : r.body()) {
                        // 1) Filtrar por texto
                        if (!u.getNombreCompleto()
                                .toLowerCase()
                                .contains(texto.toLowerCase())) {
                            continue;
                        }
                        // 2) Filtrar por tipo
                        if (!tipoLower.equals("todos") &&
                                !u.getTipo()
                                        .equalsIgnoreCase(tipoLower)) {
                            continue;
                        }
                        // 3) Filtrar por estado
                        switch (filtroEstado) {
                            case NO_VERIFICADOS:
                                if (u.isVerificado()) continue;
                                break;
                            case DEBEN_ACTUALIZAR:
                                if (!u.isDebe_actualizar_curso())
                                    continue;
                                break;
                            case NORMALES:
                                if (!u.isVerificado() ||
                                        u.isDebe_actualizar_curso()
                                ) continue;
                                break;
                            case TODOS:
                            default:
                                // no filtramos más
                        }
                        listaUsuarios.add(u);
                    }
                    adapter.actualizarLista(listaUsuarios);
                } else {
                    Toasty.error(
                            getContext(),
                            "Error al cargar usuarios",
                            Toasty.LENGTH_SHORT, true
                    ).show();
                }
                emptyView.setVisibility(
                        listaUsuarios.isEmpty()
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            @Override public void onFailure(
                    Call<List<Usuario>> c,
                    Throwable t
            ) {
                progressBar.setVisibility(View.GONE);
                Toasty.error(
                        getContext(),
                        "Error de conexión",
                        Toasty.LENGTH_SHORT, true
                ).show();
                emptyView.setVisibility(
                        listaUsuarios.isEmpty()
                                ? View.VISIBLE
                                : View.GONE
                );
            }
        });
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT) {
                    @Override public boolean onMove(
                            RecyclerView rv,
                            RecyclerView.ViewHolder vh,
                            RecyclerView.ViewHolder target
                    ) { return false; }

                    @Override public void onSwiped(
                            RecyclerView.ViewHolder vh, int dir
                    ) {
                        int pos = vh.getAdapterPosition();
                        adapter.notifyItemChanged(pos);
                        View dlgView = LayoutInflater
                                .from(requireContext())
                                .inflate(R.layout.dialog_confirm,
                                        null, false);
                        AlertDialog dlg = new AlertDialog.Builder(
                                requireContext()
                        )
                                .setView(dlgView)
                                .create();
                        dlg.show();

                        TextView tvTitle   =
                                dlgView.findViewById(R.id.tvDialogTitle);
                        TextView tvMessage =
                                dlgView.findViewById(R.id.tvDialogMessage);
                        MaterialButton btnCancel =
                                dlgView.findViewById(R.id.btnCancel);
                        MaterialButton btnConfirm =
                                dlgView.findViewById(R.id.btnConfirm);

                        tvTitle.setText("Eliminar usuario");
                        tvMessage.setText(
                                "¿Seguro que deseas eliminarlo?"
                        );
                        btnCancel.setOnClickListener(v -> dlg.dismiss());
                        btnConfirm.setOnClickListener(v -> {
                            Usuario u = listaUsuarios.get(pos);
                            apiService.eliminarUsuario(u.getId())
                                    .enqueue(new Callback<Void>() {
                                        @Override public void onResponse(
                                                Call<Void> c, Response<Void> r
                                        ) {
                                            if (r.isSuccessful()) {
                                                adapter.removerEn(pos);
                                                Toasty.success(
                                                        getContext(),
                                                        "Usuario eliminado",
                                                        Toasty.LENGTH_SHORT,
                                                        true
                                                ).show();
                                            } else {
                                                Toasty.error(
                                                        getContext(),
                                                        "Error al eliminar",
                                                        Toasty.LENGTH_SHORT,
                                                        true
                                                ).show();
                                            }
                                        }
                                        @Override public void onFailure(
                                                Call<Void> c, Throwable t
                                        ) {
                                            Toasty.error(
                                                    getContext(),
                                                    "Error de red",
                                                    Toasty.LENGTH_SHORT, true
                                            ).show();
                                        }
                                    });
                            dlg.dismiss();
                        });
                    }

                    @Override public void onChildDraw(
                            @NonNull Canvas c,
                            @NonNull RecyclerView rv,
                            @NonNull RecyclerView.ViewHolder vh,
                            float dX, float dY,
                            int actionState, boolean isActive
                    ) {
                        if (actionState ==
                                ItemTouchHelper.ACTION_STATE_SWIPE) {
                            View itemView = vh.itemView;
                            Paint paint = new Paint();
                            paint.setColor(
                                    ContextCompat.getColor(
                                            requireContext(),
                                            R.color.main_color
                                    )
                            );
                            c.drawRect(
                                    itemView.getRight() + dX,
                                    itemView.getTop(),
                                    itemView.getRight(),
                                    itemView.getBottom(),
                                    paint
                            );
                            Drawable icon = ContextCompat
                                    .getDrawable(
                                            requireContext(),
                                            R.drawable.ic_delete
                                    );
                            int margin = (
                                    itemView.getHeight()
                                            - icon.getIntrinsicHeight()
                            ) / 2;
                            int top    = itemView.getTop() + margin;
                            int bottom = top + icon.getIntrinsicHeight();
                            int left   = itemView.getRight()
                                    - margin
                                    - icon.getIntrinsicWidth();
                            int right  = itemView.getRight() - margin;
                            icon.setBounds(left, top, right, bottom);
                            icon.draw(c);
                        }
                        super.onChildDraw(c, rv, vh, dX, dY,
                                actionState, isActive);
                    }
                };
        new ItemTouchHelper(swipeCallback)
                .attachToRecyclerView(recyclerView);
    }
}
