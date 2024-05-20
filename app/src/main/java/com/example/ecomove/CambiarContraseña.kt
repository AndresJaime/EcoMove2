package com.example.ecomove

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CambiarContraseña : AppCompatActivity() {

    private lateinit var autenticacion: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_contrasena)

        autenticacion = FirebaseAuth.getInstance()

        val correoEditText: EditText = findViewById(R.id.emailEditText)
        val botonCambiarContrasena: Button = findViewById(R.id.changePasswordButton)

        botonCambiarContrasena.setOnClickListener {
            val correo = correoEditText.text.toString()

            if (correo.isNotEmpty()) {
                enviarCorreoRestablecimiento(correo)
            } else {
                Toast.makeText(this, "Por favor, ingrese su correo electrónico.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarCorreoRestablecimiento(correo: String) {
        autenticacion.sendPasswordResetEmail(correo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo enviado. Por favor revise su correo para restablecer la contraseña.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de restablecimiento.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
