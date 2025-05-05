package com.example.cafeteria_android.common;

import java.io.Serializable;
import java.util.List;

public class DetallePedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int producto_id;
    private int cantidad;
    private double precio;
    private Producto productos;
    private List<DetalleIngrediente> detalle_ingrediente;

    public int getId() { return id; }
    public int getProductoId() { return producto_id; }
    public int getCantidad() { return cantidad; }
    public double getPrecio() { return precio; }
    public Producto getProductos() { return productos; }
    public List<DetalleIngrediente> getDetalleIngrediente() { return detalle_ingrediente; }
}
