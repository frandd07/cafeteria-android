package com.example.cafeteria_android.api;

import com.example.cafeteria_android.common.DetalleIngrediente;
import com.example.cafeteria_android.common.Pedido;
import com.example.cafeteria_android.common.Producto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @Headers({ "Content-Type: application/json" })
    @POST("/auth/login")
    Call<LoginResponse> loginUser(@Body Map<String, String> body);

    @POST("/auth/register")
    Call<Void> registerUser(@Body RegisterRequest request);

    @GET("/productos") // solo productos activos (usuario)
    Call<List<Producto>> obtenerProductos();

    @POST("/pedidos")
    Call<Pedido> crearPedido(@Body Map<String,Object> pedido);

    @GET("/productos/{id}/ingredientes")
    Call<List<DetalleIngrediente>> obtenerExtrasProducto(
            @Path("id") int productoId
    );
    @DELETE("/pedidos/{id}")
    Call<Void> eliminarPedido(@Path("id") int id);

    @GET("/pedidos")
    Call<List<Pedido>> obtenerPedidosAdmin(
            @Query("rol") String rol,
            @Query("user_id") String userId  // ahora String
    );


}
