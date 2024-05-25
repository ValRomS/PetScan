package com.petscanqr.app

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.petscanqr.app.databinding.ActivityContactDetailsBinding

class ContactDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactDetailsBinding
    private lateinit var mascotaId: String
    private var numeroTelefonoPropietario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        initUI()
    }

    private fun initUI() {
        // Obtener el ID de la mascota de la intención
        mascotaId = intent.getStringExtra("MASCOTA_ID") ?: ""

        if (mascotaId.isNotEmpty()) {
            // Realizar la consulta a Firestore con el ID de la mascota
            cargarDatosMascota()
        } else {
            // Manejar el caso en el que el ID de la mascota esté vacío
            Toast.makeText(this, "ID de mascota no válido", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnMensajes.setOnClickListener {
            // Verificar si se tiene el número de teléfono del propietario
            if (!numeroTelefonoPropietario.isNullOrBlank()) {
                // Obtener el nombre de la mascota
                val nombreMascota = binding.tvNombre.text.toString()

                // Mensaje predefinido con el nombre de la mascota
                val mensaje = getString(R.string.messageChat) + " $nombreMascota"

                // Codificar el mensaje para que sea parte del URI
                val mensajeCodificado = Uri.encode(mensaje)

                // Crear un URI para abrir la conversación de WhatsApp con el mensaje predefinido
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$numeroTelefonoPropietario&text=$mensajeCodificado")

                // Crear un Intent con la acción ACTION_VIEW y el URI de WhatsApp
                val intent = Intent(Intent.ACTION_VIEW, uri)

                // Verificar si hay aplicaciones que pueden manejar este Intent
                if (intent.resolveActivity(packageManager) != null) {
                    // Si hay una aplicación que puede manejar el Intent, iniciarla
                    startActivity(intent)
                } else {
                    // Si no hay aplicación que pueda manejar el Intent, muestra un mensaje o toma otra acción
                    Toast.makeText(this, "No se encontró la aplicación de WhatsApp", Toast.LENGTH_SHORT).show()
                }
            } else {
                // El número de teléfono del propietario no está disponible
                Toast.makeText(this, "Número de teléfono del propietario no disponible", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun cargarDatosMascota() {
        val db = FirebaseFirestore.getInstance()
        val mascotasRef = db.collection("mascotas")

        mascotasRef.document(mascotaId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // El documento de la mascota existe, procesar los datos
                    val nombre = document.getString("nombre") ?: ""
                    val estatus = document.getString("estatus") ?: ""
                    val raza = document.getString("raza") ?: ""
                    val sexo = document.getString("sexo") ?: ""
                    val direccion = document.getString("direccion") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val ownerId = document.getString("ownerId") ?: ""

                    // Actualizar la interfaz con los datos de la mascota
                    //actualizarInterfaz(nombre, estatus, raza, sexo, direccion, imageUrl)
                    obtenerNumeroTelefonoPropietario(ownerId, nombre, estatus, raza, sexo, direccion, imageUrl)
                } else {
                    // El documento de la mascota no existe, manejar según sea necesario
                    Toast.makeText(this, "No se encontraron datos de la mascota", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                // Error al obtener información de Firestore, manejar según sea necesario
                Toast.makeText(this, "Error al cargar datos de la mascota: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }



    private fun actualizarInterfaz(nombre: String, estatus: String, raza: String, sexo: String, direccion: String, imageUrl: String) {
        // Actualizar la interfaz con los datos de la mascota
        // (Aquí debes establecer los datos en los TextView y otros elementos según tu diseño)

        // Cargar la imagen utilizando Glide (asegúrate de agregar la dependencia en tu archivo build.gradle)
        Glide.with(this)
            .load(imageUrl)
            .into(binding.mascotaImageView)

        binding.tvNombre.text = nombre
        binding.tvEstatus.text = estatus
        binding.tvRaza.text = "Raza: $raza"
        binding.tvSexo.text = "Sexo: $sexo"
        binding.tvDireccion.text = "Dirección: $direccion"
    }


    private fun obtenerNumeroTelefonoPropietario(
        ownerId: String,
        nombre: String,
        estatus: String,
        raza: String,
        sexo: String,
        direccion: String,
        imageUrl: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val usuariosRef = db.collection("users")

        usuariosRef.document(ownerId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // El documento del propietario existe, obtener el número de teléfono
                    numeroTelefonoPropietario = document.getString("numeroCelular")
                    Log.d("nume", "completa: $numeroTelefonoPropietario")
                }
                // Continuar con la carga de datos y actualización de interfaz
                actualizarInterfaz(nombre, estatus, raza, sexo, direccion, imageUrl)
            }
            .addOnFailureListener { e ->
                // Error al obtener información de Firestore, manejar según sea necesario
                Toast.makeText(this, "Error al cargar datos de propietario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}