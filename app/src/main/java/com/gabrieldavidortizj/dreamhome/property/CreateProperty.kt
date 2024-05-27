package com.gabrieldavidortizj.dreamhome.property

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

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
    private lateinit var foto : TextView
    private lateinit var galeria : TextView
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_PHOTO_REQUEST = 2
    private lateinit var photoURI: Uri


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
        foto = findViewById(R.id.fotoproperty)
        galeria = findViewById(R.id.albunproperty)

        //Setup
        val bundle=intent.extras
        email = bundle?.getString("email") ?: ""
        provider = bundle?.getString("provider") ?: ""
        setup(email?:"",provider?:"")

        storage = FirebaseStorage.getInstance()

        foto.setOnClickListener {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.gabrieldavidortizj.dreamhome.fileprovider",
                    it
                )
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
        galeria.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PICK_PHOTO_REQUEST)
        }
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
                    uploadImageToFirebase(documentReference.id)

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
    }private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            imageUri = Uri.fromFile(this)
        }
    }

    private fun uploadImageToFirebase(propertyId: String) {
        if (imageUri != null) {
            val ref = storage.reference.child("propertyImages/$propertyId")
            ref.putFile(imageUri!!)
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = photoURI
        } else if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            imageUri = data?.data
        }
        if (imageUri == null) {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }
}