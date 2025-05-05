package com.example.cafeteria_android.common;

public class Usuario {
    private String id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String email;
    private String tipo;
    private String curso;
    private boolean verificado;

    public String getNombreCompleto() {
        return nombre + " " + apellido1 + " " + apellido2;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return tipo;
    }

    public String getId() {
        return id;
    }

    public String getApellido1() {
        return apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public String getCurso() {
        return curso;
    }

    public boolean isVerificado() {
        return verificado;
    }
}
