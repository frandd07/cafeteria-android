package com.example.cafeteria_android.common;

public class Ingrediente {
    private int id;
    private String nombre;
    private double precio;  // 🔥 AÑADIR

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}
