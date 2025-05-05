package com.example.cafeteria_android.common;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;

    @SerializedName("creado_en")
    private String creadoEn;

    private String recreo;
    private String estado;
    private boolean pagado;
    private double total;

    @SerializedName("usuarios")
    private Usuario usuario;

    @SerializedName("detalle_pedido")
    private List<DetallePedido> detallePedido;

    public int getId() { return id; }
    public String getCreadoEn() { return creadoEn; }
    public String getRecreo() { return recreo; }
    public String getEstado() { return estado; }
    public boolean isPagado() { return pagado; }
    public double getTotal() { return total; }
    public Usuario getUsuario() { return usuario; }
    public List<DetallePedido> getDetallePedido() { return detallePedido; }
}
