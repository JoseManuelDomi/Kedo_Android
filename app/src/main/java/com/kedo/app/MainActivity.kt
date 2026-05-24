package com.kedo.app

//IMPORTS
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kedo.app.ui.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Buscamos el TextView en el XML
        val tvResultados = findViewById<TextView>(R.id.tvResultados)

        // 2. Inicializamos el ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 3. Nos quedamos "escuchando" los eventos que lleguen de internet
        viewModel.eventos.observe(this) { listaEventos ->
            if (listaEventos.isNullOrEmpty()) {
                tvResultados.text = "No hay eventos creados todavía."
            } else {
                // Montamos un texto bonito con los titulos y descripciones.
                var textoFormateado = ""
                for (evento in listaEventos) {
                    textoFormateado += "\uD83D\uDCCD \${evento.titulo}\\n\uD83D\uDCDD \${evento.descripcion}\\n\\n"
                }
                tvResultados.text = textoFormateado
            }
        }

        // 4. Escuchamos por si hay errores de red
        viewModel.error.observe(this) { mensajeError ->
            tvResultados.text = mensajeError
        }

        // 5. ¡Damos la orden de descarga!
        viewModel.cargarEventos()
    }
}