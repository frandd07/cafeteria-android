package com.example.cafeteria_android.user.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.api.ApiClient;
import com.example.cafeteria_android.api.ApiService;
import com.example.cafeteria_android.common.CartAdapter;
import com.example.cafeteria_android.common.CartItem;
import com.example.cafeteria_android.common.CartRepository;
import com.example.cafeteria_android.common.DetalleIngrediente;
import com.example.cafeteria_android.common.Pedido;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private View       contentCart, emptyCart;
    private RecyclerView rvCart;
    private TextView   tvTotalGeneral;
    private MaterialButton btnConfirmar, btnGoMenu;
    private CartAdapter adapter;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        contentCart    = v.findViewById(R.id.contentCart);
        emptyCart      = v.findViewById(R.id.emptyCart);
        rvCart         = v.findViewById(R.id.rvCart);
        tvTotalGeneral = v.findViewById(R.id.tvTotalGeneral);
        btnConfirmar   = v.findViewById(R.id.btnConfirmar);
        btnGoMenu      = v.findViewById(R.id.btnGoMenu);

        apiService = ApiClient.getClient().create(ApiService.class);

        // RecyclerView & adapter
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(CartRepository.getInstance().getItems());
        rvCart.setAdapter(adapter);

        // Swipe to delete with red background + delete icon
        ColorDrawable background = new ColorDrawable(Color.RED);
        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);

        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                          @NonNull RecyclerView.ViewHolder vh,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                        int pos = vh.getAdapterPosition();
                        CartItem removed = CartRepository.getInstance().getItems().get(pos);
                        CartRepository.getInstance().removeItem(removed);
                        actualizarVista();
                        Toast.makeText(getContext(),
                                removed.getProducto().getNombre() + " eliminado",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c,
                                            @NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            float dX, float dY,
                                            int actionState, boolean isActive) {
                        super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
                        View itemView = vh.itemView;
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop    = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();

                        if (dX > 0) { // swipe right
                            background.setBounds(
                                    itemView.getLeft(),
                                    itemView.getTop(),
                                    itemView.getLeft() + ((int)dX),
                                    itemView.getBottom());
                            icon.setBounds(
                                    itemView.getLeft() + iconMargin,
                                    iconTop,
                                    itemView.getLeft() + iconMargin + icon.getIntrinsicWidth(),
                                    iconBottom);
                        } else if (dX < 0) { // swipe left
                            background.setBounds(
                                    itemView.getRight() + ((int)dX),
                                    itemView.getTop(),
                                    itemView.getRight(),
                                    itemView.getBottom());
                            icon.setBounds(
                                    itemView.getRight() - iconMargin - icon.getIntrinsicWidth(),
                                    iconTop,
                                    itemView.getRight() - iconMargin,
                                    iconBottom);
                        } else {
                            background.setBounds(0,0,0,0);
                            icon.setBounds(0,0,0,0);
                        }

                        background.draw(c);
                        icon.draw(c);
                    }
                };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCart);

        btnConfirmar.setOnClickListener(x -> confirmarCompra());
        btnGoMenu   .setOnClickListener(x -> requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragmento, new MenuFragment())
                .commit());

        actualizarVista();
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarVista();
    }

    private void actualizarVista() {
        List<CartItem> items = CartRepository.getInstance().getItems();
        boolean empty = items.isEmpty();
        adapter.setItems(items);
        actualizarTotal(items);
        contentCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyCart  .setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void actualizarTotal(List<CartItem> items) {
        double suma = 0;
        for (CartItem c : items) suma += c.getSubtotal();
        tvTotalGeneral.setText(String.format("Total: %.2f€", suma));
    }

    private void confirmarCompra() {
        List<CartItem> items = CartRepository.getInstance().getItems();
        if (items.isEmpty()) {
            Toast.makeText(getContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String recreo = prefs.getString("recreo", "primer");

        if (userId == null) {
            Toast.makeText(getContext(),
                    "Error: usuario no identificado",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> productos = new ArrayList<>();
        for (CartItem ci : items) {
            Map<String,Object> p = new HashMap<>();
            p.put("id", ci.getProducto().getId());
            p.put("cantidad", ci.getCantidad());
            p.put("precio", ci.getProducto().getPrecio());

            List<Map<String,Object>> extras = new ArrayList<>();
            for (DetalleIngrediente di : ci.getExtras()) {
                Map<String,Object> ei = new HashMap<>();
                ei.put("id", di.getIngredienteId());
                extras.add(ei);
            }
            p.put("ingredientes", extras);


            productos.add(p);
        }

        Map<String,Object> body = new HashMap<>();
        body.put("usuario_id", userId);
        body.put("recreo", recreo);
        body.put("productos", productos);

        apiService.crearPedido(body)
                .enqueue(new Callback<Pedido>() {
                    @Override
                    public void onResponse(Call<Pedido> call, Response<Pedido> resp) {
                        if (resp.isSuccessful() && resp.body()!=null) {
                            Toast.makeText(getContext(),
                                    "Pedido confirmado (#"+resp.body().getId()+")",
                                    Toast.LENGTH_SHORT).show();
                            CartRepository.getInstance().clear();
                            actualizarVista();
                        } else {
                            Log.e("CartFragment","Error al confirmar: code="+resp.code());
                            Toast.makeText(getContext(),
                                    "Error al confirmar pedido",Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Pedido> call, Throwable t) {
                        Log.e("CartFragment","Fallo red al confirmar",t);
                        Toast.makeText(getContext(),
                                "Error de red: "+t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
