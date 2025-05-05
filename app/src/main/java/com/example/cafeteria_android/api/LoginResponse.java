package com.example.cafeteria_android.api;

public class LoginResponse {
    public User user;
    public Perfil perfil;

    public static class User {
        public String id;
        public String aud;
        public String role;
        public String email;
        public String email_confirmed_at;
        public String phone;
        public String confirmation_sent_at;
        public String confirmed_at;
        public String last_sign_in_at;
        public String created_at;
        public String updated_at;
        public boolean is_anonymous;
    }

    public static class Perfil {
        public String id;
        public String nombre;
        public String email;
        public String tipo;
        public String curso;
        public boolean verificado;
        public String apellido1;
        public String apellido2;
    }
}
