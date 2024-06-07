package com.petscanqr.app


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.petscanqr.app.dto.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.petscanqr.app.dto.response.Mascota


class MascotasFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var mascotasAdapter: MascotaAdapter // Asumiendo que tienes un adaptador llamado MascotasAdapter
    private var listaMascotas = mutableListOf<Mascota>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mascotas, container, false)

        // Inicializa los componentes y configura el RecyclerView.
        inicializarRecyclerView(view)

        // Cargar los datos en el adaptador.
        setupRetrofitListener()
        inicializarFab(view)

        swipeRefreshLayout = view.findViewById(R.id.swipe)
        configSwipe()


        return view
    }

    private fun configSwipe() {
        swipeRefreshLayout.setOnRefreshListener {
           setupRetrofitListener()
        }
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

    private fun setupRetrofitListener() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val call = RetrofitClient.instance.getMascotas(userId)

            call.enqueue(object : retrofit2.Callback<List<Mascota>> {
                override fun onResponse(call: Call<List<Mascota>>, response: Response<List<Mascota>>) {
                    if (response.isSuccessful) {
                        val mascotasList = response.body() ?: emptyList()
                        mascotasAdapter.updateData(mascotasList)
                        Log.e("funcionaretrofit", "success:")
                    } else {
                        Log.e(TAG, "Error en la respuesta: ${response.code()}")
                    }
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun onFailure(call: Call<List<Mascota>>, t: Throwable) {
                    Log.e(TAG, "Error en la solicitud: ${t.message}")
                }
            })
        }
    }








    companion object {

        private const val TAG = "MascotasFragment"
        fun newInstance() = MascotasFragment()
    }
}