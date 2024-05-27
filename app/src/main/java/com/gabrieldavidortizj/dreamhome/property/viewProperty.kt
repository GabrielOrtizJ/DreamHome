package com.gabrieldavidortizj.dreamhome.property

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso


class viewProperty : AppCompatActivity() {
    private lateinit var direccion : TextView
    private lateinit var precio : TextView
    private lateinit var tipo : TextView
    private lateinit var hab : TextView
    private lateinit var ba : TextView
    private lateinit var descripcion : TextView
    private lateinit var nombre : TextView
    private lateinit var id : String
    private val db = FirebaseFirestore.getInstance()
    private val propertiesCollection = db.collection("property")
    private lateinit var storage: FirebaseStorage

    private lateinit var image : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_property)
        direccion = findViewById(R.id.direccionView)
        precio = findViewById(R.id.precioView)
        tipo = findViewById(R.id.tipoView)
        hab = findViewById(R.id.habView)
        ba = findViewById(R.id.baView)
        descripcion = findViewById(R.id.descripcionView)
        nombre = findViewById(R.id.nombreView)
        image = findViewById(R.id.imageVproperty)
        storage = FirebaseStorage.getInstance()

        val bundle = intent.extras
        id = bundle?.getString("DOCUMENT_ID") ?: ""

        propertiesCollection.document(id).get().addOnSuccessListener { document ->
            if (document != null) {
                val property = document.toObject(PropertyData::class.java)
                direccion.text = property?.direccion
                precio.text = property?.precio
                tipo.text = property?.tipo
                hab.text = property?.habitaciones
                ba.text = property?.baños
                descripcion.text = property?.descripcion
                nombre.text = property?.asesorNombreItem
                downloadImageFromFirebase(id)
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }
    private fun downloadImageFromFirebase(propertyId: String) {
        val ref = storage.reference.child("propertyImages/$propertyId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            val url = uri.toString()
            Picasso.get().load(url).into(image)
        }.addOnFailureListener {
            // Puedes manejar el error aquí, por ejemplo, estableciendo una imagen predeterminada
            // image.setImageResource(R.drawable.default_image)
        }
    }

}