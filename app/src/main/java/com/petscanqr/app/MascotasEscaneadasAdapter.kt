// MascotasEscaneadasAdapter.kt
package com.petscanqr.app

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MascotasEscaneadasAdapter(private val mascotasEscaneadas: List<MascotaEscaneada>) :
    RecyclerView.Adapter<MascotasEscaneadasAdapter.MascotaEscaneadaViewHolder>() {

    class MascotaEscaneadaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val fechaTextView: TextView = itemView.findViewById(R.id.fechaTextView)
        val imagenMascotaImageView: ImageView = itemView.findViewById(R.id.imagenMascotaImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaEscaneadaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.mascota_escaneada_item_cardview, parent, false)
        return MascotaEscaneadaViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: MascotaEscaneadaViewHolder, position: Int) {
        val currentMascotaEscaneada = mascotasEscaneadas[position]
        holder.nombreTextView.text = currentMascotaEscaneada.nombre
        holder.fechaTextView.text = currentMascotaEscaneada.fechaEscaneo

        // Log para verificar el objeto currentMascotaEscaneada completo
        Log.d("MascotasEscAdapts", "MascotaEscaneada completa: $currentMascotaEscaneada")

        // Aquí puedes cargar la imagen utilizando alguna biblioteca como Picasso, Glide, etc.
        // Por ejemplo, con Glide:
        Glide.with(holder.itemView).load(currentMascotaEscaneada.imageUrl).into(holder.imagenMascotaImageView)

        // Agregar un clic en el CardView
        holder.itemView.setOnClickListener {
            // Obtener el contexto desde el CardView
            val context = holder.itemView.context

            // Crear un Intent para abrir ContactDetailsActivity
            val intent = Intent(context, ContactDetailsActivity::class.java)

            // Obtener el mascotaId directamente de la lista mascotasEscaneadas
            val mascotaId = currentMascotaEscaneada.mascotaId
            Log.d("MascotasEscAdapt", "ID de la mascota: $mascotaId")
            // Agregar el ID de la mascota como extra
            intent.putExtra("MASCOTA_ID", mascotaId)

            // Iniciar la actividad
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mascotasEscaneadas.size
    }
}