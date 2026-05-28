package com.kedo.app.ui

//IMPORTS
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

    // 1. Actualizamos los enchufes para recibir los datos exactos del formulario
    fun publicarNuevoEvento(titulo: String, descripcion: String, latitud: Double, longitud: Double, fechaCompleta: String) {
        viewModelScope.launch {
            try {
                // 2. Extraemos el email real del usuario que tiene la sesión iniciada
                val correoEmpresa = FirebaseAuth.getInstance().currentUser?.email ?: ""

                // 3. Construimos el usuario "cebo".
                val creador = Usuario(
                    id = null,
                    nombre = "",
                    email = correoEmpresa,
                    password = "", // La seguridad la lleva Firebase
                    rol = "EMPRESA"
                )

                // 4. Empaquetamos el Evento con los datos reales recogidos del GPS y del calendario
                val nuevoEvento = Evento(
                    id = null,
                    titulo = titulo,
                    descripcion = descripcion,
                    creador = creador,
                    latitud = latitud,
                    longitud = longitud,
                    fechaEvento = fechaCompleta,
                    fechaRegistro = null // Spring Boot pondrá la fecha de creación automáticamente
                )

                // 5. Disparamos el misil hacia el servidor backend
                val response = RetrofitClient.apiService.crearEvento(nuevoEvento)
                if (response.isSuccessful) {
                    _eventoCreado.value = true // Avisamos de que todo ha ido genial
                } else {
                    _error.value = "Error al guardar en el servidor: código ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error técnico de red: revisa que Spring Boot esté encendido"
            }
        }
    }
}