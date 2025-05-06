package com.example.cafeteria_android.common;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DetalleIngrediente implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("ingrediente_id")
    private int ingrediente_id;

    private Ingrediente ingredientes;

    // Nuevo campo para el precio extra que viene del pedido
    @SerializedName("precio_extra")
    private double precio_extra;

    public int getIngredienteId() {
        return ingrediente_id;
    }

    public void setIngredienteId(int ingrediente_id) {
        this.ingrediente_id = ingrediente_id;
    }

    public Ingrediente getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(Ingrediente ingredientes) {
        this.ingredientes = ingredientes;
    }

    /** Mantiene compatibilidad, pero ya no lo usamos en el adapter de pedidos */
    @Deprecated
    public double getPrecio() {
        return ingredientes != null ? ingredientes.getPrecio() : 0.0;
    }

    // Nuevo getter/setter
    public double getPrecioExtra() {
        return precio_extra;
    }

    public void setPrecioExtra(double precio_extra) {
        this.precio_extra = precio_extra;
    }
}
