package com.petscanqr.app

import Mascota
import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.petscanqr.app.databinding.ActivityEditPetBinding

class EditPetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPetBinding
    private var mascota: Mascota? = null
    lateinit var uriDeLaImagen: Uri
    var mascotaId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPetBinding.inflate(layoutInflater)
        setContentView(binding.root)


        unitIU()
    }

    private fun unitIU() {

        val adapterMascota = ArrayAdapter.createFromResource(this, com.petscanqr.app.R.array.opciones_tipo_mascota, R.layout.simple_spinner_item)
        adapterMascota.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerMascota.adapter = adapterMascota


        val adapterSexo = ArrayAdapter.createFromResource(this, com.petscanqr.app.R.array.opciones_sexo, R.layout.simple_spinner_item)
        adapterSexo.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerSexo.adapter = adapterSexo


        mascota?.let {
            mostrarDatosMascota(it)
        }

        binding.imageView.setOnClickListener {
            verificarPermisoYAbrirGaleria()
        }

        binding.btnGuardar.setOnClickListener { actualizarMascotaEnDB() }

        mascotaId = intent.getStringExtra("MASCOTA_ID")

        if (mascotaId != null) {
            fetchMascotaDetailsFromFirestore(mascotaId!!)
        }

    }

    private fun fetchMascotaDetailsFromFirestore(mascotaId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("mascotas").document(mascotaId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    mascota = documentSnapshot.toObject(Mascota::class.java)
                    mascota?.let {
                        mostrarDatosMascota(it)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditPetActivity", "Error fetching pet details: ", e)
            }
    }

    private fun actualizarMascotaEnDB() {
        if (validarCampos() && !mascotaId.isNullOrEmpty()) {
            // Verifica si uriDeLaImagen ha sido inicializado
            if (::uriDeLaImagen.isInitialized) {
                mascota?.imageUrl?.let { eliminarImagenDeFirebase(it) }
                subirImagenAFirebase { uri ->
                    guardarDatosEnFirestore(uri)
                }
            } else {
                guardarDatosEnFirestore(mascota?.imageUrl)
            }
        } else if (mascotaId.isNullOrEmpty()) {
            Toast.makeText(this@EditPetActivity, "ID de la mascota inválido", Toast.LENGTH_LONG).show()
        }
    }

    private fun eliminarImagenDeFirebase(urlDeImagenAnterior: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(urlDeImagenAnterior)
        storageRef.delete().addOnSuccessListener {
            Log.d("DeleteImage", "Imagen anterior eliminada con éxito.")
        }.addOnFailureListener {
            Log.e("DeleteImage", "Error al eliminar la imagen: ${it.message}")
        }
    }

    private fun guardarDatosEnFirestore(imageUrl: String?) {
        val mascotaMap = mutableMapOf<String, Any>()

        val nombre = binding.tilNombre.editText?.text.toString()
        val raza = binding.tilRaza.editText?.text.toString()
        val edad = binding.tilEdad.editText?.text.toString().toIntOrNull() ?: 0
        val nota = binding.tilNota.editText?.text.toString()
        val tipo = binding.spinnerMascota.selectedItem.toString()
        val sexo = binding.spinnerSexo.selectedItem.toString()

        mascotaMap["nombre"] = nombre
        mascotaMap["raza"] = raza
        mascotaMap["edad"] = edad
        mascotaMap["nota"] = nota
        mascotaMap["tipo"] = tipo
        mascotaMap["sexo"] = sexo
        imageUrl?.let { mascotaMap["imageUrl"] = it }

        val db = FirebaseFirestore.getInstance()
        db.collection("mascotas").document(mascotaId!!)
            .set(mascotaMap, SetOptions.merge())
            .addOnSuccessListener {
                val intent = Intent(this, MascotaDetailActivity::class.java)
                intent.putExtra("MASCOTA_ID", mascotaId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                val errorMsg = "Error al actualizar: ${e.message}"
                Toast.makeText(this@EditPetActivity, errorMsg, Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarDatosMascota(mascota: Mascota) {
        // Imagen
        Glide.with(this)
            .load(mascota.imageUrl)
            .into(binding.imageView)

        // Tipo de mascota
        binding.spinnerMascota.setSelection(getSpinnerIndex(binding.spinnerMascota, mascota.tipo))

        // Nombre
        binding.tilNombre.editText?.setText(mascota.nombre)

        // Raza
        binding.tilRaza.editText?.setText(mascota.raza)

        // Sexo
        binding.spinnerSexo.setSelection(getSpinnerIndex(binding.spinnerSexo, mascota.sexo))

        // Edad
        binding.tilEdad.editText?.setText(mascota.edad.toString())

        // Nota
        binding.tilNota.editText?.setText(mascota.nota)
    }

    private fun getSpinnerIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == myString) {
                return i
            }
        }
        return 0
    }

    private fun getLineCount(text: String): Int {
        return text.count { it == '\n' } + 1
    }

    private fun verificarPermisoYAbrirGaleria() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                AddPetActivity.REQUEST_STORAGE_PERMISSION
            )
        } else {
            abrirGaleria()
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, AddPetActivity.REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AddPetActivity.REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            uriDeLaImagen = data.data!!
            binding.imageView.setImageURI(uriDeLaImagen)
        }

    }

    private fun subirImagenAFirebase(callback: (imageUrl: String?) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().getReference("mascotas/$mascotaId")
        storageReference.putFile(uriDeLaImagen)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageReference.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Log.d("UploadImage", "URL de la imagen: ${downloadUri.toString()}")
                    callback(downloadUri.toString())
                } else {
                    Toast.makeText(this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            AddPetActivity.REQUEST_STORAGE_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    abrirGaleria()
                } else {
                    // Mostrar un mensaje al usuario sobre la importancia del permiso
                }
                return
            }
            else -> {
                // Ignorar todos los otros casos de solicitud
            }
        }
    }

    private fun validarCampos(): Boolean {
        var esValido = true

        // Validar nombre de mascota
        val nombreMascota = binding.tilNombre.editText?.text.toString().trim()
        if (nombreMascota.isEmpty()) {
            binding.tilNombre.error = "Ingresa el nombre de la mascota."
            esValido = false
        } else {
            binding.tilNombre.error = null
        }

        // Validar raza
        val razaMascota = binding.tilRaza.editText?.text.toString().trim()
        if (razaMascota.isEmpty()) {
            binding.tilRaza.error = "Ingresa la raza de la mascota."
            esValido = false
        } else {
            binding.tilRaza.error = null
        }

        // Validar edad
        val edadMascota = binding.tilEdad.editText?.text.toString().trim()
        if (edadMascota.isEmpty()) {
            binding.tilEdad.error = "Ingresa la edad de la mascota."
            esValido = false
        } else {
            binding.tilEdad.error = null
        }

        // Validar Nota
        val nota = binding.tilNota.editText?.text.toString().trim()
        if (nota.isEmpty()) {
            binding.tilNota.error = "Ingresa una nota."
            esValido = false
        } else if (getLineCount(nota) > 5) {  // Validar máximo de 5 líneas
            binding.tilNota.error = "La nota no debe tener más de 5 líneas."
            esValido = false
        } else {
            binding.tilNota.error = null
        }

        return esValido
    }



}