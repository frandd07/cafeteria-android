package com.example.cafeteria_android.common;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String imagen;
    private double precio;
    private boolean habilitado;

    // Lista de ingredientes extra disponibles para este producto
    @SerializedName("ingredientes_extra")  // ajústalo al nombre real que devuelve tu API
    private List<DetalleIngrediente> extrasDisponibles;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagen() {
        return imagen;
    }
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public double getPrecio() {
        return precio;
    }
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isHabilitado() {
        return habilitado;
    }
    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    /**
     * Obtiene la lista de ingredientes extra disponibles.
     * Si la lista es null, devuelve una lista vacía para evitar NPE.
     */
    public List<DetalleIngrediente> getExtrasDisponibles() {
        if (extrasDisponibles == null) {
            return new ArrayList<>();
        }
        return extrasDisponibles;
    }
    public void setExtrasDisponibles(List<DetalleIngrediente> extras) {
        this.extrasDisponibles = extras;
    }
}
