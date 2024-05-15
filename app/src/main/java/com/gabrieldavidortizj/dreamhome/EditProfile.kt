package com.gabrieldavidortizj.dreamhome

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class EditProfile : AppCompatActivity() {
    private lateinit var providert: TextView
    private lateinit var emailt : TextView
    private lateinit var deleteb: TextView
    private lateinit var saveb : TextView
    private lateinit var getb : TextView
    private lateinit var home: TextView
    private lateinit var nombre : EditText
    private lateinit var direccion : EditText
    private lateinit var telefono : EditText
    private lateinit var estandarm : RadioButton
    private lateinit var asesorm :RadioButton

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        providert = findViewById(R.id.providertextView)
        emailt = findViewById(R.id.emailtextView)
        deleteb = findViewById(R.id.deleteButton)
        saveb = findViewById(R.id.saveButton)
        getb = findViewById(R.id.getButton)
        home = findViewById(R.id.homeButton)
        nombre = findViewById(R.id.nameTextText)
        direccion =  findViewById(R.id.addressTextText)
        telefono = findViewById(R.id.phoneTextText)
        estandarm = findViewById(R.id.radioEstandar)
        asesorm = findViewById(R.id.radioAsesor)
        // Obteniendo el email y el provider del Intent
        val email = intent.getStringExtra("email")
        val provider = intent.getStringExtra("provider")
        setup(email ?: "", provider ?: "")


        home.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }
    private fun setup(email: String, provider: String){
        // Configurando las vistas con los valores obtenidos
        emailt.text = email
        providert.text = provider
        saveb.setOnClickListener {
            var tipo :String?
            if(estandarm.isChecked){
                tipo = "estandar"
            }else{
                tipo = "asesor"
            }
            db.collection("user").document(email).set(
                hashMapOf("provider" to provider,
                    "nombre" to nombre.text.toString(),
                    "address" to direccion.text.toString(),
                    "phone" to telefono.text.toString(),
                    "tipo" to tipo)
            )
        }
        getb.setOnClickListener {
            var tipo :String?
            db.collection("user").document(email).get().addOnSuccessListener {
                direccion.setText(it.get("address") as? String ?: "")
                telefono.setText(it.get("phone") as? String ?: "")
                nombre.setText(it.get("nombre") as? String ?: "")
                tipo = it.get("tipo") as? String
                if(tipo.equals("asesor")){
                    asesorm.isChecked = true
                }else{
                    estandarm.isChecked = true
                }
            }
        }
        deleteb.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro de que quieres eliminar tus datos?")
                .setPositiveButton("Sí") { _, _ ->
                    // Si el usuario confirma, se eliminan los datos
                    db.collection("user").document(email).delete()
                }
                .setNegativeButton("No", null)
                .show()
        }

    }


}