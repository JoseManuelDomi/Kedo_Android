package com.kedo.app

//IMPORTS
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    // 1. Declaramos las variables visuales
    private lateinit var toggleGroupAuth: MaterialButtonToggleGroup
    private lateinit var layoutRegistroExtra: LinearLayout
    private lateinit var btnAccionAuth: Button

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etNombre: TextInputEditText
    private lateinit var rbCliente: RadioButton

    // 2. Declaramos el motor de Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance()

        /*
        ¡CONTROL DE PERSISTENCIA!
        Preguntaremos a Firebase si ya hay un usuario logueado en este mismo dispositivo.
         */
        if (auth.currentUser != null) {
            irAlMapa()
            return
        }

        setContentView(R.layout.activity_auth)

        // Enlazamos las variables con los ID del XML
        toggleGroupAuth = findViewById(R.id.toggleGroupAuth)
        layoutRegistroExtra = findViewById(R.id.layoutRegistroExtra)
        btnAccionAuth = findViewById(R.id.btnAccionAuth)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etNombre = findViewById(R.id.etNombre)
        rbCliente = findViewById(R.id.rbCliente)

        // Escuchamos el interruptor de pestañas
        toggleGroupAuth.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnLoginTab -> {
                        layoutRegistroExtra.visibility = View.GONE
                        btnAccionAuth.text = "Iniciar Sesión"
                    }
                    R.id.btnRegisterTab -> {
                        layoutRegistroExtra.visibility = View.VISIBLE
                        btnAccionAuth.text = "Crear Cuenta"
                    }
                }
            }
        }

        // Lógica principal al pulsar el botón
        btnAccionAuth.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Mini-validación: Evitamos que manden campos vacíos
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Cortamos la ejecución aquí
            }

            if (layoutRegistroExtra.visibility == View.VISIBLE) {
                // MODO REGISTRO
                val nombre = etNombre.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(this, "El nombre es obligatorio para registrarse", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Extraemos el rol (Cliente o Empresa)
                val rol = if (rbCliente.isChecked) "CLIENTE" else "EMPRESA"

                //1. NUEVO: Guardamos el rol en la libreta del teléfono (SharedPreferences).
                val sharedPref = getSharedPreferences("KedoAppPrefs", MODE_PRIVATE)
                sharedPref.edit().putString("ROL_USUARIO", rol).apply()

                registrarUsuario(email, password, nombre, rol)
            } else {
                // MODO LOGIN
                iniciarSesion(email, password)
            }
        }
    }

    /**
     * Función para crear un usuario nuevo en Firebase
     */
    /**
     * Función para crear un usuario nuevo en Firebase y posteriormente guardarlo en Spring Boot
     */
    private fun registrarUsuario(email: String, pass: String, nombre: String, rol: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // ¡Éxito en Firebase! Extraemos el identificador único (UID)
                    val firebaseUser = auth.currentUser
                    val firebaseUid = firebaseUser?.uid ?: ""

                    // Construimos el objeto Usuario para tu backend.
                    // Nota: Pasamos la contraseña vacía o un valor por defecto, ya que la seguridad la gestiona Firebase.
                    // Si tu Spring Boot requiere un ID de tipo Long, usaremos temporalmente null para que lo genere,
                    // pero si puedes cambiarlo a String en tu backend para guardar el UID de Firebase, sería lo ideal.
                    val nuevoUsuario = com.kedo.app.domain.Usuario(
                        id = null, // Spring Boot generará el ID Long por defecto
                        nombre = nombre,
                        email = email,
                        rol = rol,
                        password = "" // Firebase ya la gestiona de forma segura
                    )


                    lifecycleScope.launch {
                        try {
                            val response = com.kedo.app.data.RetrofitClient.apiService.registrarUsuarioBackend(nuevoUsuario)

                            if (response.isSuccessful) {
                                // ¡Éxito total! Guardado en Firebase y en tu base de datos
                                Toast.makeText(this@AuthActivity, "¡Cuenta creada con éxito en el sistema!", Toast.LENGTH_SHORT).show()
                                irAlMapa()
                            } else {
                                // El servidor respondió pero con un código de error (ej: 400 o 500)
                                Toast.makeText(this@AuthActivity, "Firebase correcto, pero error en servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                                // Opcional: podrías decidir ir al mapa igualmente o gestionar el reintento
                                irAlMapa()
                            }
                        } catch (e: Exception) {
                            // Error de conexión (ej: el servidor Spring Boot está apagado)
                            Toast.makeText(this@AuthActivity, "Sin conexión con el servidor. Cuenta local activa.", Toast.LENGTH_LONG).show()
                            irAlMapa()
                        }
                    }

                } else {
                    // Fallo en Firebase (ej: contraseña débil o correo ya registrado)
                    Toast.makeText(this, "Error al registrar en Firebase: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Función para entrar con un usuario existente
     */
    private fun iniciarSesion(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                    irAlMapa()
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Función auxiliar para saltar a la pantalla principal
     */
    private fun irAlMapa() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Destruimos esta pantalla para que el usuario no pueda volver al Login dándole al botón de "Atrás" de Android
        finish()
    }
}