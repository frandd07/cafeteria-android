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
import com.google.android.material.button.MaterialButton;
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
    private TextView tvTotal, tvFecha, tvFilterBadge;
    private View emptyView, loadingView;
    private MaterialButton btnAdjustFilter;
    private FloatingActionButton fabFilter;
    private HistorialPedidoAdapter adapter;
    private final List<Pedido> listaPedidos = new ArrayList<>();
    private ApiService apiService;

    // Parsers
    private final SimpleDateFormat isoParser = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()
    );
    private final SimpleDateFormat isoBuilder = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()
    );
    private final SimpleDateFormat displayFormat = new SimpleDateFormat(
            "dd/MM/yyyy", Locale.getDefault()
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_historial_pedidos,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService      = ApiClient.getClient().create(ApiService.class);
        rvHistorial     = view.findViewById(R.id.rvHistorial);
        tvTotal         = view.findViewById(R.id.tvTotal);
        tvFecha         = view.findViewById(R.id.tvFecha);
        tvFilterBadge   = view.findViewById(R.id.tvFilterBadge);
        emptyView       = view.findViewById(R.id.emptyView);
        loadingView     = view.findViewById(R.id.loadingView);
        fabFilter       = view.findViewById(R.id.fabFilter);
        btnAdjustFilter = view.findViewById(R.id.btnAdjustFilter);

        // Initial visibility
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        tvFilterBadge.setVisibility(View.GONE);

        // RecyclerView setup
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistorialPedidoAdapter(listaPedidos);
        rvHistorial.setAdapter(adapter);

        // "Todos" range initially
        tvFecha.setText("Rango: Todos los pedidos");

        // "Ajustar Filtro" button
        btnAdjustFilter.setOnClickListener(v -> fabFilter.callOnClick());

        // FAB opens the date picker
        fabFilter.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> picker =
                    MaterialDatePicker.Builder.<Pair<Long, Long>>dateRangePicker()
                            .setTitleText("Selecciona rango de fechas")
                            .build();
            picker.show(getParentFragmentManager(), "RANGE_PICKER");
            picker.addOnPositiveButtonClickListener(range -> {
                Date dFrom = new Date(range.first);
                Date dTo   = new Date(range.second + 86_399_000L);
                String from = isoBuilder.format(dFrom);
                String to   = isoBuilder.format(dTo);
                Log.d(TAG, "Filtrando de " + from + " a " + to);

                // Update header
                tvFecha.setText("Rango: "
                        + displayFormat.format(dFrom)
                        + " – "
                        + displayFormat.format(dTo));
                tvFilterBadge.setVisibility(View.VISIBLE);

                // Load data
                cargarHistorial(from, to);
            });
        });

        // Load all on start
        cargarHistorial(null, null);
    }

    private void cargarHistorial(@Nullable String from, @Nullable String to) {
        // Show loading
        loadingView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        rvHistorial.setVisibility(View.GONE);

        Call<List<Pedido>> call = apiService
                .obtenerPedidosHistorial("admin", "recogido", from, to);

        call.enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pedido>> call,
                                   @NonNull Response<List<Pedido>> response) {
                loadingView.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(),
                            "Error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Filter "recogido" only
                List<Pedido> filtrados = new ArrayList<>();
                for (Pedido p : response.body()) {
                    if (!"recogido".equalsIgnoreCase(p.getEstado())) continue;
                    if (from == null || to == null) {
                        filtrados.add(p);
                    } else {
                        try {
                            Date fecha = isoParser.parse(p.getCreadoEn());
                            Date dFrom = isoBuilder.parse(from);
                            Date dTo   = isoBuilder.parse(to);
                            if (fecha != null && !fecha.before(dFrom) && !fecha.after(dTo)) {
                                filtrados.add(p);
                            }
                        } catch (Exception ignored) { }
                    }
                }

                listaPedidos.clear();
                listaPedidos.addAll(filtrados);
                adapter.notifyDataSetChanged();

                // Update total
                double suma = 0;
                for (Pedido p : listaPedidos) suma += p.getTotal();
                tvTotal.setText(String.format(Locale.getDefault(),
                        "%.2f €", suma));

                // Show list or empty
                if (listaPedidos.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    rvHistorial.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Pedido>> call,
                                  @NonNull Throwable t) {
                loadingView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(),
                        "Red: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
