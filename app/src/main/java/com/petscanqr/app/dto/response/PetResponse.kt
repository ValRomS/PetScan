package com.petscanqr.app.dto.response

// Mascota.kt
data class Mascota(
    var id: String = "",
    var ownerId: String = "",
    var tipo: String = "",
    var nombre: String = "",
    var raza: String = "",
    var sexo: String = "",
    var edad: Int = 0,
    var estatus: String = "",
    var imageUrl: String = "",
    var fechaPerdido: FechaPerdido = FechaPerdido(),
    var nota: String = "",
    var direccion: String = ""
)

data class FechaPerdido(
    var _seconds: Long = 0,
    var _nanoseconds: Int = 0
)
