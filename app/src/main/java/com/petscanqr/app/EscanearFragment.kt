package com.petscanqr.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.text.SimpleDateFormat
import java.util.Date

class EscanearFragment : Fragment() {
    private lateinit var usuarioActualId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_escanear, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtén el ID del usuario actual desde Firebase Authentication
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Inicializa la referencia a Firestore
            val db = FirebaseFirestore.getInstance()

            // Obtén el ID del usuario directamente desde Firebase Firestore
            usuarioActualId = user.uid

            iniciarEscaneo(view)

            view.findViewById<MaterialButton>(R.id.btnEscanear).setOnClickListener {
                iniciarEscaneo(view)
            }
        } else {
            // El usuario no está autenticado, maneja según tus necesidades
        }
    }

    // Maneja el resultado del escáner de QR
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null && result.contents != null) {
            // Aquí result.contents contiene el texto del código QR escaneado

            // Consulta Firestore para obtener la información de la mascota escaneada
            val db = FirebaseFirestore.getInstance()
            val mascotasRef = db.collection("mascotas")

            mascotasRef
                .document(result.contents)  // Utiliza el ID del documento directamente
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val mascotaId = result.contents  // Asigna el valor del ID del documento
                        val nombre = document.getString("nombre") ?: ""
                        val fechaEscaneo = obtenerFechaActual()
                        val imageUrl = document.getString("imageUrl") ?: ""

                        val ownerId = document.getString("ownerId")

                        if (ownerId == usuarioActualId) {
                            // La mascota pertenece al usuario actual, redirige a MascotaDetailActivity
                            val intent = Intent(context, MascotaDetailActivity::class.java)
                            intent.putExtra("MASCOTA_ID", mascotaId)
                            startActivity(intent)
                        } else {
                            // La mascota no pertenece al usuario actual, redirige a ContactDetailsActivity
                            val intent = Intent(context, ContactDetailsActivity::class.java)
                            intent.putExtra("MASCOTA_ID", mascotaId)


                            // Agregar la mascota escaneada si es necesario
                            agregarMascotaEscaneada(mascotaId, nombre, imageUrl)

                            startActivity(intent)
                        }
                    } else {
                        // El documento de la mascota no existe, maneja según tus necesidades
                    }
                }
                .addOnFailureListener {
                    // Error al obtener información de Firestore, maneja según tus necesidades
                }
        } else {
            // Escaneo cancelado o fallido
            // Puedes manejar esto según tus necesidades
        }
    }*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null && result.contents != null) {
            // Aquí result.contents contiene el texto del código QR escaneado
            val scannedUrl = result.contents

            // Analiza la URL para obtener el ID de la mascota
            val uri = Uri.parse(scannedUrl)
            val mascotaId = uri.getQueryParameter("id") ?: ""

            if (mascotaId.isNotEmpty()) {
                // Consulta Firestore para obtener la información de la mascota escaneada
                val db = FirebaseFirestore.getInstance()
                val mascotasRef = db.collection("mascotas")

                mascotasRef
                    .document(mascotaId)  // Utiliza el ID del documento directamente
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val nombre = document.getString("nombre") ?: ""
                            val fechaEscaneo = obtenerFechaActual()
                            val imageUrl = document.getString("imageUrl") ?: ""

                            val ownerId = document.getString("ownerId")

                            if (ownerId == usuarioActualId) {
                                // La mascota pertenece al usuario actual, redirige a MascotaDetailActivity
                                val intent = Intent(context, MascotaDetailActivity::class.java)
                                intent.putExtra("MASCOTA_ID", mascotaId)
                                startActivity(intent)
                            } else {
                                // La mascota no pertenece al usuario actual, redirige a ContactDetailsActivity
                                val intent = Intent(context, ContactDetailsActivity::class.java)
                                intent.putExtra("MASCOTA_ID", mascotaId)

                                // Agregar la mascota escaneada si es necesario
                                agregarMascotaEscaneada(mascotaId, nombre, imageUrl)

                                startActivity(intent)
                            }
                        } else {
                            // El documento de la mascota no existe, maneja según tus necesidades
                        }
                    }
                    .addOnFailureListener {
                        // Error al obtener información de Firestore, maneja según tus necesidades
                    }
            } else {
                // El ID de la mascota no es válido, maneja según tus necesidades
            }
        } else {
            // Escaneo cancelado o fallido
            // Puedes manejar esto según tus necesidades
        }
    }


    fun iniciarEscaneo(view: View) {
        // Inicia el escáner de QR
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea el código QR")
        integrator.setCameraId(0)  // Use la cámara posterior por defecto
        integrator.setBeepEnabled(false)  // Deshabilita el sonido al escanear
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    private fun agregarMascotaEscaneada(mascotaId: String, nombreMascota: String?, imageUrl: String?) {
        // Agregar la mascota escaneada a la colección mascotasEscaneadas si es necesario
        if (mascotaId.isNotEmpty() && nombreMascota != null) {
            val mascotaEscaneada = MascotaEscaneada(mascotaId, nombreMascota, obtenerFechaActual(), imageUrl ?: "", usuarioActualId)
            Log.d("EscanearFragment", "Valor de mascotaId obtenidosss: $mascotaId")
            val db = FirebaseFirestore.getInstance()
            db.collection("mascotasEscaneadas").add(mascotaEscaneada)
        }
    }

    private fun obtenerFechaActual(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date()
        return dateFormat.format(date)
    }

}