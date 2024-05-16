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
import com.gabrieldavidortizj.dreamhome.property.CreateProperty
import com.gabrieldavidortizj.dreamhome.property.favorite
import com.gabrieldavidortizj.dreamhome.property.properties
import com.gabrieldavidortizj.dreamhome.property.yourproperties
import com.gabrieldavidortizj.dreamhome.user.meProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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
             }
            startActivity(intent)
        }
        addHome.setOnClickListener{
            val intent = Intent(this, CreateProperty::class.java).apply {
                putExtra("email", emailText.text.toString())
                putExtra("provider", providerText.text.toString())
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
        perfil = menu?.findItem(R.id.perfil)!! // Aquí obtienes la referencia al elemento del menú
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
                } else {
                    perfil.isVisible = false
                    misPisos.isVisible = false
                    favoritos.isVisible = false
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
            val intent = Intent(this, meProfile::class.java).apply {
                putExtra("email", emailText.text.toString())
                putExtra("provider", providerText.text.toString())
            }
            startActivity(intent)
            return true
        }  else if (item.itemId == R.id.favoritos) {
            val intent = Intent(this, favorite::class.java).apply {
                putExtra("email", emailText.text.toString())
                putExtra("provider", providerText.text.toString())
            }
            startActivity(intent)
            return true
        }  else if (item.itemId == R.id.misPisos) {
            val intent = Intent(this, yourproperties::class.java).apply {
                putExtra("email", emailText.text.toString())
                putExtra("provider", providerText.text.toString())
            }
            startActivity(intent)
            return true
        }else {
            return super.onOptionsItemSelected(item)
        }
    }

}