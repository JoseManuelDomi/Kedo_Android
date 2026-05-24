package com.kedo.app.domain

data class Evento(
    val id: Long? = null,
    val titulo: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val creador: String? = null
)
