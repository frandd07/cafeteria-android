package com.example.cafeteria_android.api;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("refresh_token")
    public String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    @SerializedName("perfil")
    public Perfil perfil;

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
