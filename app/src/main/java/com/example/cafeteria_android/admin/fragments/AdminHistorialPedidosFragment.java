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
import androidx.core.util.Pair;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHistorialPedidosFragment extends Fragment {
    private static final String TAG = "HistorialPedidos";

    private RecyclerView rvHistorial;
    private TextView tvTotal, tvFecha, emptyView;
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
    // Para mostrarle al usuario el rango
    private final SimpleDateFormat displayFormat = new SimpleDateFormat(
            "dd/MM/yyyy", Locale.getDefault()
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
        tvFecha     = view.findViewById(R.id.tvFecha);       // bind del nuevo TextView
        emptyView   = view.findViewById(R.id.emptyView);
        fabFilter   = view.findViewById(R.id.fabFilter);

        // Setup RecyclerView
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistorialPedidoAdapter(listaPedidos);
        rvHistorial.setAdapter(adapter);

        // Estado inicial: sin filtro → muestra "Todos"
        tvFecha.setText("Rango: Todos");
        cargarHistorial(null, null);

        // Listener del FAB para seleccionar rango
        fabFilter.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> picker =
                    MaterialDatePicker.Builder.<Pair<Long, Long>>dateRangePicker()
                            .setTitleText("Selecciona rango de fechas")
                            .build();
            picker.show(getParentFragmentManager(), "RANGE_PICKER");
            picker.addOnPositiveButtonClickListener(range -> {
                // Convierte los timestamps a Date
                Date dFrom = new Date(range.first);
                // Añade 23:59:59 al final del segundo día
                Date dTo   = new Date(range.second + 86_399_000L);

                // Formatea para la API
                String from = isoBuilder.format(dFrom);
                String to   = isoBuilder.format(dTo);
                Log.d(TAG, "Filtrando de " + from + " a " + to);

                // Actualiza el TextView con formato dd/MM/yyyy
                String textoFecha = "Rango: "
                        + displayFormat.format(dFrom)
                        + " – "
                        + displayFormat.format(dTo);
                tvFecha.setText(textoFecha);

                // Lanza la recarga del historial
                cargarHistorial(from, to);
            });
        });
    }

    /**
     * Carga el historial de pedidos, filtrando por estado "recogido" y opcionalmente por rango.
     *
     * @param from ISO 'yyyy-MM-dd'T'HH:mm:ss' o null
     * @param to   ISO 'yyyy-MM-dd'T'HH:mm:ss' o null
     */
    private void cargarHistorial(@Nullable String from, @Nullable String to) {
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
                    if (!"recogido".equalsIgnoreCase(p.getEstado())) continue;
                    if (from == null || to == null) {
                        filtrados.add(p);
                        continue;
                    }
                    try {
                        Date fecha = isoParser.parse(p.getCreadoEn());
                        Date dFrom = isoBuilder.parse(from);
                        Date dTo   = isoBuilder.parse(to);
                        if (fecha != null && !fecha.before(dFrom) && !fecha.after(dTo)) {
                            filtrados.add(p);
                        }
                    } catch (Exception e) {
                        // Ignorar errores de parseo
                    }
                }

                listaPedidos.clear();
                listaPedidos.addAll(filtrados);
                adapter.notifyDataSetChanged();

                // Recalcula y muestra el total
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
