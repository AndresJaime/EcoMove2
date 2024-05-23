package com.example.ecomove

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ReservarBicicletaActivity : AppCompatActivity() {

    private lateinit var baseDatos: DatabaseReference
    private lateinit var sucursalId: String
    private lateinit var bicicletaNombre: String
    private var bicicletaExistencias: Int = 0
    private lateinit var tituloTextView: TextView
    private lateinit var imagenImageView: ImageView
    private lateinit var existenciasTextView: TextView
    private lateinit var ahoraYaButton: Button
    private lateinit var agendarParaMasTardeButton: Button
    private lateinit var horasDisponiblesLayout: View
    private lateinit var comprarButton: Button
    private lateinit var alquilarButton: Button
    private var isAgendarMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservar_bicicleta)

        sucursalId = intent.getStringExtra("sucursalId") ?: ""
        bicicletaNombre = intent.getStringExtra("bicicletaNombre") ?: ""
        bicicletaExistencias = intent.getIntExtra("bicicletaExistencias", 0)

        tituloTextView = findViewById(R.id.bicicletaTituloTextView)
        imagenImageView = findViewById(R.id.bicicletaImageView)
        existenciasTextView = findViewById(R.id.bicicletaExistenciasTextView)
        ahoraYaButton = findViewById(R.id.ahoraYaButton)
        agendarParaMasTardeButton = findViewById(R.id.agendarParaMasTardeButton)
        horasDisponiblesLayout = findViewById(R.id.horasDisponiblesLayout)
        comprarButton = findViewById(R.id.comprarButton)
        alquilarButton = findViewById(R.id.alquilarButton)

        tituloTextView.text = bicicletaNombre
        existenciasTextView.text = "Existencias: $bicicletaExistencias"

        // Configura la imagen de la bicicleta de forma dinámica
        val imagenResId = intent.getIntExtra("bicicletaImagen", R.drawable.ic_bike_placeholder)
        imagenImageView.setImageResource(imagenResId)

        baseDatos = FirebaseDatabase.getInstance().getReference("sucursales/$sucursalId/productos/$bicicletaNombre")

        ahoraYaButton.setOnClickListener {
            comprarButton.visibility = View.VISIBLE
            alquilarButton.visibility = View.VISIBLE
            horasDisponiblesLayout.visibility = View.GONE
            resetAgendarButton()
        }

        agendarParaMasTardeButton.setOnClickListener {
            if (isAgendarMode) {
                resetAgendarButton()
            } else {
                horasDisponiblesLayout.visibility = View.VISIBLE
                comprarButton.visibility = View.GONE
                alquilarButton.visibility = View.GONE
                agendarParaMasTardeButton.text = "Cancelar"
                isAgendarMode = true
            }
        }

        // Agregar listeners a los botones de horas disponibles
        findViewById<Button>(R.id.hora12Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora1Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora2Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora3Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora4Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora5Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora6Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora7Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora8Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        findViewById<Button>(R.id.hora9Button).setOnClickListener {
            mostrarOpcionesDeCompraAlquiler()
        }

        comprarButton.setOnClickListener {
            modificarExistencias("comprar")
        }

        alquilarButton.setOnClickListener {
            modificarExistencias("alquilar")
        }
    }

    private fun mostrarOpcionesDeCompraAlquiler() {
        comprarButton.visibility = View.VISIBLE
        alquilarButton.visibility = View.VISIBLE
    }

    private fun modificarExistencias(accion: String) {
        if (bicicletaExistencias > 0) {
            val nuevasExistencias = bicicletaExistencias - 1
            baseDatos.child("existencias").setValue(nuevasExistencias)
                .addOnSuccessListener {
                    Toast.makeText(this, "Has $accion $bicicletaNombre", Toast.LENGTH_SHORT).show()
                    existenciasTextView.text = "Existencias: $nuevasExistencias"
                    bicicletaExistencias = nuevasExistencias
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al $accion $bicicletaNombre", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No hay existencias de $bicicletaNombre", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetAgendarButton() {
        horasDisponiblesLayout.visibility = View.GONE
        agendarParaMasTardeButton.text = "Agendar para más tarde"
        isAgendarMode = false
    }
}
