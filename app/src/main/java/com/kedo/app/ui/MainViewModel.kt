package com.kedo.app.ui

//IMPORTS
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedo.app.data.RetrofitClient
import com.kedo.app.domain.Evento
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Variables protegidas para guardar la lista de eventos o los errores
    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> = _eventos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun cargarEventos() {
        // Lanzamos la petición a internet en un hilo secundario
        viewModelScope.launch {
            try {
                // Llamamos a mi servidor Spring Boot
                val response = RetrofitClient.apiService.obtenerEventos()

                if (response.isSuccessful) {
                    // Si el servidor devuelve 200 OK, guardamos los datos
                    _eventos.value = response.body()
                } else {
                    _error.value = "Error del servidor: ${response.code()}"
                }
            } catch (e: Exception) {
                // Si el servidor está apagado o no hay internet, capturamos el error
                _error.value = "Error de conexión: Verifica que tu servidor local esté encendido."
            }
        }
    }
}