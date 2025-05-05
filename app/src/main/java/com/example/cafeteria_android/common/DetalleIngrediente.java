package com.example.cafeteria_android.common;

import java.io.Serializable;

public class DetalleIngrediente implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ingrediente_id;
    private Ingrediente ingredientes;

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

    // Nuevo método para acceder directamente al precio real
    public double getPrecio() {
        return ingredientes != null ? ingredientes.getPrecio() : 0.0;
    }
}
