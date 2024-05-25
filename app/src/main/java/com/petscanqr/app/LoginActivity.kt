package com.petscanqr.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.petscanqr.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickableText()
        initUI()
    }



    private fun initUI() {
        binding.btnLogin.setOnClickListener { logingUser() }
    }

    private fun logingUser() {
        val email = binding.tilCorreo.editText?.text.toString().trim()
        val password = binding.tilContrasenia.editText?.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, HomeActivity::class.java)
                     startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al iniciar sesión: Correo o Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Por favor, ingrese un correo y contraseña", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initClickableText() {
        val styledTex = createStyledTextWithLink()
        binding.tvRegistrarPrompt.text = styledTex
        binding.tvRegistrarPrompt.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun createStyledTextWithLink(): SpannableString {
        val fullString = "${getString(R.string.registrateAqui)}"
        val spannableString = SpannableString(fullString)

        val clikableSpan = object : ClickableSpan(){
            override fun onClick(widget: View) {
                navigateToRegisterActivity()
            }
        }

        val start = fullString.indexOf("Registrate aquí")
        if(start != -1) {
            spannableString.setSpan(clikableSpan, start, start + "Registrate aquí".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannableString
    }

    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }


}