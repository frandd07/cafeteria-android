// AdminHistorialPedidosFragment.java
package com.example.cafeteria_android.admin.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.HistorialPedidoAdapter;
import com.example.cafeteria_android.common.Pedido;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.core.util.Pair;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHistorialPedidosFragment extends Fragment {
    private static final String TAG = "HistorialPedidos";

    private RecyclerView rvHistorial;
    private TextView tvTotal, emptyView;
    private FloatingActionButton fabFilter;
    private HistorialPedidoAdapter adapter;
    private final List<Pedido> listaPedidos = new ArrayList<>();
    private ApiService apiService;
    // Para parsear las fechas del JSON
    private final SimpleDateFormat isoParser = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()
    );
    // Para construir from/to
    private final SimpleDateFormat isoBuilder = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_admin_historial_pedidos,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService  = ApiClient.getClient().create(ApiService.class);
        rvHistorial = view.findViewById(R.id.rvHistorial);
        tvTotal     = view.findViewById(R.id.tvTotal);
        emptyView   = view.findViewById(R.id.emptyView);
        fabFilter   = view.findViewById(R.id.fabFilter);

        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistorialPedidoAdapter(listaPedidos);
        rvHistorial.setAdapter(adapter);

        // Primera carga: todos los 'recogido', sin filtro de fecha
        cargarHistorial(null, null);

        fabFilter.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> picker =
                    MaterialDatePicker.Builder.<Pair<Long, Long>>dateRangePicker()
                            .setTitleText("Selecciona rango de fechas")
                            .build();
            picker.show(getParentFragmentManager(), "RANGE_PICKER");
            picker.addOnPositiveButtonClickListener(range -> {
                // from al inicio del primer día
                String from = isoBuilder.format(new Date(range.first));
                // to al final del segundo día (+ 23:59:59)
                long endMs = range.second + 86_399_000L;
                String to = isoBuilder.format(new Date(endMs));
                Log.d(TAG, "Filtrando de " + from + " a " + to);
                cargarHistorial(from, to);
            });
        });
    }

    /**
     * @param from ISO 'yyyy-MM-dd'T'HH:mm:ss' o null
     * @param to   ISO 'yyyy-MM-dd'T'HH:mm:ss' o null
     */
    private void cargarHistorial(@Nullable String from, @Nullable String to) {
        // Pedimos siempre estado="recogido" al servidor
        Call<List<Pedido>> call = apiService
                .obtenerPedidosHistorial("admin", "recogido", from, to);

        Log.d(TAG, "🚀 URL: " + call.request().url());
        call.enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pedido>> call,
                                   @NonNull Response<List<Pedido>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(),
                            "Error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Pedido> filtrados = new ArrayList<>();
                for (Pedido p : response.body()) {
                    // 1) Filtrar estrictamente por estado "recogido"
                    if (!"recogido".equalsIgnoreCase(p.getEstado())) {
                        continue;
                    }

                    // 2) Si no hay rango, lo aceptamos
                    if (from == null || to == null) {
                        filtrados.add(p);
                        continue;
                    }

                    // 3) Si hay from/to, filtramos también por fechas
                    try {
                        Date fecha = isoParser.parse(p.getCreadoEn());
                        Date dFrom = isoBuilder.parse(from);
                        Date dTo   = isoBuilder.parse(to);
                        if (!fecha.before(dFrom) && !fecha.after(dTo)) {
                            filtrados.add(p);
                        }
                    } catch (Exception e) {
                        // Si falla el parse, descartamos
                    }
                }

                listaPedidos.clear();
                listaPedidos.addAll(filtrados);
                adapter.notifyDataSetChanged();

                // Recalcula total
                double suma = 0;
                for (Pedido p : listaPedidos) suma += p.getTotal();
                tvTotal.setText(
                        String.format(Locale.getDefault(), "Total: %.2f €", suma)
                );

                emptyView.setVisibility(
                        listaPedidos.isEmpty() ? View.VISIBLE : View.GONE
                );
            }


            @Override
            public void onFailure(@NonNull Call<List<Pedido>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(),
                        "Red: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
