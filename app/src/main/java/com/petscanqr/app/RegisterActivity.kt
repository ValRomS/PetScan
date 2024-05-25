package com.petscanqr.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.petscanqr.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        binding.btnRegistrar.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }

        }
    }

    private fun registerUser() {
        val email = binding.tilCorreo.editText?.text.toString().trim()
        val password = binding.tilContrasenia.editText?.text.toString().trim()

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveUserDataToFirestore()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Error al registrar: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun saveUserDataToFirestore() {
        val nombre = binding.tilNombre.editText?.text.toString().trim()
        val apellido = binding.tilApellido.editText?.text.toString().trim()
        val correo = binding.tilCorreo.editText?.text.toString().trim()
        val numeroCelular = binding.tilNumeroCelular.editText?.text.toString().trim()
        val ciudad = binding.tilCiudad.editText?.text.toString().trim()

        val user = firebaseAuth.currentUser
        user?.let {
            val userData = hashMapOf(
                "nombre" to nombre,
                "apellido" to apellido,
                "correo" to correo,
                "numeroCelular" to numeroCelular,
                "ciudad" to ciudad
            )

            db.collection("users").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Log.d(
                        "Firestore",
                        "DocumentSnapshot successfully written!"
                    )
                }
                .addOnFailureListener { e -> Log.w("Firestore", "Error writing document", e) }
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

        if (binding.tilCorreo.editText?.text.isNullOrEmpty()) {
            binding.tilCorreo.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilCorreo.error = null
        }

        // Validación de contraseña
        val password = binding.tilContrasenia.editText?.text.toString()
        if (password.length < 8 || !password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"))) {
            binding.tilContrasenia.error =
                "La contraseña debe tener al menos 8 caracteres, incluyendo mayúsculas, minúsculas y números"
            isValid = false
        } else {
            binding.tilContrasenia.error = null
        }

        // Validación de campos vacíos
        if (binding.tilNumeroCelular.editText?.text.isNullOrEmpty()) {
            binding.tilNumeroCelular.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilNumeroCelular.error = null
        }

        if (binding.tilCiudad.editText?.text.isNullOrEmpty()) {
            binding.tilCiudad.error = "Este campo no puede estar vacío"
            isValid = false
        } else {
            binding.tilCiudad.error = null
        }

        return isValid
    }
}