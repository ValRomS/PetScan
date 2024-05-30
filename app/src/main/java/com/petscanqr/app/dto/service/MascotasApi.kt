package com.petscanqr.app.dto.service

import com.petscanqr.app.dto.response.Mascota
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MascotasApi {
    @GET("mascotas")
    fun getMascotas(@Query("userId") userId: String): Call<List<Mascota>>

    @POST("guardar-mascota")
    fun guardarMascota(@Body mascota: Mascota): Call<Void>

    @DELETE("eliminar-mascota/{id}")
    fun eliminarMascota(@Path("id") mascotaId: String): Call<Void>
}