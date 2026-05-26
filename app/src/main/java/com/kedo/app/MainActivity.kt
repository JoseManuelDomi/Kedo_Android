package com.kedo.app

//IMPORTS
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kedo.app.ui.EventoAdapter
import com.kedo.app.ui.MainViewModel

// 1. Añadimos OnMapReadyCallback al contrato de la clase
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EventoAdapter

    // Variable global para guardar el mapa cuando esté listo
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Encendemos el motor del mapa de forma asíncrona
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapaFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 3. Configuramos el contenedor visual de la lista (RecyclerView)
        val rvEventos = findViewById<RecyclerView>(R.id.rvEventos)
        rvEventos.layoutManager = LinearLayoutManager(this)

        adapter = EventoAdapter(emptyList())
        rvEventos.adapter = adapter

        // 4. Configuración del botón flotante para ir al formulario
        val fabCrearEvento = findViewById<FloatingActionButton>(R.id.fabCrearEvento)
        fabCrearEvento.setOnClickListener {
            val intent = Intent(this, CrearEventoActivity::class.java)
            startActivity(intent)
        }

        // 5. Inicializamos el ViewModel (El cerebro de la pantalla)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 6. Escuchamos los datos del servidor para actualizar la lista y los pines
        viewModel.eventos.observe(this) { listaEventos ->
            if (!listaEventos.isNullOrEmpty()) {
                // Actualizamos las tarjetas de la lista abajo
                adapter.actualizarEventos(listaEventos)

                // Si el mapa ya terminó de cargar, le pintamos los pines
                googleMap?.let { mapa ->
                    pintarPinesDeEventos(mapa, listaEventos)
                }
            } else {
                Toast.makeText(this, "No hay eventos creados todavía", Toast.LENGTH_SHORT).show()
            }
        }

        // 7. Escuchamos errores de conexión
        viewModel.error.observe(this) { mensajeError ->
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
        }

        // 8. Solicitamos la descarga de datos de Spring Boot
        viewModel.cargarEventos()
    }

    /**
     * Este método se ejecuta automáticamente cuando Google Maps está listo para usarse.
     */
    override fun onMapReady(mapa: GoogleMap) {
        // Guardamos el mapa en nuestra variable global
        this.googleMap = mapa

        // Habilitamos los controles de zoom nativos (+ / -) en el lateral del mapa
        mapa.uiSettings.isZoomControlsEnabled = true

        // Centramos la cámara por defecto en una ubicación inicial (Sevilla)
        val sevillaCentro = LatLng(37.3891, -5.9845)
        // El valor 12f es el nivel de zoom (cuanto más alto, más cerca)
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(sevillaCentro, 12f))

        // Si los eventos se descargaron antes de que el mapa cargara, los pintamos ahora
        viewModel.eventos.value?.let { lista ->
            pintarPinesDeEventos(mapa, lista)
        }
    }

    /**
     * Función auxiliar didáctica para limpiar el mapa y dibujar los marcadores de la lista.
     */
    private fun pintarPinesDeEventos(mapa: GoogleMap, eventos: List<com.kedo.app.domain.Evento>) {
        // Borramos los marcadores antiguos para no duplicar si refrescamos datos
        mapa.clear()

        // Recorremos la lista con un bucle for-each
        for (evento in eventos) {
            val posicion = LatLng(evento.latitud, evento.longitud)

            // Configuramos el "Globo/Pin" del mapa
            val opcionesMarcador = MarkerOptions()
                .position(posicion)
                .title(evento.titulo)
                .snippet(evento.descripcion) // Texto secundario al pulsar el pin

            // Lo plantamos en el mapa
            mapa.addMarker(opcionesMarcador)
        }
    }
}