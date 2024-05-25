package com.petscanqr.app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MascotasEscaneadasFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MascotasEscaneadasAdapter
    private lateinit var mascotasEscaneadas: MutableList<MascotaEscaneada>
    private lateinit var usuarioActualId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mascotas_escaneadas, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMascotasEscaneadas)
        recyclerView.layoutManager = LinearLayoutManager(context)

        mascotasEscaneadas = mutableListOf()
        adapter = MascotasEscaneadasAdapter(mascotasEscaneadas)
        recyclerView.adapter = adapter

        // Obtén el ID del usuario actual desde Firebase Authentication
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            usuarioActualId = user.uid
            obtenerMascotasEscaneadas()
        } else {
            // El usuario no está autenticado, maneja según tus necesidades
        }

        return view
    }

    private fun obtenerMascotasEscaneadas() {
        val db = FirebaseFirestore.getInstance()

        db.collection("mascotasEscaneadas")
            .whereEqualTo("userId", usuarioActualId)
            .get()
            .addOnSuccessListener { result ->
                mascotasEscaneadas.clear()

                for (document in result) {
                    val mascotaId = document.getString("mascotaId") ?: ""
                    val nombre = document.getString("nombre") ?: ""
                    val fechaEscaneo = document.getString("fechaEscaneo") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val userId = document.getString("userId") ?: ""

                    val mascotaEscaneada = MascotaEscaneada(mascotaId, nombre, fechaEscaneo, imageUrl, userId)
                    mascotasEscaneadas.add(mascotaEscaneada)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Manejar el error al obtener las mascotas escaneadas
                Log.e("MascotasEscFragment", "Error al obtener mascotas escaneadas: $exception")
            }
    }
}