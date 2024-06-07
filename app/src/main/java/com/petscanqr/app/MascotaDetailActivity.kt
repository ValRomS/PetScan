package com.petscanqr.app



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.petscanqr.app.databinding.ActivityMascotaDetailBinding
import com.petscanqr.app.dto.response.Mascota
import com.petscanqr.app.dto.service.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MascotaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMascotaDetailBinding
    var mascota: Mascota? = null

    var mascotaId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMascotaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        unitUI()
        Log.d("MascotaDetailActivitiy", "ID de la mascota: $mascotaId")
    }

    private fun unitUI() {

        binding.icOpciones.setOnClickListener {
            showPopupMenu(it)
        }

        mascotaId = intent.getStringExtra("MASCOTA_ID")

        if (mascotaId != null) {
            fetchMascotaDetailsFromFirestore(mascotaId!!)
        }

        // Configura un Listener para el Switch
        binding.switchEstatus.setOnCheckedChangeListener { _, isChecked ->
            // Actualiza el texto del TextView en función del estado del Switch
            val estatusText = if (isChecked) getString(R.string.estatus_localizada) else getString(R.string.estatus_desaparecida)
            binding.tvEstatus.text = estatusText

            // Actualiza el estado de la mascota en la base de datos
            val nuevoEstatus = if (isChecked) getString(R.string.estatus_localizada) else getString(R.string.estatus_desaparecida)
            actualizarEstatusMascota(nuevoEstatus)
        }

    }

    private fun fetchMascotaDetailsFromFirestore(mascotaId: String) {
        val db = FirebaseFirestore.getInstance()
        Log.i("MsDetailActivitybefore", "Consultando Firestore con ID: $mascotaId")
        db.collection("mascotas").document(mascotaId)
            .addSnapshotListener(this) { documentSnapshot, error ->
                if (error != null) {
                    Log.e("MascotaDetailActivity", "Error fetching pet details: ", error)
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    mascota = documentSnapshot.toObject(Mascota::class.java)  // Actualizar la variable global 'mascota'
                    mascota?.let {
                        mostrarDatosMascota(it)
                        actualizarEstadoInicialSwitch(it.estatus)
                    }
                }
            }
    }




    private fun mostrarDatosMascota(mascota: Mascota) {
        Glide.with(this@MascotaDetailActivity)
            .load(mascota.imageUrl)
            .signature(ObjectKey(System.currentTimeMillis())) // Forzar la actualización de la imagen
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.mascotaImageView)

        binding.tvNombre.text = mascota.nombre
        binding.tvNota.text = mascota.nota
        binding.tvRaza.text = "Raza: ${mascota.raza}"
        binding.tvSexo.text = "Sexo: ${mascota.sexo}"
        binding.tvDireccion.text = "Dirección: ${mascota.direccion}"


    }

    private fun actualizarEstadoInicialSwitch(estado: String) {
        // Configura el estado inicial del Switch y el TextView
        val isChecked = estado == getString(R.string.estatus_localizada)
        binding.switchEstatus.isChecked = isChecked
        val estatusText = if (isChecked) getString(R.string.estatus_localizada) else getString(R.string.estatus_desaparecida)
        binding.tvEstatus.text = estatusText
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.popup_menu)

        try {
            val field = popupMenu.javaClass.getDeclaredField("mPopup")
            field.isAccessible = true
            val menuPopupHelper = field.get(popupMenu)
            menuPopupHelper.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menuPopupHelper, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    val intent = Intent(this@MascotaDetailActivity, EditPetActivity::class.java)
                    intent.putExtra("MASCOTA_ID", mascotaId)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_delete -> {
                    mostrarDialogoConfirmacion()

                    true
                }
                R.id.menu_generateQR -> {
                    val intent = Intent(this@MascotaDetailActivity, QRCodeActivity::class.java)
                    intent.putExtra("MASCOTA_ID", mascotaId) // Agrega el ID de la mascota al intent
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun mostrarDialogoConfirmacion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que quieres eliminar a esta mascota?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            eliminarRegistroMascota()
        }
        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }

    /*private fun eliminarMascotaYImagen() {
        Log.d("DeleteProcess", "Iniciando proceso de eliminación...")
        mascota?.imageUrl?.let {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(it)
            storageRef.delete().addOnSuccessListener {
                Log.d("DeleteImage", "Imagen eliminada con éxito.")
                eliminarRegistroMascota()
            }.addOnFailureListener {
                Log.e("DeleteImage", "Error al eliminar la imagen: ${it.message}")
            }
        } ?: Log.d("DeleteProcess", "imageUrl es null.")
    }*/

   /* private fun eliminarRegistroMascota() {
        Log.d("DeleteRecord", "Iniciando eliminación del registro...")
        val db = FirebaseFirestore.getInstance()
        mascotaId?.let {
            db.collection("mascotas").document(it).delete().addOnSuccessListener {
                Log.d("DeleteRecord", "Registro de mascota eliminado con éxito.")
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Log.e("DeleteRecord", "Error al eliminar el registro de la mascota: ${it.message}")
            }
        } ?: Log.d("DeleteRecord", "mascotaId es null.")
    }*/

    private fun eliminarRegistroMascota() {
        Log.d("DeleteRecord", "Iniciando eliminación del registro...")
        mascotaId?.let {
            Log.d("deletePet", "metodo {$mascotaId}")
            val call = RetrofitClient.instance.eliminarMascota(it)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("DeleteRecord", "Registro de mascota eliminado con éxito.")
                        val intent = Intent(this@MascotaDetailActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("DeleteRecord", "Error al eliminar el registro de la mascota: ${response.code()}")
                        Toast.makeText(this@MascotaDetailActivity, "Error al eliminar la mascota: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("DeleteRecord", "Error al eliminar el registro de la mascota: ${t.message}", t)
                    Toast.makeText(this@MascotaDetailActivity, "Error al eliminar la mascota: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: Log.d("DeleteRecord", "mascotaId es null.")
    }

    private fun actualizarEstatusMascota(nuevoEstatus: String) {
        if (mascotaId != null) {
            // Accede a la base de datos y actualiza el estado de la mascota
            val db = FirebaseFirestore.getInstance()
            db.collection("mascotas").document(mascotaId!!)
                .update("estatus", nuevoEstatus)
                .addOnSuccessListener {
                    Log.d("UpdateEstatus", "Estado actualizado en la base de datos: $nuevoEstatus")
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateEstatus", "Error al actualizar el estado en la base de datos: ${e.message}")
                }
        }
    }




}