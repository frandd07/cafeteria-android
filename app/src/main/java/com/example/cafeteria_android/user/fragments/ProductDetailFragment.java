package com.example.cafeteria_android.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.CartItem;
import com.example.cafeteria_android.common.CartRepository;
import com.example.cafeteria_android.common.DetalleIngrediente;
import com.example.cafeteria_android.common.ExtraOptionAdapter;
import com.example.cafeteria_android.common.Producto;
import com.google.android.material.button.MaterialButton;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailFragment extends Fragment {
    private static final String ARG_PRODUCTO = "arg_producto";

    private Producto producto;
    private ApiService apiService;

    private ImageView ivImagen;
    private TextView tvNombre, tvPrecio;
    private RecyclerView rvExtras;
    private ExtraOptionAdapter extraAdapter;
    private MaterialButton btnAddToCart;

    public static ProductDetailFragment newInstance(@NonNull Producto p) {
        ProductDetailFragment f = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCTO, p);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle saved) {
        super.onCreate(saved);
        if (getArguments() != null) {
            producto = (Producto) getArguments().getSerializable(ARG_PRODUCTO);
        }
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup container,
                             Bundle savedInstanceState) {
        return inf.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        ivImagen     = v.findViewById(R.id.ivDetalleImagen);
        tvNombre     = v.findViewById(R.id.tvDetalleNombre);
        tvPrecio     = v.findViewById(R.id.tvDetallePrecio);
        rvExtras     = v.findViewById(R.id.rvDetalleExtras);
        btnAddToCart = v.findViewById(R.id.btnDetalleAdd);

        // Carga imagen y datos
        Glide.with(requireContext())
                .load(producto.getImagen())
                .into(ivImagen);
        tvNombre.setText(producto.getNombre());
        tvPrecio.setText(String.format(Locale.getDefault(), "%.2f €", producto.getPrecio()));

        // RecyclerView extras
        rvExtras.setLayoutManager(new LinearLayoutManager(getContext()));
        extraAdapter = new ExtraOptionAdapter(Collections.emptyList());
        rvExtras.setAdapter(extraAdapter);

        apiService.obtenerExtrasProducto(producto.getId())
                .enqueue(new Callback<List<DetalleIngrediente>>() {
                    @Override
                    public void onResponse(Call<List<DetalleIngrediente>> call,
                                           Response<List<DetalleIngrediente>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            extraAdapter.updateOptions(resp.body());
                        } else {
                            Toast.makeText(getContext(),
                                    "Este producto no tiene extras",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<DetalleIngrediente>> call,
                                          Throwable t) {
                        Toast.makeText(getContext(),
                                "Error al cargar extras: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        btnAddToCart.setOnClickListener(x -> {
            // Cantidad fija a 1
            CartItem ci = new CartItem(producto);
            ci.setCantidad(1);
            ci.setExtras(extraAdapter.getSeleccionados());
            CartRepository.getInstance().addItem(ci);

            Toasty.success(getContext(),
                    producto.getNombre() + " añadido al carrito",
                    Toasty.LENGTH_SHORT,
                    true).show();

            getParentFragmentManager().popBackStack();
        });
    }
}
