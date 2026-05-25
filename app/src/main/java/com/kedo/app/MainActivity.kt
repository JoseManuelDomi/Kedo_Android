package com.kedo.app

//IMPORTS
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kedo.app.ui.EventoAdapter
import com.kedo.app.ui.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EventoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Configuramos el contenedor visual (RecyclerView)
        val rvEventos = findViewById<RecyclerView>(R.id.rvEventos)

        rvEventos.layoutManager = LinearLayoutManager(this)

        // 2. Inicializamos nuestro Adaptador con una lista vacía al arrancar la app
        adapter = EventoAdapter(emptyList())
        rvEventos.adapter = adapter

        // 3. Inicializamos el "Cerebro" (ViewModel)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 4. Nos quedamos escuchando los datos que lleguen desde el servidor
        viewModel.eventos.observe(this) { listaEventos ->
            if (!listaEventos.isNullOrEmpty()) {
                adapter.actualizarEventos(listaEventos)
            } else {
                Toast.makeText(this, "No hay eventos creados todavía", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Escuchamos por si hay errores (ej: Spring Boot está apagado)
        viewModel.error.observe(this) { mensajeError ->
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
        }

        // 6. ¡Damos la orden de descarga a través de Retrofit!
        viewModel.cargarEventos()
    }
}