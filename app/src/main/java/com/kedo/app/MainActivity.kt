package com.kedo.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kedo.app.ui.EventoAdapter
import com.kedo.app.ui.MainViewModel

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EventoAdapter

    // Variable global para guardar el mapa cuando esté listo
    private var googleMap: GoogleMap? = null

    // Variable para la antena del GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Contrato para pedir el permiso de ubicación
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // ¡El usuario ha dicho que SÍ! Activamos el GPS.
            enableMyLocation()
        } else {
            // El usuario ha dicho NO. Mostramos un aviso y lo dejamos en Sevilla.
            Toast.makeText(this, "Para ver eventos cerca de ti, necesitamos saber tu ubicación.", Toast.LENGTH_LONG).show()
        }
    }

    // ¡FUNCIÓN onCreate!
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializamos Splash Screen y GPS antes de dibujar la interfaz
        installSplashScreen()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Encendemos el motor del mapa de forma asíncrona
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapaFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configuramos el contenedor visual de la lista (RecyclerView)
        val rvEventos = findViewById<RecyclerView>(R.id.rvEventos)
        rvEventos.layoutManager = LinearLayoutManager(this)

        adapter = EventoAdapter(emptyList())
        rvEventos.adapter = adapter

        // Configuración del botón flotante para ir al formulario
        val fabCrearEvento = findViewById<FloatingActionButton>(R.id.fabCrearEvento)

        // NUEVO: Leemos la libreta para saber quién acaba de entrar.
        val sharedPref = getSharedPreferences("KedoAppPrefs", MODE_PRIVATE)
        // Pedimos el rol. Si por algún error la libreta está vacía, asumimos que es "CLIENTE" por seguridad.
        val rolUsuario = sharedPref.getString("ROL_USUARIO", "CLIENTE")

        // NUEVO: Lógica de visibilidad.
        if (rolUsuario == "CLIENTE") {
            fabCrearEvento.visibility = View.GONE // Lo ocultamos y quitamos su espacio.
        } else {
            fabCrearEvento.visibility = View.VISIBLE // Se lo mostramos a las empresas.
        }

        fabCrearEvento.setOnClickListener {
            val intent = Intent(this, CrearEventoActivity::class.java)
            startActivity(intent)
        }

        // 1. Enlazamos el botón de salir
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // 2. Programamos el clic
        btnCerrarSesion.setOnClickListener {
            // Acción A: Borramos la libreta del teléfono
            val sharedPref = getSharedPreferences("KedoAppPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            // Acción B: Desconectamos la cuenta de Firebase
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            // Acción C: Volvemos a la pantalla de Login y cerramos el mapa
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }



        // Inicializamos el ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Escuchamos los datos del servidor para actualizar la lista y los pines
        viewModel.eventos.observe(this) { listaEventos ->
            if (!listaEventos.isNullOrEmpty()) {
                adapter.actualizarEventos(listaEventos)

                googleMap?.let { mapa ->
                    pintarPinesDeEventos(mapa, listaEventos)
                }
            } else {
                Toast.makeText(this, "No hay eventos creados todavía", Toast.LENGTH_SHORT).show()
            }
        }

        // Escuchamos errores de conexión
        viewModel.error.observe(this) { mensajeError ->
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
        }

        // Solicitamos la descarga de datos de Spring Boot
        viewModel.cargarEventos()
    }

    /**
     * Este método se ejecuta automáticamente cuando Google Maps está listo para usarse.
     */
    override fun onMapReady(mapa: GoogleMap) {
        // Guardamos el mapa en nuestra variable global
        this.googleMap = mapa

        // Habilitamos los controles de zoom nativos (+ / -)
        mapa.uiSettings.isZoomControlsEnabled = true

        // Centramos la cámara por defecto en Sevilla (ubicación inicial de seguridad)
        val sevillaCentro = LatLng(37.3891, -5.9845)
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(sevillaCentro, 12f))

        // Si los eventos se descargaron antes de que el mapa cargara, los pintamos
        viewModel.eventos.value?.let { lista ->
            pintarPinesDeEventos(mapa, lista)
        }

        // Arrancamos el protocolo GPS
        enableMyLocation()
    }

    /**
     * Función para limpiar el mapa y dibujar los marcadores de la lista.
     */
    private fun pintarPinesDeEventos(mapa: GoogleMap, eventos: List<com.kedo.app.domain.Evento>) {
        mapa.clear()
        for (evento in eventos) {
            val posicion = LatLng(evento.latitud, evento.longitud)
            val opcionesMarcador = MarkerOptions()
                .position(posicion)
                .title(evento.titulo)
                .snippet(evento.descripcion)
            mapa.addMarker(opcionesMarcador)
        }
    }

    /**
     * Función que comprueba los permisos y arranca la cámara hacia la ubicación real
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // 1. Comprobamos si el permiso ya está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Como nos dio permiso, encendemos la capa del mapa que dibuja el puntito azul
            googleMap?.isMyLocationEnabled = true

            // Leemos las coordenadas actuales del GPS
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Volamos con la cámara desde Sevilla hacia las coordenadas reales del usuario
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } else {
            // 2. Si no tenemos permiso, lanzamos la alerta de Android para pedirlo
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}