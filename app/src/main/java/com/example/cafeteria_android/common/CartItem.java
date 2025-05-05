package com.example.cafeteria_android.common;

import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private Producto producto;
    private int cantidad;
    private List<DetalleIngrediente> extras;

    public CartItem(Producto producto) {
        this.producto = producto;
        this.cantidad = 1;
        this.extras = new ArrayList<>();
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public List<DetalleIngrediente> getExtras() { return extras; }
    public void setExtras(List<DetalleIngrediente> extras) { this.extras = extras; }

    public double getSubtotal() {
        double base = producto.getPrecio() * cantidad;
        double sumExtras = 0;
        for (DetalleIngrediente di : extras) {
            sumExtras += di.getPrecio();
        }
        return base + sumExtras;
    }

}
