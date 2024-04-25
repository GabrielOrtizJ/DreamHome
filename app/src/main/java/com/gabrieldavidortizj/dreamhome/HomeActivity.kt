package com.gabrieldavidortizj.dreamhome

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.remoteConfig
import org.checkerframework.checker.units.qual.A

enum class ProviderType{
    BASIC,
    GOOGLE,
    ANONIMO
}
class HomeActivity : AppCompatActivity() {

    private lateinit var providerText : TextView
    private lateinit var emailText : TextView
    private lateinit var cerrar : MenuItem
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var searchView : SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        providerText = findViewById<TextView>(R.id.providerTextView)
        emailText = findViewById<TextView>(R.id.EmailTextView)
        searchView = findViewById<SearchView>(R.id.searchView)
        firebaseAuth = FirebaseAuth.getInstance()

        //Setup
        val bundle=intent.extras
        val email =bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email?:"",provider?:"")

        //Guardado de datos

        val sharedPreferences = getSharedPreferences(getString(R.string.prefs_file) , Context.MODE_PRIVATE)
        val prefs = sharedPreferences.edit()

        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()

        searchView.setOnClickListener{
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        //remote Config
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val showButton = Firebase.remoteConfig.getBoolean("show_button")
                val ButtonText = Firebase.remoteConfig.getString("button_text")
                if (showButton && provider != ProviderType.ANONIMO.name) {
                    cerrar?.isVisible = true
                    cerrar?.title = ButtonText
                } else {
                    cerrar?.isVisible = false
                }
            }
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
        cerrar =
            menu?.findItem(R.id.perfil)!! // Aquí obtienes la referencia al elemento del menú
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
            val intent = Intent(this, EditProfile::class.java).apply {
                putExtra("email", emailText.text.toString())
                putExtra("provider", providerText.text.toString())
            }
            startActivity(intent)
            return true
          } else {
            return super.onOptionsItemSelected(item)
        }
    }

}