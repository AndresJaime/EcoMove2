package com.example.ecomove

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Localizacion : AppCompatActivity() {

    private lateinit var baseDatos: DatabaseReference
    private lateinit var sucursalId: String
    private lateinit var tituloTextView: TextView
    private lateinit var deslizadorBicicletas: ViewPager2
    private lateinit var adaptadorDeslizador: AdaptadorDeslizadorBicicletas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localizacion)

        sucursalId = intent.getStringExtra("sucursalId") ?: ""
        val sucursalTitulo = intent.getStringExtra("sucursalTitulo") ?: "Sucursal"
        tituloTextView = findViewById(R.id.tituloTextView)
        deslizadorBicicletas = findViewById(R.id.deslizadorBicicletas)
        val volverButton: ImageView = findViewById(R.id.volverButton)

        tituloTextView.text = sucursalTitulo

        volverButton.setOnClickListener {
            val intent = Intent(this, UbicacionTiempoReal::class.java)
            startActivity(intent)
        }

        baseDatos = FirebaseDatabase.getInstance().getReference("sucursales/$sucursalId")

        baseDatos.get().addOnSuccessListener { snapshot ->
            val productosSnapshot = snapshot.child("productos")
            val bicicletas = mutableListOf<Bicicleta>()

            productosSnapshot.children.forEach { productoSnapshot ->
                val nombre = productoSnapshot.key ?: ""
                val existencias = productoSnapshot.child("existencias").getValue(Int::class.java) ?: 0
                val imagen = when (nombre) {
                    "Mark I" -> R.drawable.mark1
                    "Mark II" -> R.drawable.mark2
                    "Mark III" -> R.drawable.mark3
                    else -> R.drawable.ic_bike_placeholder
                }
                bicicletas.add(Bicicleta(nombre, existencias, imagen))
            }

            adaptadorDeslizador = AdaptadorDeslizadorBicicletas(bicicletas) { bicicleta ->
                val intent = Intent(this, ReservarBicicletaActivity::class.java)
                intent.putExtra("sucursalId", sucursalId)
                intent.putExtra("bicicletaNombre", bicicleta.nombre)
                intent.putExtra("bicicletaExistencias", bicicleta.existencias)
                intent.putExtra("bicicletaImagen", bicicleta.imagen)
                startActivity(intent)
            }

            deslizadorBicicletas.adapter = adaptadorDeslizador
        }
    }
}
