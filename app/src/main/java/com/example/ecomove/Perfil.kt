package com.example.ecomove

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Perfil : AppCompatActivity() {

    private lateinit var autenticacion: FirebaseAuth
    private lateinit var baseDatos: DatabaseReference
    private lateinit var usuarioActual: FirebaseUser

    private lateinit var nombreEditText: EditText
    private lateinit var correoEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var botonGuardar: Button
    private lateinit var botonCerrarSesion: Button
    private lateinit var botonVolver: Button

    private var enModoEdicion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        autenticacion = FirebaseAuth.getInstance()
        baseDatos = FirebaseDatabase.getInstance().reference.child("users")
        usuarioActual = autenticacion.currentUser!!

        nombreEditText = findViewById(R.id.nombreEditText)
        correoEditText = findViewById(R.id.correoEditText)
        telefonoEditText = findViewById(R.id.telefonoEditText)
        botonGuardar = findViewById(R.id.botonGuardar)
        botonCerrarSesion = findViewById(R.id.botonCerrarSesion)
        botonVolver = findViewById(R.id.botonVolver)

        cargarDatosUsuario()

        // Configura el botón para iniciar en modo "Editar"
        botonGuardar.text = "Editar"

        botonGuardar.setOnClickListener {
            if (enModoEdicion) {
                guardarDatosUsuario()
            } else {
                habilitarEdicion()
            }
        }

        botonCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        botonVolver.setOnClickListener {
            finish() // Finaliza la actividad actual y vuelve a la actividad anterior
        }
    }

    // Habilita los campos de edición y cambia el texto del botón a "Guardar"
    private fun habilitarEdicion() {
        nombreEditText.isEnabled = true
        correoEditText.isEnabled = true
        telefonoEditText.isEnabled = true
        botonGuardar.text = "Guardar"
        enModoEdicion = true
    }

    // Deshabilita los campos de edición y cambia el texto del botón a "Editar"
    private fun deshabilitarEdicion() {
        nombreEditText.isEnabled = false
        correoEditText.isEnabled = false
        telefonoEditText.isEnabled = false
        botonGuardar.text = "Editar"
        enModoEdicion = false
    }

    private fun cargarDatosUsuario() {
        val usuarioId = usuarioActual.uid
        baseDatos.child(usuarioId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usuario = dataSnapshot.getValue(Usuario::class.java)
                if (usuario != null) {
                    nombreEditText.setText(usuario.nombreUsuario)
                    correoEditText.setText(usuario.correo)
                    telefonoEditText.setText(usuario.telefono)
                    deshabilitarEdicion() // Deshabilita edición al cargar los datos
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Perfil, "Error al cargar datos.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun guardarDatosUsuario() {
        val usuarioId = usuarioActual.uid
        val nombreNuevo = nombreEditText.text.toString().trim()
        val correoNuevo = correoEditText.text.toString().trim()
        val telefonoNuevo = telefonoEditText.text.toString().trim()

        if (nombreNuevo.isEmpty()) {
            nombreEditText.error = "El nombre es requerido"
            nombreEditText.requestFocus()
            return
        }

        if (correoNuevo.isEmpty()) {
            correoEditText.error = "El correo es requerido"
            correoEditText.requestFocus()
            return
        }

        if (telefonoNuevo.isEmpty()) {
            telefonoEditText.error = "El teléfono es requerido"
            telefonoEditText.requestFocus()
            return
        }

        val usuarioActualizado = Usuario(nombreNuevo, correoNuevo, telefonoNuevo)
        baseDatos.child(usuarioId).setValue(usuarioActualizado)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Datos guardados exitosamente.", Toast.LENGTH_SHORT).show()
                    deshabilitarEdicion() // Deshabilita edición después de guardar los datos
                } else {
                    Toast.makeText(this, "Error al guardar datos.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun cerrarSesion() {
        autenticacion.signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    data class Usuario(
        val nombreUsuario: String = "",
        val correo: String = "",
        val telefono: String = ""
    )
}
