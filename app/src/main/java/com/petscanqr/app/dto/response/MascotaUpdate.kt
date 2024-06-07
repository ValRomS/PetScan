package com.petscanqr.app.dto.response

data class MascotaUpdate(
    val nombre: String?,
    val raza: String?,
    val edad: Int?,
    val nota: String?,
    val tipo: String?,
    val sexo: String?,
    val imageUrl: String?
)
