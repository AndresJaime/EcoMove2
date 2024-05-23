package com.example.ecomove

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdaptadorDeslizadorBicicletas(
    private val bicicletas: List<Bicicleta>,
    private val itemClickListener: (Bicicleta) -> Unit
) : RecyclerView.Adapter<AdaptadorDeslizadorBicicletas.BicicletaViewHolder>() {

    class BicicletaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.tituloBicicletaTextView)
        val imagenImageView: ImageView = view.findViewById(R.id.imagenBicicletaImageView)
        val existenciasTextView: TextView = view.findViewById(R.id.existenciasBicicletaTextView)
        val reservarButton: Button = view.findViewById(R.id.reservarButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BicicletaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bicicleta, parent, false)
        return BicicletaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BicicletaViewHolder, position: Int) {
        val bicicleta = bicicletas[position]
        holder.tituloTextView.text = bicicleta.nombre
        holder.existenciasTextView.text = "Existencias: ${bicicleta.existencias}"
        holder.imagenImageView.setImageResource(bicicleta.imagen)

        holder.reservarButton.setOnClickListener { itemClickListener(bicicleta) }
    }

    override fun getItemCount(): Int {
        return bicicletas.size
    }
}

data class Bicicleta(val nombre: String, val existencias: Int, val imagen: Int)
