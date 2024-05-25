package com.petscanqr.app

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.petscanqr.app.databinding.ActivityAddPetBinding
import android.Manifest
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class AddPetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPetBinding
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    lateinit var uriDeLaImagen: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPetBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initUI()
    }

    private fun initUI(){
        configureSpinners()
        binding.imageView.setOnClickListener {
            verificarPermisoYAbrirGaleria()
        }


        binding.btnGuardar.setOnClickListener { guardarMascotaEnDB() }

    }

    private fun guardarMascotaEnDB() {
        if (validarCampos()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {

                val storageReference = FirebaseStorage.getInstance().reference
                val filePath = storageReference.child("mascotas").child("${UUID.randomUUID()}.jpg")


                if (!::uriDeLaImagen.isInitialized) {
                    Toast.makeText(this, "Por favor, selecciona una imagen para la mascota.", Toast.LENGTH_SHORT).show()
                    return
                }

                val edadMascota = binding.tilEdad.editText?.text.toString().trim().toIntOrNull() ?: 0

                val uploadTask = filePath.putFile(uriDeLaImagen)
                uploadTask.addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { uri ->

                        val mascota = hashMapOf(
                            "ownerId" to userId,
                            "tipo" to binding.spinnerMascota.selectedItem.toString(),
                            "nombre" to binding.tilNombre.editText?.text.toString().trim(),
                            "raza" to binding.tilRaza.editText?.text.toString().trim(),
                            "sexo" to binding.spinnerSexo.selectedItem.toString(),
                            "edad" to edadMascota,
                            "estatus" to "Localizada",
                            "imageUrl" to uri.toString()

                        )

                        db.collection("mascotas")
                            .add(mascota)
                            .addOnSuccessListener {
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al agregar la mascota: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al subir imagen: ${it.message}", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Error al obtener el ID del usuario.", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun configureSpinners() {

        ArrayAdapter.createFromResource(
            this,
            R.array.opciones_tipo_mascota,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerMascota.adapter = adapter
        }


        ArrayAdapter.createFromResource(
            this,
            R.array.opciones_sexo,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSexo.adapter = adapter
        }
    }

    private fun verificarPermisoYAbrirGaleria() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        } else {
            abrirGaleria()
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            uriDeLaImagen = data.data!!
            binding.imageView.setImageURI(uriDeLaImagen)
        }
    }


    private fun mostrarImagenEnImageView(selectedImageUri: Uri?) {
        binding.imageView.setImageURI(selectedImageUri)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
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

    companion object {
        const val REQUEST_STORAGE_PERMISSION = 1001
        const val REQUEST_IMAGE_PICK = 1002
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

        return esValido
    }


    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }





}