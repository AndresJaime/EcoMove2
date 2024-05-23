package com.example.ecomove

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    // Inicializa la instancia de FirebaseAuth
    private lateinit var autenticacion: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Obtiene la instancia de autenticación de Firebase
        autenticacion = FirebaseAuth.getInstance()

        // Verifica si el usuario ya está logueado
        if (autenticacion.currentUser != null) {
            // Redirige a UbicacionTiempoReal si el usuario ya está logueado
            startActivity(Intent(this, UbicacionTiempoReal::class.java))
            finish()
            return
        }

        // Inicializa las vistas (campos de texto y botones)
        val correoEditText: EditText = findViewById(R.id.emailEditText)
        val contrasenaEditText: EditText = findViewById(R.id.passwordEditText)
        val botonIniciarSesion: Button = findViewById(R.id.loginButton)
        val textoOlvidasteContrasena: TextView = findViewById(R.id.forgotPasswordTextView)
        val textoRegistrarse: TextView = findViewById(R.id.signUpLinkTextView)

        // Configura el listener para el botón de iniciar sesión
        botonIniciarSesion.setOnClickListener {
            val correo = correoEditText.text.toString().trim()
            val contrasena = contrasenaEditText.text.toString().trim()

            // Verifica si el campo de correo está vacío
            if (correo.isEmpty()) {
                correoEditText.error = "Correo electrónico es requerido"
                correoEditText.requestFocus()
                return@setOnClickListener
            }

            // Verifica si el campo de contraseña está vacío
            if (contrasena.isEmpty()) {
                contrasenaEditText.error = "Contraseña es requerida"
                contrasenaEditText.requestFocus()
                return@setOnClickListener
            }

            // Llama al método para iniciar sesión
            iniciarSesion(correo, contrasena)
        }

        // Configura el listener para el texto de olvidaste tu contraseña
        textoOlvidasteContrasena.setOnClickListener {
            // Redirige a la actividad CambiarContraseña
            val intent = Intent(this, CambiarContraseña::class.java)
            startActivity(intent)
        }

        // Configura el listener para el texto de registrarse
        textoRegistrarse.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
    }

    // Método para iniciar sesión con FirebaseAuth
    private fun iniciarSesion(correo: String, contrasena: String) {
        autenticacion.signInWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso, redirige a la actividad principal
                    val usuario = autenticacion.currentUser
                    val uid = usuario?.uid
                    val intent = Intent(this, UbicacionTiempoReal::class.java) // Redirige a UbicacionTiempoReal después de iniciar sesión
                    intent.putExtra("USER_UID", uid)
                    startActivity(intent)
                    finish()
                } else {
                    // Muestra un error si el inicio de sesión falla
                    mostrarErrorInicioSesion()
                }
            }
    }

    // Método para mostrar un error si el inicio de sesión falla
    private fun mostrarErrorInicioSesion() {
        val contrasenaEditText: EditText = findViewById(R.id.passwordEditText)
        contrasenaEditText.error = "La contraseña es incorrecta o la cuenta no existe"
        contrasenaEditText.requestFocus()
    }
}
