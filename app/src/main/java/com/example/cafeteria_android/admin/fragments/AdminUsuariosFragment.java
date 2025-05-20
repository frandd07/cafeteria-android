package com.example.cafeteria_android.admin.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.admin.dialogs.FilterBottomSheet;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.DeleteUsersRequest;
import com.example.cafeteria_android.common.DeleteUsersResponse;
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

    // Para filtros
    private EstadoUsuario filtroEstado = EstadoUsuario.TODOS;

    // ===== Selección múltiple =====
    private ActionMode actionMode;
    private List<String> selectedIds = new ArrayList<>();
    // ===============================

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

        // Inicializamos el adapter PASANDOLE el callback de multi-select
        adapter = new UsuarioAdapter(
                listaUsuarios,
                new UsuarioAdapter.OnUsuarioActionListener() {
                    @Override public void onVerificar(int pos, @NonNull Usuario u) {
                        apiService.verificarUsuario(u.getId())
                                .enqueue(genericCallback(pos, "Usuario verificado"));
                    }
                    @Override public void onRechazar(int pos, @NonNull Usuario u) {
                        // lógica de rechazo…
                    }
                    private Callback<Void> genericCallback(int pos, String msg) {
                        return new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                if (r.isSuccessful()) {
                                    Toasty.success(getContext(), msg,
                                            Toasty.LENGTH_SHORT, true).show();
                                    adapter.marcarVerificadoEn(pos);
                                } else {
                                    Toasty.error(getContext(),
                                            "Error en la acción",
                                            Toasty.LENGTH_SHORT, true).show();
                                }
                            }
                            @Override public void onFailure(Call<Void> c, Throwable t) {
                                Toasty.error(getContext(),
                                        "Error de red",
                                        Toasty.LENGTH_SHORT, true).show();
                            }
                        };
                    }
                },
                // ESTE es el nuevo callback que recibe la lista de IDs seleccionados
                ids -> {
                    selectedIds = ids;
                    if (ids.isEmpty() && actionMode != null) {
                        actionMode.finish();
                    } else if (!ids.isEmpty() && actionMode == null) {
                        actionMode = ((AppCompatActivity)getActivity())
                                .startSupportActionMode(actionModeCallback);
                    }
                    if (actionMode != null) {
                        actionMode.setTitle(ids.size() + " seleccionados");
                    }
                }
        );

        recyclerView.setAdapter(adapter);
        attachSwipeToDelete();

        fabFilter.setOnClickListener(x ->
                new FilterBottomSheet(this)
                        .show(getChildFragmentManager(), "filter_sheet")
        );

        loadUsuarios("", "Todos");
        return v;
    }

    @Override
    public void onFilter(String texto, String tipo, EstadoUsuario estado) {
        this.filtroEstado = estado;
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
                listaUsuarios.clear();
                if (r.isSuccessful() && r.body() != null) {
                    for (Usuario u : r.body()) {
                        if (!u.getNombreCompleto().toLowerCase().contains(texto.toLowerCase()))
                            continue;
                        if (!tipoLower.equals("todos") &&
                                !u.getTipo().equalsIgnoreCase(tipoLower))
                            continue;
                        switch (filtroEstado) {
                            case NO_VERIFICADOS:
                                if (u.isVerificado()) continue;
                                break;
                            case DEBEN_ACTUALIZAR:
                                if (!u.isDebe_actualizar_curso()) continue;
                                break;
                            case NORMALES:
                                if (!u.isVerificado() || u.isDebe_actualizar_curso()) continue;
                                break;
                            case TODOS:
                            default:
                        }
                        listaUsuarios.add(u);
                    }
                    adapter.actualizarLista(listaUsuarios);
                } else {
                    Toasty.error(getContext(),
                            "Error al cargar usuarios",
                            Toasty.LENGTH_SHORT, true).show();
                }
                emptyView.setVisibility(listaUsuarios.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<List<Usuario>> c, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toasty.error(getContext(),
                        "Error de conexión",
                        Toasty.LENGTH_SHORT, true).show();
                emptyView.setVisibility(listaUsuarios.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh,
                                                    RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                        int pos = vh.getAdapterPosition();
                        adapter.notifyItemChanged(pos);
                        View dlgView = LayoutInflater.from(requireContext())
                                .inflate(R.layout.dialog_confirm, null, false);
                        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                                .setView(dlgView).create();
                        dlg.show();

                        TextView tvTitle   = dlgView.findViewById(R.id.tvDialogTitle);
                        TextView tvMessage = dlgView.findViewById(R.id.tvDialogMessage);
                        MaterialButton btnCancel  = dlgView.findViewById(R.id.btnCancel);
                        MaterialButton btnConfirm = dlgView.findViewById(R.id.btnConfirm);

                        tvTitle.setText("Eliminar usuario");
                        tvMessage.setText("¿Seguro que deseas eliminarlo?");
                        btnCancel.setOnClickListener(v -> dlg.dismiss());
                        btnConfirm.setOnClickListener(v -> {
                            Usuario u = listaUsuarios.get(pos);
                            apiService.eliminarUsuario(u.getId())
                                    .enqueue(new Callback<Void>() {
                                        @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                            if (r.isSuccessful()) {
                                                adapter.removerEn(pos);
                                                Toasty.success(getContext(),
                                                        "Usuario eliminado",
                                                        Toasty.LENGTH_SHORT, true).show();
                                            } else {
                                                Toasty.error(getContext(),
                                                        "Error al eliminar",
                                                        Toasty.LENGTH_SHORT, true).show();
                                            }
                                        }
                                        @Override public void onFailure(Call<Void> c, Throwable t) {
                                            Toasty.error(getContext(),
                                                    "Error de red",
                                                    Toasty.LENGTH_SHORT, true).show();
                                        }
                                    });
                            dlg.dismiss();
                        });
                    }
                    @Override public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                                      @NonNull RecyclerView.ViewHolder vh,
                                                      float dX, float dY,
                                                      int actionState, boolean isActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            View itemView = vh.itemView;
                            Paint paint = new Paint();
                            paint.setColor(ContextCompat.getColor(requireContext(), R.color.main_color));
                            c.drawRect(itemView.getRight() + dX,
                                    itemView.getTop(),
                                    itemView.getRight(),
                                    itemView.getBottom(),
                                    paint);
                            Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                            int margin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int top    = itemView.getTop() + margin;
                            int left   = itemView.getRight() - margin - icon.getIntrinsicWidth();
                            icon.setBounds(left, top, itemView.getRight() - margin, top + icon.getIntrinsicHeight());
                            icon.draw(c);
                        }
                        super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
                    }
                };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    // ===== ActionMode para borrar varios usuarios =====
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            Drawable icon = deleteItem.getIcon();
            if (icon != null) {
                icon = icon.mutate();
                icon.setTint(ContextCompat.getColor(getContext(), R.color.main_color));
                deleteItem.setIcon(icon);
            }

            return true;
        }
        @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }
        @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                borrarUsuariosMasivo();
                return true;
            }
            return false;
        }
        @Override public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            actionMode = null;
        }
    };

    /** Llama al endpoint /eliminar-masivo */
    private void borrarUsuariosMasivo() {
        DeleteUsersRequest req = new DeleteUsersRequest(selectedIds);

        // 0) Leer el token de las prefs donde lo guardaste en LoginActivity
        SharedPreferences prefs = getContext()
                .getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String rawToken = prefs.getString("access_token", null);
        Log.d("DEBUG_TOKEN", "rawToken=" + rawToken);

        if (rawToken == null) {
            Toasty.error(getContext(),
                    "No hay token almacenado. Vuelve a iniciar sesión.",
                    Toasty.LENGTH_LONG, true).show();
            actionMode.finish();
            return;
        }

        String bearer = "Bearer " + rawToken;
        Log.d("DEBUG_TOKEN", "bearerToken=" + bearer);

        // 1) Construye la llamada con token + body
        Call<DeleteUsersResponse> call =
                apiService.eliminarUsuariosMasivo(bearer, req);

        // 2) Log de la URL
        Log.d("DEBUG_DELETE", "URL: " + call.request().url());

        // 3) Log de los headers
        Log.d("DEBUG_DELETE", "Headers: " + call.request().headers());

        // 4) Log del JSON que se enviará
        String jsonBody = new com.google.gson.Gson().toJson(req);
        Log.d("DEBUG_DELETE", "Body: " + jsonBody);

        // 5) Encolamos la llamada
        call.enqueue(new Callback<DeleteUsersResponse>() {
            @Override public void onResponse(Call<DeleteUsersResponse> c,
                                             Response<DeleteUsersResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().success) {
                    // Eliminar de la lista local y refrescar
                    listaUsuarios.removeIf(u -> selectedIds.contains(u.getId()));
                    adapter.actualizarLista(listaUsuarios);
                    Toasty.success(getContext(),
                            "Usuarios eliminados",
                            Toasty.LENGTH_SHORT, true).show();
                } else {
                    String err = (r.body() != null && r.body().error != null)
                            ? r.body().error
                            : String.valueOf(r.code());
                    Toasty.error(getContext(),
                            "Error: " + err,
                            Toasty.LENGTH_LONG, true).show();
                }
                actionMode.finish();
            }

            @Override public void onFailure(Call<DeleteUsersResponse> c, Throwable t) {
                Toasty.error(getContext(),
                        "Error de red",
                        Toasty.LENGTH_LONG, true).show();
                actionMode.finish();
            }
        });
    }

}
