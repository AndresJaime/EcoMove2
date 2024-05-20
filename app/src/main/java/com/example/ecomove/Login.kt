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

        // Verificar si el usuario ya está logueado
        if (autenticacion.currentUser != null) {
            // Redirigir a UbicacionTiempoReal si el usuario ya está logueado
            startActivity(Intent(this, UbicacionTiempoReal::class.java))
            finish()
            return
        }

        // Inicializa las vistas (campos de texto y botones)
        val correoEditText: EditText = findViewById(R.id.emailEditText)
        val contrasenaEditText: EditText = findViewById(R.id.passwordEditText)
        val botonIniciarSesion: Button = findViewById(R.id.loginButton)
        val botonRegistrarse: Button = findViewById(R.id.signUpButton)
        //val botonMapaTiempoReal: Button = findViewById(R.id.real)
       // val botonCambiarContrasena: Button = findViewById(R.id.changePasswordButton)
        val textoOlvidasteContrasena: TextView = findViewById(R.id.forgotPasswordTextView)

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

        // Configura el listener para el botón de registrarse
        botonRegistrarse.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }



        // Configura el listener para el texto de olvidaste tu contraseña
        textoOlvidasteContrasena.setOnClickListener {
            val correo = correoEditText.text.toString().trim()

            // Verifica si el campo de correo está vacío
            if (correo.isEmpty()) {
                correoEditText.error = "Correo electrónico es requerido para reestablecer la contraseña"
                correoEditText.requestFocus()
                return@setOnClickListener
            }

            // Llama al método para reestablecer la contraseña
            reestablecerContrasena(correo)
        }
    }

    // Método para iniciar sesión con FirebaseAuth
    private fun iniciarSesion(correo: String, contrasena: String) {
        autenticacion.signInWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val usuario = autenticacion.currentUser
                    val uid = usuario?.uid
                    val intent = Intent(this, UbicacionTiempoReal::class.java) // Aquí es donde se especifica la actividad a la que se redirige después de loguearse
                    intent.putExtra("USER_UID", uid)
                    startActivity(intent)
                    finish()
                } else {
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

    // Método para reestablecer la contraseña con FirebaseAuth
    private fun reestablecerContrasena(correo: String) {
        autenticacion.sendPasswordResetEmail(correo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo para reestablecer la contraseña enviado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de reestablecimiento", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
