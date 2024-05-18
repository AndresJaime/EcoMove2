package com.example.ecomove

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val guestButton: Button = findViewById(R.id.guestButton)
        val mapatiemporeal: Button = findViewById(R.id.real)
        val changePasswordButton: Button = findViewById(R.id.changePasswordButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Correo electrónico es requerido"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Contraseña es requerida"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            signIn(email, password)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        guestButton.setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
            finish()
        }

        mapatiemporeal.setOnClickListener {
            if (auth.currentUser != null) {
                startActivity(Intent(this, UbicacionTiempoReal::class.java))
            } else {
                Toast.makeText(this, "Por favor inicie sesión para acceder a esta función.", Toast.LENGTH_SHORT).show()
            }
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, CambiarContraseña::class.java))
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    val intent = Intent(this, UbicacionTiempoReal::class.java)
                    intent.putExtra("USER_UID", uid)
                    startActivity(intent)
                    finish()
                } else {
                    showSignInError()
                }
            }
    }

    private fun showSignInError() {
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        passwordEditText.error = "La contraseña es incorrecta o la cuenta no existe"
        passwordEditText.requestFocus()
    }
}
