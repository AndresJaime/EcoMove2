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
    private lateinit var pagarButton: Button
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
        pagarButton = findViewById(R.id.pagarButton)

        tituloTextView.text = bicicletaNombre
        existenciasTextView.text = "Existencias: $bicicletaExistencias"

        // Configura la imagen de la bicicleta de forma dinámica
        val imagenResId = intent.getIntExtra("bicicletaImagen", R.drawable.ic_bike_placeholder)
        imagenImageView.setImageResource(imagenResId)

        baseDatos = FirebaseDatabase.getInstance().getReference("sucursales/$sucursalId/productos/$bicicletaNombre")

        ahoraYaButton.setOnClickListener {
            pagarButton.visibility = View.VISIBLE
            horasDisponiblesLayout.visibility = View.GONE
            resetAgendarButton()
        }

        agendarParaMasTardeButton.setOnClickListener {
            if (isAgendarMode) {
                resetAgendarButton()
            } else {
                horasDisponiblesLayout.visibility = View.VISIBLE
                pagarButton.visibility = View.GONE
                agendarParaMasTardeButton.text = "Cancelar"
                isAgendarMode = true
            }
        }

        // Agregar listeners a los botones de horas disponibles
        val horasDisponibles = listOf(
            R.id.hora12Button, R.id.hora1Button, R.id.hora2Button, R.id.hora3Button,
            R.id.hora4Button, R.id.hora5Button, R.id.hora6Button, R.id.hora7Button,
            R.id.hora8Button, R.id.hora9Button, R.id.hora10Button, R.id.hora11Button
        )

        for (hora in horasDisponibles) {
            findViewById<Button>(hora).setOnClickListener {
                mostrarOpcionesDePago()
            }
        }

        pagarButton.setOnClickListener {
            modificarExistencias()
        }
    }

    private fun mostrarOpcionesDePago() {
        pagarButton.visibility = View.VISIBLE
    }

    private fun modificarExistencias() {
        if (bicicletaExistencias > 0) {
            val nuevasExistencias = bicicletaExistencias - 1
            baseDatos.child("existencias").setValue(nuevasExistencias)
                .addOnSuccessListener {
                    Toast.makeText(this, "Has pagado por $bicicletaNombre", Toast.LENGTH_SHORT).show()
                    existenciasTextView.text = "Existencias: $nuevasExistencias"
                    bicicletaExistencias = nuevasExistencias
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al pagar por $bicicletaNombre", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No hay existencias de $bicicletaNombre", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetAgendarButton() {
        horasDisponiblesLayout.visibility = View.GONE
        agendarParaMasTardeButton.text = "Agendar para más tarde"
        pagarButton.visibility = View.GONE
        isAgendarMode = false
    }
}
