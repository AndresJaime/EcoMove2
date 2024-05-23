package com.example.ecomove

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

open class Menu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        setupMenu(findViewById(R.id.menu_superpuesto), findViewById(R.id.boton_menu), findViewById(R.id.boton_cerrar))
    }

    fun setupMenu(menuSuperpuesto: View, botonMenu: ImageView, botonCerrar: ImageView) {
        botonMenu.setOnClickListener {
            menuSuperpuesto.visibility = if (menuSuperpuesto.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        botonCerrar.setOnClickListener {
            menuSuperpuesto.visibility = View.GONE
        }

        menuSuperpuesto.findViewById<Button>(R.id.boton_gestionar_viajes).setOnClickListener {
            // Lógica para gestionar viajes
        }

        menuSuperpuesto.findViewById<Button>(R.id.boton_perfil).setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        menuSuperpuesto.findViewById<Button>(R.id.boton_visita_la_tienda).setOnClickListener {
            // Lógica para visitar la tienda
        }
    }
}
