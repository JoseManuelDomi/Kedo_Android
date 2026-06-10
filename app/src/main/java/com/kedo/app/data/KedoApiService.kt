package com.kedo.app.data

//IMPORTS
import com.kedo.app.domain.Evento
import com.kedo.app.domain.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface KedoApiService {

    @GET("/api/usuarios")
    suspend fun obtenerUsuarios(): Response<List<Usuario>>

    @GET("/api/eventos")
    suspend fun obtenerEventos(): Response<List<Evento>>

    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(@Path("id") id: Long): Response<Unit>

    @POST("/api/eventos")
    suspend fun crearEvento(@Body evento: Evento): Response<Evento>

    @POST("/api/usuarios")
    suspend fun registrarUsuarioBackend(@Body usuario: Usuario): Response<Usuario>
}