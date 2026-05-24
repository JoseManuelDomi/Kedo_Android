package com.kedo.app.domain

data class Usuario(
    val id: Long? = null,
    val nombre: String,
    val email: String,
    val rol: String? = "USER"
)
