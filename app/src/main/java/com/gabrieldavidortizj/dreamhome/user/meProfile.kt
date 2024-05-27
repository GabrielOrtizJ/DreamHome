package com.gabrieldavidortizj.dreamhome.user

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File

class meProfile : AppCompatActivity() {
    private lateinit var config : TextView
    private lateinit var providerText : TextView
    private lateinit var emailText : TextView
    private lateinit var provider : String
    private lateinit var email : String
    private lateinit var nombre : TextView
    private lateinit var direccion : TextView
    private lateinit var telefono : TextView
    private lateinit var userImage: ImageView
    private lateinit var mode : TextView
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
        userImage = findViewById(R.id.imagePerfilme)

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
        db.collection("user").document(email).get().addOnSuccessListener {
            // ...
            val imageUrl = email
            if (imageUrl != null) {
                downloadImageFromFirebase(imageUrl)
            }
        }
    }

    private fun downloadImageFromFirebase(userId: String) {
        val ref = storage.reference.child("images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            val url = uri.toString()
            Picasso.get().load(url).into(userImage)
        }.addOnFailureListener {
            Toast.makeText(this, "Error al descargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setup(email : String,provider:String) {
        title = "Inicio"
        emailText.text = email
        providerText.text = provider

    }
}