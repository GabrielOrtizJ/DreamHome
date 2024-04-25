package com.gabrieldavidortizj.dreamhome.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gabrieldavidortizj.dreamhome.HomeActivity
import com.gabrieldavidortizj.dreamhome.ProviderType
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class start : AppCompatActivity() {
    private lateinit var guest: TextView
    private lateinit var sigIns: TextView
    private lateinit var logIns: TextView
    lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screenSplash =  installSplashScreen()
        setContentView(R.layout.activity_start)
        screenSplash.setKeepOnScreenCondition{false}
        guest = findViewById(R.id.Invitado_button)
        sigIns = findViewById(R.id.Registro_button)
        logIns = findViewById(R.id.Entrar_button)
        firebaseAuth = FirebaseAuth.getInstance()

        guest.setOnClickListener {
            anonymousAuth()
        }
        //Analytics Event
        val analyics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message","Integración de firebase completa")
        analyics.logEvent("InitScreen",bundle)

        //remote Config
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60
        }
        val firebaseConfig = Firebase.remoteConfig
        firebaseConfig.setConfigSettingsAsync(configSettings)
        firebaseConfig.setDefaultsAsync(mapOf("show_button" to false, "button_text" to "forzar error"))

        sigIns.setOnClickListener {
            val intent = Intent(this,signIn::class.java)
            startActivity(intent)
        }

        logIns.setOnClickListener {
            val intent = Intent(this,logIn::class.java)
            startActivity(intent)
        }
    }

    private fun anonymousAuth() {
         firebaseAuth.signInAnonymously().addOnCompleteListener{
             if(it.isSuccessful){
                 showHome(it.result?.user?.email ?: "",ProviderType.ANONIMO)
             }else{
                 showAlert()
             }
         }


    }
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("error")
        builder.setMessage("se ha producido un error autenticando el usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }
    private fun showHome(email: String,provider: ProviderType){

        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homeIntent)

    }
}