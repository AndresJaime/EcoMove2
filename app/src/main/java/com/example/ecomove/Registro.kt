package com.example.ecomove

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registro : AppCompatActivity() {

    private lateinit var autenticacion: FirebaseAuth
    private lateinit var baseDatos: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        autenticacion = FirebaseAuth.getInstance()
        baseDatos = FirebaseDatabase.getInstance().reference.child("users")

        val nombreUsuarioEditText: EditText = findViewById(R.id.usernameEditText)
        val correoEditText: EditText = findViewById(R.id.emailEditText)
        val telefonoEditText: EditText = findViewById(R.id.phoneEditText)
        val contrasenaEditText: EditText = findViewById(R.id.passwordEditText)
        val confirmarContrasenaEditText: EditText = findViewById(R.id.confirmPasswordEditText)
        val botonRegistrar: Button = findViewById(R.id.registerButton)

        botonRegistrar.setOnClickListener {
            val nombreUsuario = nombreUsuarioEditText.text.toString().trim()
            val correo = correoEditText.text.toString().trim()
            val telefono = telefonoEditText.text.toString().trim()
            val contrasena = contrasenaEditText.text.toString().trim()
            val confirmarContrasena = confirmarContrasenaEditText.text.toString().trim()

            if (nombreUsuario.isEmpty() || correo.isEmpty() || telefono.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            } else if (contrasena != confirmarContrasena) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                registrarUsuario(nombreUsuario, correo, telefono, contrasena)
            }
        }
    }

    private fun registrarUsuario(nombreUsuario: String, correo: String, telefono: String, contrasena: String) {
        autenticacion.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val idUsuario = autenticacion.currentUser?.uid
                    val usuarioDb = idUsuario?.let { baseDatos.child(it) }
                    val usuario = Usuario(nombreUsuario, correo, telefono)
                    usuarioDb?.setValue(usuario)?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error al guardar datos de usuario: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Error de registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    data class Usuario(
        val nombreUsuario: String = "",
        val correo: String = "",
        val telefono: String = ""
    )
}
