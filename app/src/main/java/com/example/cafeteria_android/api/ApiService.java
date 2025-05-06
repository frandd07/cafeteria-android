package com.example.cafeteria_android.api;

import com.example.cafeteria_android.common.DetalleIngrediente;
import com.example.cafeteria_android.common.FavoritoId;
import com.example.cafeteria_android.common.Ingrediente;
import com.example.cafeteria_android.common.Pedido;
import com.example.cafeteria_android.common.Producto;
import com.example.cafeteria_android.common.Usuario;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    // Añadir producto a favoritos
    @POST("/productos/favoritos")
    Call<Void> añadirAFavoritos(@Body Map<String, Object> body);

    @HTTP(method = "DELETE", path = "/productos/favoritos", hasBody = true)
    Call<Void> eliminarDeFavoritos(@Body Map<String, Object> body);

    // Obtener IDs de productos favoritos del usuario
    @GET("/productos/favoritos/{usuario_id}")
    Call<List<FavoritoId>> obtenerFavoritos(@Path("usuario_id") String usuarioId);

    // Obtener todos los usuarios (excepto admins)
    @GET("/usuarios")
    Call<List<Usuario>> obtenerUsuarios();

    // Obtener usuarios filtrados por tipo (alumno, profesor…)
    @GET("/usuarios")
    Call<List<Usuario>> obtenerUsuariosFiltrado(
            @Query("tipo") String tipo
    );

    // Verificar usuario
    @PATCH("/usuarios/{id}/verificar")
    Call<Void> verificarUsuario(@Path("id") String userId);

    // Aceptar usuario
    @PATCH("/usuarios/{id}/aceptar")
    Call<Void> aceptarUsuario(@Path("id") String userId);

    // Rechazar (eliminar) usuario
    @DELETE("/usuarios/{id}/rechazar")
    Call<Void> rechazarUsuario(@Path("id") String userId);

    // Obtiene todos los productos (admin, sin filtro de habilitado)
    @GET("/productos/admin")
    Call<List<Producto>> getProductosAdmin();

    // Crea un nuevo producto
    @Headers("Content-Type: application/json")
    @POST("/productos")
    Call<Void> crearProducto(@Body Map<String, Object> body);
    // body esperado: { "nombre": String, "precio": Double, "imagen": String }

    // Toggle de habilitado/deshabilitado
    @Headers("Content-Type: application/json")
    @PATCH("/productos/{id}/estado")
    Call<Void> toggleProducto(
            @Path("id") int productoId,
            @Body Map<String, Object> body
    );
    // body esperado: { "habilitado": Boolean }

    // Elimina un producto
    @DELETE("/productos/{id}")
    Call<Void> eliminarProducto(@Path("id") int productoId);


    @GET("/ingredientes")
    Call<List<Ingrediente>> getIngredientes();

    @GET("/productos/{id}/ingredientes")
    Call<List<DetalleIngrediente>> obtenerIngredientesProducto(@Path("id") int productoId);

    @POST("/productos/{id}/ingredientes")
    Call<Void> asignarIngredientes(
            @Path("id") int productoId,
            @Body List<Map<String,Object>> ingredientes
    );

    @PATCH("/pedidos/{id}")
    Call<Void> actualizarPedido(
            @Path("id") int pedidoId,
            @Body Map<String, Object> campos
    );

    @POST("ingredientes")
    Call<Void> crearIngrediente(@Body Map<String,Object> body);

    @GET("productos/{id}/ingredientes")
    Call<List<Ingrediente>> getIngredientesProducto(@Path("id") int productoId);

    @PUT("productos/{id}/ingredientes/precios")
    Call<Void> actualizarPreciosIngrediente(@Path("id") int productoId,
                                            @Body Map<Integer, Double> precios);

    @Headers("Content-Type: application/json")
    @PATCH("ingredientes/{id}")
    Call<Void> actualizarIngrediente(
            @Path("id") int ingredienteId,
            @Body Map<String, Object> updates
    );
}
