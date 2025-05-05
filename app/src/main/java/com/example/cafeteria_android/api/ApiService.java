package com.example.cafeteria_android.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers({ "Content-Type: application/json" })
    @POST("/auth/login")
    Call<LoginResponse> loginUser(@Body Map<String, String> body);

    @POST("/auth/register")
    Call<Void> registerUser(@Body RegisterRequest request);
}
