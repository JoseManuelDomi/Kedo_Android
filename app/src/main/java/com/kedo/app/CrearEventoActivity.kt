package com.kedo.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.kedo.app.ui.CrearEventoViewModel

class CrearEventoActivity : AppCompatActivity() {

    private lateinit var viewModel: CrearEventoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_evento)

        viewModel = ViewModelProvider(this)[CrearEventoViewModel::class.java]

        // 1. Buscamos los elementos visuales
        val etTitulo = findViewById<TextInputEditText>(R.id.etTitulo)
        val etDescripcion = findViewById<TextInputEditText>(R.id.etDescripcion)
        val etAsistentes = findViewById<TextInputEditText>(R.id.etAsistentes)
        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarEvento)

        // 2. Configuramos el clic del botón
        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val asistentesStr = etAsistentes.text.toString().trim()

            // Pequeña validación para que no envíen campos vacíos
            if (titulo.isEmpty() || descripcion.isEmpty() || asistentesStr.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val maxAsistentes = asistentesStr.toIntOrNull() ?: 0

            // Mandamos los datos al ViewModel
            viewModel.publicarNuevoEvento(titulo, descripcion, maxAsistentes)
        }

        // 3. Escuchamos si ha habido éxito
        viewModel.eventoCreado.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "¡Evento publicado con éxito!", Toast.LENGTH_SHORT).show()
                finish() // Cierra esta pantalla y vuelve a la lista principal automáticamente
            }
        }

        // 4. Escuchamos si ha habido un error
        viewModel.error.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
    }
}