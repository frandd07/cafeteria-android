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

import com.example.cafeteria_android.R;
import com.example.cafeteria_android.common.Pedido;
import com.example.cafeteria_android.common.UserDetallePedidoAdapter;


public class PedidoDetalleFragment extends Fragment {

    private static final String ARG_PEDIDO = "arg_pedido";
    private Pedido pedido;

    public static PedidoDetalleFragment newInstance(Pedido p) {
        PedidoDetalleFragment f = new PedidoDetalleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PEDIDO, p);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        if (getArguments()!=null) {
            pedido = (Pedido) getArguments().getSerializable(ARG_PEDIDO);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c,
                             Bundle b) {
        return i.inflate(R.layout.fragment_pedido_detalle, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        RecyclerView rv = v.findViewById(R.id.rvDetallePedido);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserDetallePedidoAdapter(pedido.getDetallePedido()));
    }
}
