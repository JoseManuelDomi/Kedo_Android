package com.kedo.app.ui

//IMPORTS
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedo.app.data.RetrofitClient
import com.kedo.app.domain.Evento
import com.kedo.app.domain.Usuario
import kotlinx.coroutines.launch

class CrearEventoViewModel : ViewModel() {

    // Variables para avisar a la pantalla si ha habido éxito o error.
    private val _eventoCreado = MutableLiveData<Boolean>()
    val eventoCreado: LiveData<Boolean> = _eventoCreado

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun publicarNuevoEvento(titulo: String, descripcion: String, maxAsistentes: Int) {
        viewModelScope.launch {
            try {
                // 1. Creamos un Usuario 'simulado' temporal hasta que hagamos el Login real
                val usuarioSim = Usuario(
                    id = 1,
                    nombre = "José Manuel",
                    email = "jose.manuel@kedo.dev",
                    password = "secret_1234!",
                    rol = "USER"
                )

                /* 2. Construimos el Evento con los datos del formulario
                Ponemos unas coordenadas genéricas de Sevilla por el momento.
                 */
                val nuevoEvento = Evento(
                    id = null,
                    titulo = titulo,
                    descripcion = descripcion,
                    creador = usuarioSim,
                    latitud = 37.3891,
                    longitud = -5.9845,
                    fechaEvento = "2026-09-15T18:00:00", // Fecha y hora de ejemplo.
                    fechaRegistro = null
                )

                // 3. Enviamos el POST a Spring Boot.
                val response = RetrofitClient.apiService.crearEvento(nuevoEvento)
                if (response.isSuccessful) {
                    _eventoCreado.value = true
                } else {
                    _error.value = "Error al guardar: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error técnico: ${e.message}"
            }
        }
    }
}