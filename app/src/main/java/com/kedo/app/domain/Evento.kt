package com.kedo.app.domain

data class Evento(
    val id: Long?,
    val titulo: String,
    val descripcion: String,
    val creador: Usuario,
    val latitud: Double,
    val longitud: Double,
    val fechaEvento: String,
    val fechaRegistro: String?
)