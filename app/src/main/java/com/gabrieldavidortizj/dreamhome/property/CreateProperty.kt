package com.gabrieldavidortizj.dreamhome.property

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.firestore.FirebaseFirestore

class CreateProperty : AppCompatActivity() {
    private lateinit var providerText : TextView
    private lateinit var emailText : TextView
    private lateinit var provider : String
    private lateinit var email : String
    private val db = FirebaseFirestore.getInstance()
    private lateinit var descripcion : EditText
    private lateinit var direccion : EditText
    private lateinit var habitacion : EditText
    private lateinit var ba  : EditText
    private lateinit var nombre : TextView
    private lateinit var btnAdd : TextView
    private lateinit var precio : EditText
    private lateinit var venta : RadioButton
    private lateinit var alquiler : RadioButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_property)
        providerText = findViewById<TextView>(R.id.providerEdit)
        emailText = findViewById<TextView>(R.id.emailEdit)

        descripcion = findViewById(R.id.desc_editP)
        direccion = findViewById(R.id.direccion_editP)
        habitacion = findViewById(R.id.hab_editP)
        ba = findViewById(R.id.ba_editP)
        nombre = findViewById(R.id.nombreEdit)
        btnAdd = findViewById(R.id.btnAddProperty)
        precio = findViewById(R.id.preciotext)
        alquiler = findViewById(R.id.alquilertext)
        venta = findViewById(R.id.ventatext)

        //Setup
        val bundle=intent.extras
        email = bundle?.getString("email") ?: ""
        provider = bundle?.getString("provider") ?: ""
        setup(email?:"",provider?:"")



    }
    private fun setup(email : String,provider:String) {
        title = "Inicio"
        emailText.text = email
        providerText.text = provider

        db.collection("user").document(email).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    nombre.text = document.getString("nombre") ?: ""
                } else {
                    Log.d(TAG, "No such document")
                }
            }
        btnAdd.setOnClickListener {
            var tipo :String?
            if(alquiler.isChecked){
                tipo = "Alquiler"
            }else{
                tipo = "Venta"
            }
            val propertyData = hashMapOf(
                "direccion" to direccion.text.toString(),
                "habitaciones" to habitacion.text.toString(),
                "baños" to ba.text.toString(),
                "descripcion" to descripcion.text.toString(),
                "asesorNombreItem" to nombre.text.toString(),
                "precio" to precio.text.toString(),
                "tipo" to tipo,
                "userId" to email
            )

            db.collection("property").add(propertyData)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(this, "Propiedad añadida con éxito", Toast.LENGTH_SHORT).show()

                    // Agrega el ID del documento al campo 'id' del documento
                    db.collection("property").document(documentReference.id).update("idP", documentReference.id)

                    direccion.setText("")
                    habitacion.setText("")
                    ba.setText("")
                    descripcion.setText("")
                    nombre.setText("")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(this, "Error al añadir propiedad", Toast.LENGTH_SHORT).show()
                }
        }
    }
}