package com.petscanqr.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.petscanqr.app.AddPetActivity.Companion.REQUEST_IMAGE_PICK
import com.petscanqr.app.AddPetActivity.Companion.REQUEST_STORAGE_PERMISSION
import com.petscanqr.app.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    lateinit var uriDeLaImagen: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {

        configureSpinners()
        mostrarDatosDelUsuario()

        binding.flImagen.setOnClickListener {
            if (!::uriDeLaImagen.isInitialized) {
                // Si la imagen no se ha seleccionado previamente, abre la galería
                verificarPermisoYAbrirGaleria()
            } else {
                // La imagen ya se ha seleccionado previamente, muestra un Toast o un mensaje al usuario
                Toast.makeText(this, "Ya has seleccionado una imagen. Si deseas cambiarla, selecciona una nueva imagen.", Toast.LENGTH_SHORT).show()
            }
        }


        binding.btnEdit.setOnClickListener {
            guardarDatosDelUsuario()
        }
    }

    private fun mostrarDatosDelUsuario() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userData = documentSnapshot.data

                        // Asumiendo que userData es un Map que contiene los datos del usuario
                        if (userData != null) {
                            val nombre = userData["nombre"] as? String ?: ""
                            val apellido = userData["apellido"] as? String ?: ""
                            val email = userData["correo"] as? String ?: ""
                            val numeroCelular = userData["numeroCelular"] as? String ?: ""
                            val ciudad = userData["ciudad"] as? String ?: ""
                            val edad = userData["edad"] as? String ?: ""
                            val imageUrl = userData["imageUrl"] as? String ?: ""

                            // Cargar y mostrar la imagen en el ImageView
                            Glide.with(this)
                                .load(imageUrl) // Reemplaza imageUrl con la URL de la imagen
                                .into(binding.imageView)

                            // Resto del código para llenar los campos de texto
                            binding.tilNombre.editText?.setText(nombre)
                            binding.tilApellido.editText?.setText(apellido)
                            binding.tilEdad.editText?.setText(edad)
                            binding.tilEmail.editText?.setText(email)
                            binding.tilNumeroCelular.editText?.setText(numeroCelular)
                            binding.tilCiudad.editText?.setText(ciudad)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Manejar errores, si es necesario
                }
        }
    }

    private fun guardarDatosDelUsuario() {
        if (validateInputs()) {
            val userId = Firebase.auth.currentUser?.uid

            if (userId != null) {
                // Crear un mapa con los datos del usuario
                val userData = hashMapOf(
                    "nombre" to binding.tilNombre.editText?.text.toString(),
                    "apellido" to binding.tilApellido.editText?.text.toString(),
                    "correo" to binding.tilEmail.editText?.text.toString(),
                    "numeroCelular" to binding.tilNumeroCelular.editText?.text.toString(),
                    "ciudad" to binding.tilCiudad.editText?.text.toString(),
                    "edad" to binding.tilEdad.editText?.text.toString(),
                    "sexo" to binding.spinnerSexo.selectedItem.toString()
                )

                // Verificar si la imagen se ha seleccionado
                if (::uriDeLaImagen.isInitialized) {
                    // Si la imagen se ha seleccionado, subirla a Firebase Storage y actualizar la URL en Firestore
                    subirImagenAFirebase { imageUrl ->
                        userData["imageUrl"] = imageUrl ?: "" // Agregar la URL de la imagen a los datos del usuario, o un valor predeterminado si es nulo

                        // Actualizar los datos del usuario en Firestore
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userId)
                            .set(userData, SetOptions.merge())
                            .addOnSuccessListener {
                                // Los datos del usuario se guardaron con éxito
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Manejar errores, si es necesario
                                Toast.makeText(this, "Error al guardar los datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Si no se ha seleccionado una imagen, actualizar los demás datos directamente en Firestore
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userId)
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener {
                            // Los datos del usuario se guardaron con éxito
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            // Manejar errores, si es necesario
                            Toast.makeText(this, "Error al guardar los datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }


    private fun subirImagenAFirebase(callback: (imageUrl: String?) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().getReference("users/${Firebase.auth.currentUser?.uid}")
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
                    callback(downloadUri.toString())
                } else {
                    Toast.makeText(this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show()
                }
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


    private fun validateInputs(): Boolean {
        var isValid = true

        // Validación de campos vacíos
        if (binding.tilNombre.editText?.text.isNullOrEmpty()) {
            binding.tilNombre.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        if (binding.tilApellido.editText?.text.isNullOrEmpty()) {
            binding.tilApellido.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilApellido.error = null
        }

        if (binding.tilEdad.editText?.text.isNullOrEmpty()) {
            binding.tilEdad.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilEdad.error = null
        }

        if (binding.tilCiudad.editText?.text.isNullOrEmpty()) {
            binding.tilCiudad.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilCiudad.error = null
        }

        if (binding.tilEmail.editText?.text.isNullOrEmpty()) {
            binding.tilEmail.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (binding.tilNumeroCelular.editText?.text.isNullOrEmpty()) {
            binding.tilNumeroCelular.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilNumeroCelular.error = null
        }

        return isValid
    }

    private fun configureSpinners() {

        ArrayAdapter.createFromResource(
            this,
            R.array.opciones_sexo_usuario,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSexo.adapter = adapter
        }
    }


}