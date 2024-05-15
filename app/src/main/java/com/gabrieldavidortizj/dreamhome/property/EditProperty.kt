package com.gabrieldavidortizj.dreamhome.property

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.gabrieldavidortizj.dreamhome.HomeActivity
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.firestore.FirebaseFirestore

class EditProperty : AppCompatActivity() {
     private lateinit var idT : TextView
     private lateinit var id : String
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
        setContentView(R.layout.activity_edit_property)

         idT = findViewById<TextView>(R.id.idpropertytext)

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
        val bundle = intent.extras
        id = bundle?.getString("DOCUMENT_ID") ?: ""
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
        var tipo :String?

        idT.text = id
                // Recuperar los valores del documento
        db.collection("property").document(id).get().addOnSuccessListener { document ->
            if (document != null) {
                descripcion.setText(document.getString("descripcion"))
                direccion.setText(document.getString("direccion"))
                habitacion.setText(document.getString("habitaciones"))
                ba.setText(document.getString("baños"))
                nombre.setText(document.getString("asesorNombreItem"))
                precio.setText(document.getString("precio"))
                tipo = document.getString("tipo")
                if(tipo.equals("Venta")){
                    venta.isChecked = true
                }else{
                    alquiler.isChecked = true
                }
             } else {
                Log.d(TAG, "No exist tal documento")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error al obtener el documento", exception)
        }

         btnAdd.setOnClickListener {
            var tipo :String?
            if(venta.isChecked){
                tipo = "Venta"
            }else{
                tipo = "Alquilar"
            }
            val propertyUpdates = hashMapOf<String, Any>(
                "descripcion" to descripcion.text.toString(),
                "direccion" to direccion.text.toString(),
                "habitaciones" to habitacion.text.toString(),
                "baños" to ba.text.toString(),
                "precio" to precio.text.toString(),
                "tipo" to tipo.toString()
             )

            db.collection("property").document(id).update(propertyUpdates)
                .addOnSuccessListener { Log.d(TAG, "Documento actualizado con éxito!")
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() }
                .addOnFailureListener { e -> Log.w(TAG, "Error al actualizar el documento", e) }

        }

    }
}