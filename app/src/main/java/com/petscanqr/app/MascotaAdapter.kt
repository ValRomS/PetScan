package com.petscanqr.app

import Mascota
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MascotaAdapter(private val mascotas: MutableList<com.petscanqr.app.dto.response.Mascota>, private val context: Context) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.mascota_item_cardview, parent, false)
        return MascotaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mascotas.size
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = mascotas[position]
        holder.mascotaNombre.text = mascota.nombre

        Glide.with(context)
            .load(mascota.imageUrl)
            .centerCrop()
            .into(holder.mascotaImagen)
    }

    inner class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mascotaImagen: ImageView = itemView.findViewById(R.id.mascota_image)
        val mascotaNombre: TextView = itemView.findViewById(R.id.mascota_nombre)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val mascotaSeleccionada = mascotas[position]
                    val intent = Intent(context, MascotaDetailActivity::class.java)
                    intent.putExtra("MASCOTA_ID", mascotaSeleccionada.id)
                    context.startActivity(intent)
                }
            }
        }
    }

    fun updateData(newData: List<com.petscanqr.app.dto.response.Mascota>) {
        this.mascotas.clear()
        this.mascotas.addAll(newData)
        notifyDataSetChanged()
    }


}
