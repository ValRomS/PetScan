package com.petscanqr.app

import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.petscanqr.app.databinding.ActivityHomeBinding
import com.petscanqr.app.databinding.ActivityLoginBinding

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var toogle: ActionBarDrawerToggle
    private lateinit var drawer : DrawerLayout

    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, MascotasFragment.newInstance())
            transaction.commit()
        }
    }

    override fun onResume() {
        super.onResume()

        updateHeaderUI()
    }


    private fun initUI() {

        val toolbar: Toolbar = findViewById(R.id.toolbar_home)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toogle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toogle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        // Inicializa la referencia al NavigationView.
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Llama a la función para mostrar la imagen y el nombre del usuario.
        updateHeaderUI()

        // Agrega un OnClickListener a la imagen del usuario.
        val headerView = navigationView.getHeaderView(0)
        val userImageView = headerView.findViewById<ImageView>(R.id.nav_header_imageView)
        userImageView.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateHeaderUI() {
        val headerView = navigationView.getHeaderView(0)
        val userImageView = headerView.findViewById<ImageView>(R.id.nav_header_imageView)
        val userNameTextView = headerView.findViewById<TextView>(R.id.nav_header_textView)

        // Obtén el usuario actual desde Firebase.
        val user = Firebase.auth.currentUser

        if (user != null) {
            // Inicializa la referencia a Firestore
            val db = FirebaseFirestore.getInstance()

            // Obtén el nombre del usuario directamente desde Firebase Firestore
            val userId = user.uid
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document != null) {
                    val nombre = document.getString("nombre")
                    val imageUrl = document.getString("imageUrl") // Obtén la URL de la imagen desde Firestore

                    // Actualiza la imagen y el nombre del usuario.
                    if (imageUrl != null) {
                        // Carga la imagen del usuario desde la URL de Firestore.
                        Glide.with(this).load(imageUrl).into(userImageView)
                    }

                    if (!nombre.isNullOrBlank()) {
                        userNameTextView.text = nombre
                    } else {
                        // En caso de que el usuario no haya configurado su nombre en Firebase, puedes mostrar un nombre predeterminado.
                        userNameTextView.text = "Nombre de Usuario"
                    }
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.nav_item_cerrarSesion) {
            logOut()
            drawer.closeDrawer(GravityCompat.START)
            return true
        }

        val fragment: Fragment = when (item.itemId){
            R.id.nav_item_mascotas -> MascotasFragment()
            R.id.nav_item_escanear -> EscanearFragment()
            R.id.nav_item_mascotasEscaneadas -> MascotasEscaneadasFragment()
            R.id.nav_item_mascotasPerdidas -> LostPetsFragment()
            R.id.nav_item_configuracion -> ConfigurationFragment()

            else -> MascotasFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toogle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toogle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public fun logOut(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}