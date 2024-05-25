package com.petscanqr.app

import Mascota
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MascotasFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MascotasFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var mascotasAdapter: MascotaAdapter // Asumiendo que tienes un adaptador llamado MascotasAdapter
    private var listaMascotas = mutableListOf<Mascota>()   // Asumiendo que tienes una lista de mascotas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mascotas, container, false)

        // Inicializa los componentes y configura el RecyclerView.
        inicializarRecyclerView(view)

        // Cargar los datos en el adaptador.
        setupFirestoreListener()
        inicializarFab(view)

        return view
    }

    private fun inicializarRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_mascotas)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inicializa el adaptador con la lista de mascotas.
        mascotasAdapter = MascotaAdapter(listaMascotas, requireContext())
        recyclerView.adapter = mascotasAdapter
    }




    private fun inicializarFab(view: View) {
        val fabAgregarMascota: FloatingActionButton = view.findViewById(R.id.fab_agregar_mascota)
        fabAgregarMascota.setOnClickListener {
            val intent = Intent(requireContext(), AddPetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupFirestoreListener() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        if (userId != null) {
            db.collection("mascotas").whereEqualTo("ownerId", userId)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Escuchando cambios falló", error)
                        return@addSnapshotListener
                    }

                    val mascotasList = mutableListOf<Mascota>()
                    for (doc in snapshots!!) {
                        val mascota = doc.toObject(Mascota::class.java)
                        mascota.id = doc.id
                        mascotasList.add(mascota)
                    }


                    mascotasAdapter.updateData(mascotasList)
                }
        }
    }






    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MascotasFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MascotasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        private const val TAG = "MascotasFragment"
        fun newInstance() = MascotasFragment()
    }
}