package com.kedo.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Calendar

class CrearEventoActivity : AppCompatActivity() {

    // Variables visuales
    private lateinit var btnSeleccionarFecha: Button
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var btnSeleccionarHora: Button
    private lateinit var tvHoraSeleccionada: TextView
    private lateinit var btnObtenerUbicacion: Button
    private lateinit var tvUbicacionSeleccionada: TextView
    private lateinit var etTituloEvento: com.google.android.material.textfield.TextInputEditText
    private lateinit var etDescripcionEvento: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnPublicarEvento: Button

    // Variable para la arquitectura MVVM
    private lateinit var viewModel: com.kedo.app.ui.CrearEventoViewModel

    // Variable para la antena del GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variables globales donde guardaremos las coordenadas reales
    private var latitudEvento: Double = 0.0
    private var longitudEvento: Double = 0.0

    // Contrato para pedir el permiso de ubicación de forma segura
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            obtenerUbicacionGPS()
        } else {
            Toast.makeText(this, "Necesitamos el GPS para poder publicar tu evento en el mapa.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_evento)

        // Inicializamos el motor del GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Enlazamos las variables con la pantalla
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        btnSeleccionarHora = findViewById(R.id.btnSeleccionarHora)
        tvHoraSeleccionada = findViewById(R.id.tvHoraSeleccionada)
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion)
        tvUbicacionSeleccionada = findViewById(R.id.tvUbicacionSeleccionada)
        etTituloEvento = findViewById(R.id.etTituloEvento)
        etDescripcionEvento = findViewById(R.id.etDescripcionEvento)
        btnPublicarEvento = findViewById(R.id.btnPublicarEvento)

        // Inicializamos el ViewModel
        viewModel = androidx.lifecycle.ViewModelProvider(this)[com.kedo.app.ui.CrearEventoViewModel::class.java]

        // ==========================================
        // LOS CLICS DE LOS BOTONES
        // ==========================================

        btnSeleccionarFecha.setOnClickListener {
            abrirCalendario()
        }

        btnSeleccionarHora.setOnClickListener {
            abrirSelectorHorario()
        }

        btnObtenerUbicacion.setOnClickListener {
            // Antes de buscar, comprobamos si tenemos permiso
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionGPS()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // ==========================================
        // PUBLICAR EL EVENTO
        // ==========================================
        btnPublicarEvento.setOnClickListener {
            val titulo = etTituloEvento.text.toString().trim()
            val descripcion = etDescripcionEvento.text.toString().trim()
            val fecha = tvFechaSeleccionada.text.toString()
            val horario = tvHoraSeleccionada.text.toString()

            // 1. Validamos que no haya campos vacíos
            if (titulo.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Por favor, escribe un título y una descripción al Evento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fecha.contains("no") || horario.contains("Ej:")) {
                Toast.makeText(this, "Por favor, selecciona una fecha y un horario para el Evento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (latitudEvento == 0.0 || longitudEvento == 0.0) {
                Toast.makeText(this, "Por favor, pulsa en 'Usar mi GPS' para localizar el Evento en el Mapa.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Traducción de Fecha para Spring Boot.
            val partesFecha = fecha.split("/")
            val dia = partesFecha[0]
            val mes = partesFecha[1]
            val anio = partesFecha[2]

            // Usamos substringBefore para evitar avisos de Android Studio
            val horaInicio = horario.substringBefore(" - ")

            val fechaTecnicaBackend = "${anio}-${mes}-${dia}T${horaInicio}:00"
            val descripcionConHorario = "$descripcion\n\nHorario del evento: $horario"

            // 3. Se lo enviamos al ViewModel usando descripcionConHorario
            viewModel.publicarNuevoEvento(titulo, descripcionConHorario, latitudEvento, longitudEvento, fechaTecnicaBackend)
        }

        // ==========================================
        // ESCUCHAMOS LAS RESPUESTAS DEL SERVIDOR
        // ==========================================
        viewModel.eventoCreado.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "¡Evento publicado con éxito!", Toast.LENGTH_LONG).show()
                finish() // Cierra la pantalla y vuelve al mapa
            }
        }

        viewModel.error.observe(this) { mensajeError ->
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
        }

    } // AQUÍ TERMINA LA FUNCIÓN ONCREATE


    // ==========================================
    // FUNCIONES AUXILIARES (Dentro de la clase)
    // ==========================================

    private fun abrirCalendario() {
        val calendario = Calendar.getInstance()
        val añoActual = calendario.get(Calendar.YEAR)
        val mesActual = calendario.get(Calendar.MONTH)
        val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val mesReal = month + 1
            val fechaFormateada = String.format("%02d/%02d/%d", dayOfMonth, mesReal, year)
            tvFechaSeleccionada.text = fechaFormateada
        }, añoActual, mesActual, diaActual)

        datePickerDialog.datePicker.minDate = calendario.timeInMillis
        datePickerDialog.show()
    }

    private fun abrirSelectorHorario() {
        val calendario = Calendar.getInstance()
        val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendario.get(Calendar.MINUTE)

        val timePickerDialogInicio = TimePickerDialog(this, { _, horaInicio, minutoInicio ->

            val timePickerDialogFin = TimePickerDialog(this, { _, horaFin, minutoFin ->
                val horarioFinal = String.format("%02d:%02d - %02d:%02d", horaInicio, minutoInicio, horaFin, minutoFin)
                tvHoraSeleccionada.text = horarioFinal
            }, horaInicio + 1, minutoInicio, true)

            timePickerDialogFin.setTitle("Selecciona la hora de FIN")
            timePickerDialogFin.show()

        }, horaActual, minutoActual, true)

        timePickerDialogInicio.setTitle("Selecciona la hora de INICIO")
        timePickerDialogInicio.show()
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionGPS() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitudEvento = location.latitude
                longitudEvento = location.longitude

                val textoUbicacion = String.format("Lat: %.4f, Lng: %.4f", location.latitude, location.longitude)
                tvUbicacionSeleccionada.text = textoUbicacion

                Toast.makeText(this, "¡Ubicación capturada!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Buscando satélites... intenta darle de nuevo", Toast.LENGTH_SHORT).show()
            }
        }
    }
} // AQUÍ TERMINA LA CLASE