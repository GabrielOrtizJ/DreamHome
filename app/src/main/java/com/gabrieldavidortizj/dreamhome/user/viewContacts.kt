package com.gabrieldavidortizj.dreamhome.user

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.gabrieldavidortizj.dreamhome.R
import com.gabrieldavidortizj.dreamhome.property.PropertyData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class viewContacts : AppCompatActivity() {
    private lateinit var id : String
    private lateinit var userImage: ImageView
    private lateinit var nombre : TextView
    private lateinit var tlf : TextView
    private lateinit var direccion : TextView
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("user")
    private lateinit var btngmail : TextView
     private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_contacts)

        nombre = findViewById(R.id.nombreContact)
        tlf = findViewById(R.id.telefonoContact)
        direccion = findViewById(R.id.direccionContact)
        btngmail = findViewById(R.id.btnsendgmail)
        val bundle = intent.extras
        id = bundle?.getString("DOCUMENT_ID") ?: ""
        userImage = findViewById(R.id.fotocontac)

        userCollection.document(id).get().addOnSuccessListener { document ->
            if (document != null) {
                val user = document.toObject(user::class.java)
                direccion.text = user?.address
                nombre.text = user?.nombre
                tlf.text = user?.phone

            } else {
                Log.d(ContentValues.TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "get failed with ", exception)
        }

        btngmail.setOnClickListener {
            val intent = Intent(this, mailPersonas::class.java).apply {
                putExtra("email", id)
            }
            startActivity(intent)
        }

        db.collection("user").document(id).get().addOnSuccessListener {
            // ...
            val imageUrl = id
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
}