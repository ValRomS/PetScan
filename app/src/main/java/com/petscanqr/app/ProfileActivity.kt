package com.petscanqr.app

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.petscanqr.app.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        val user = firebaseAuth.currentUser

        if (user != null) {
            val userId = user.uid
            val userDocument = db.collection("users").document(userId)

            userDocument.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    displayUserData(document)
                    val imageUrl = document.getString("imageUrl")
                    if (imageUrl != null) {
                        loadUserImage(imageUrl)
                    } else {
                        // Log para depuración
                    }
                }
            }
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            startActivityForResult(intent, REQUEST_EDIT_PROFILE)
        }
    }

    private fun displayUserData(document: DocumentSnapshot) {
        val nombre = document.getString("nombre")
        val apellido = document.getString("apellido")
        val edad = document.getString("edad")
        val sexo = document.getString("sexo")
        val ciudad = document.getString("ciudad")
        val email = firebaseAuth.currentUser?.email
        val numeroCelular = document.getString("numeroCelular")

        val nombreCompleto = "$nombre $apellido"
        updateTextView(binding.tvNombre, nombreCompleto)
        updateTextView(binding.tvEdad, edad)
        updateTextView(binding.tvSexo, sexo)
        updateTextView(binding.tvCiudad, ciudad)
        updateTextView(binding.tvEmail, email)
        updateTextView(binding.tvNumero, numeroCelular)
    }

    private fun updateTextView(textView: TextView, value: String?) {
        if (!value.isNullOrBlank()) {
            textView.text = value
        }
    }

    private fun cargarDatosDelUsuario() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val userId = user.uid
            val userDocument = db.collection("users").document(userId)

            userDocument.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    displayUserData(document)
                    val imageUrl = document.getString("imageUrl")
                    if (imageUrl != null) {
                        loadUserImage(imageUrl)
                    } else {
                        // Log para depuración
                    }
                }
            }
        }
    }

    private fun loadUserImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(binding.imageView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            // Llama a cargarDatosDelUsuario() para actualizar los datos
            cargarDatosDelUsuario()
        }
    }

    companion object {
        const val REQUEST_EDIT_PROFILE = 1
    }
}
