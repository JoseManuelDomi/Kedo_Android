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
                // Mantenemos el aviso en pantalla
                _error.value = "Error técnico: Revisa el Logcat"
                // ¡ESTA ES LA LÍNEA MÁGICA! Imprime el error entero en rojo en la consola
                android.util.Log.e("ERROR_KEDO", "Fallo exacto de Gson:", e)
            }
        }
    }
}