package com.example.cafeteria_android.common;
/**
 * Define los posibles estados de filtrado para los usuarios.
 */
public enum EstadoUsuario {
    TODOS,           // Sin filtro de estado
    NO_VERIFICADOS,  // Usuarios no verificados
    DEBEN_ACTUALIZAR,// Usuarios que deben actualizar su curso
    NORMALES         // Usuarios verificados y sin curso pendiente
}
