package com.example.ecomove

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DetalleBicicletaActivity : AppCompatActivity() {

    private lateinit var baseDatos: DatabaseReference
    private lateinit var sucursalId: String
    private lateinit var bicicletaNombre: String
    private var bicicletaExistencias: Int = 0
    private lateinit var tituloTextView: TextView
    private lateinit var existenciasTextView: TextView
    private lateinit var comprarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_bicicleta)

        sucursalId = intent.getStringExtra("sucursalId") ?: ""
        bicicletaNombre = intent.getStringExtra("bicicletaNombre") ?: ""
        bicicletaExistencias = intent.getIntExtra("bicicletaExistencias", 0)

        tituloTextView = findViewById(R.id.bicicletaTituloTextView)
        existenciasTextView = findViewById(R.id.bicicletaExistenciasTextView)
        comprarButton = findViewById(R.id.comprarButton)

        tituloTextView.text = bicicletaNombre
        existenciasTextView.text = "Existencias: $bicicletaExistencias"

        baseDatos = FirebaseDatabase.getInstance().getReference("sucursales/$sucursalId/productos/$bicicletaNombre")

        comprarButton.setOnClickListener {
            comprarBicicleta()
        }
    }

    private fun comprarBicicleta() {
        if (bicicletaExistencias > 0) {
            val nuevasExistencias = bicicletaExistencias - 1
            baseDatos.child("existencias").setValue(nuevasExistencias)
                .addOnSuccessListener {
                    Toast.makeText(this, "Has comprado $bicicletaNombre", Toast.LENGTH_SHORT).show()
                    existenciasTextView.text = "Existencias: $nuevasExistencias"
                    bicicletaExistencias = nuevasExistencias
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al comprar $bicicletaNombre", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No hay existencias de $bicicletaNombre", Toast.LENGTH_SHORT).show()
        }
    }
}
