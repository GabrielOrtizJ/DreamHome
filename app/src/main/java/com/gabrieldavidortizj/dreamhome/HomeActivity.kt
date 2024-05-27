package com.gabrieldavidortizj.dreamhome

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible

import com.gabrieldavidortizj.dreamhome.property.CreateProperty
import com.gabrieldavidortizj.dreamhome.property.favorite
import com.gabrieldavidortizj.dreamhome.property.properties
import com.gabrieldavidortizj.dreamhome.property.yourproperties
import com.gabrieldavidortizj.dreamhome.user.Contacts
import com.gabrieldavidortizj.dreamhome.user.EditProfile
import com.gabrieldavidortizj.dreamhome.user.mailPersonas
import com.gabrieldavidortizj.dreamhome.user.meProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.remoteConfig


enum class ProviderType{
    BASIC,
    GOOGLE,
    ANONIMO
}
class HomeActivity : AppCompatActivity() {

    private lateinit var providerText : TextView
    private lateinit var emailText : TextView
    private lateinit var perfil : MenuItem
    private lateinit var misPisos : MenuItem
    private lateinit var favoritos : MenuItem
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var searchView : SearchView
    private lateinit var provider : String
    private lateinit var email : String
    private lateinit var addHome : TextView
    private lateinit var contacts : TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        providerText = findViewById<TextView>(R.id.providerTextView)
        emailText = findViewById<TextView>(R.id.EmailTextView)
        searchView = findViewById<SearchView>(R.id.searchView)
        firebaseAuth = FirebaseAuth.getInstance()
        addHome = findViewById(R.id.btnAddHome)
         contacts = findViewById(R.id.Buscar_contactos_button)
        db = FirebaseFirestore.getInstance()

        //Setup
        val bundle=intent.extras
        email = bundle?.getString("email") ?: ""
        provider = bundle?.getString("provider") ?: ""
        setup(email?:"",provider?:"")

        //Guardado de datos

        val sharedPreferences = getSharedPreferences(getString(R.string.prefs_file) , Context.MODE_PRIVATE)
        val prefs = sharedPreferences.edit()

        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()

        searchView.setOnClickListener{
            val intent = Intent(this, properties::class.java).apply {
                putExtra("email", emailText.text.toString())
                putExtra("provider", providerText.text.toString())
            }
            startActivity(intent)
        }
        addHome.setOnClickListener{
            checkEmailAndStartActivity(CreateProperty::class.java)
        }

        contacts.setOnClickListener{
            val intent = Intent(this, Contacts::class.java).apply {
                putExtra("email", emailText.text.toString())
            }
            startActivity(intent)
        }


    }
    private fun setup(email : String,provider:String) {
        title = "Inicio"
        emailText.text = email
        providerText.text = provider

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menuhome, menu)
        perfil = menu?.findItem(R.id.perfil)!!
        misPisos = menu?.findItem(R.id.misPisos)!!
        favoritos = menu?.findItem(R.id.favoritos)!!
        //remote Config
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val showButton = Firebase.remoteConfig.getBoolean("show_button")
                val ButtonText = Firebase.remoteConfig.getString("button_text")
                if (showButton && provider != ProviderType.ANONIMO.name) {
                    perfil.isVisible = true
                    misPisos.isVisible = true
                    favoritos.isVisible = true
                    perfil.title = ButtonText
                    addHome.isVisible = true
                } else {
                    perfil.isVisible = false
                    misPisos.isVisible = false
                    favoritos.isVisible = false
                    addHome.isVisible = false
                }
            }
        }

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.salir) {
            finishAffinity()
            return true



        } else if (item.itemId == R.id.cerrar_sesion) {
            //limpiar preferences
            val sharedPreferences = getSharedPreferences(getString(R.string.prefs_file) ,Context.MODE_PRIVATE)
            val prefs = sharedPreferences.edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            finish()
            return true
        } else if (item.itemId == R.id.perfil) {
            checkEmailAndStartActivity(meProfile::class.java)

            return true
        }  else if (item.itemId == R.id.favoritos) {
            checkEmailAndStartActivity(favorite::class.java)

            return true
        }  else if (item.itemId == R.id.misPisos) {
            checkEmailAndStartActivity(yourproperties::class.java)

            return true
        } else if (item.itemId == R.id.gmailP) {
            val intent = Intent(this, mailPersonas::class.java).apply {
                putExtra("email", emailText.text.toString())
             }
            startActivity(intent)
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
    private fun checkEmailAndStartActivity(activityClass: Class<*>) {
        val email = emailText.text.toString()
        db.collection("user").document(email).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val intent = Intent(this, activityClass).apply {
                    putExtra("email", email)
                    putExtra("provider", providerText.text.toString())
                }
                startActivity(intent)
            } else {
                showAlertDialog()
            }
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Identificación requerida")
            .setMessage("Para usar esta función, debes identificarte.")
            .setPositiveButton("Identificarse") { _, _ ->
                val intent = Intent(this, EditProfile::class.java).apply {
                    putExtra("email", emailText.text.toString())
                    putExtra("provider", providerText.text.toString())
                }
                startActivity(intent)
            }
            .setNegativeButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}