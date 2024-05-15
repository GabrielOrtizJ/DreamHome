package com.gabrieldavidortizj.dreamhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class meProfile : AppCompatActivity() {
    private lateinit var config : TextView
    private lateinit var providerText : TextView
    private lateinit var emailText : TextView
    private lateinit var provider : String
    private lateinit var email : String
    private lateinit var nombre : TextView
    private lateinit var direccion : TextView
    private lateinit var telefono : TextView
    private lateinit var mode : TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me_profile)

        config = findViewById(R.id.btnConfigProfileme)
        providerText = findViewById<TextView>(R.id.providertextViewme)
        emailText = findViewById<TextView>(R.id.emailtextViewme)
        nombre = findViewById(R.id.nameTextTextme)
        direccion =  findViewById(R.id.addressTextTextme)
        telefono = findViewById(R.id.phoneTextTextme)
        mode = findViewById(R.id.modeTextme)

        val bundle=intent.extras
        email = bundle?.getString("email") ?: ""
        provider = bundle?.getString("provider") ?: ""
        setup(email?:"",provider?:"")
        var tipo :String?

        db.collection("user").document(email).get().addOnSuccessListener {
            direccion.setText(it.get("address") as? String ?: "")
            telefono.setText(it.get("phone") as? String ?: "")
            nombre.setText(it.get("nombre") as? String ?: "")
            mode.setText(it.get("tipo") as? String ?: "")

        }

        config.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java).apply {
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
}