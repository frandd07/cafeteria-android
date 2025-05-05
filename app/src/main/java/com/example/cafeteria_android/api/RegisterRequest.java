package com.example.cafeteria_android.api;

public class RegisterRequest {
    public String nombre;
    public String apellido1;
    public String apellido2;
    public String email;
    public String password;
    public String tipo;
    public String curso;

    public RegisterRequest(String nombre, String apellido1, String apellido2, String email,
                           String password, String tipo, String curso) {
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.email = email;
        this.password = password;
        this.tipo = tipo;
        this.curso = curso;
    }
}
