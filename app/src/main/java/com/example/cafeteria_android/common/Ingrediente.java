package com.example.cafeteria_android.common;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Ingrediente implements Serializable {
    private int id;
    private String nombre;

    // Mapeamos el JSON "precio_extra" en este campo
    @SerializedName("precio_extra")
    private double precio;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}
