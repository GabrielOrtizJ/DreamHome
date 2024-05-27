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
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gabrieldavidortizj.dreamhome.HomeActivity
import com.gabrieldavidortizj.dreamhome.R
import com.gabrieldavidortizj.dreamhome.user.EditProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class EditProperty : AppCompatActivity() {
    private lateinit var idT : TextView
    private lateinit var id : String
    private val db = FirebaseFirestore.getInstance()
    private lateinit var descripcion : EditText
    private lateinit var direccion : EditText
    private lateinit var habitacion : EditText
    private lateinit var ba  : EditText
    private lateinit var nombre : TextView
    private lateinit var btnconfig : TextView

    private lateinit var precio : EditText
    private lateinit var venta : RadioButton
    private lateinit var alquiler : RadioButton
    private lateinit var image : ImageView
    private lateinit var foto : TextView
    private lateinit var galeria : TextView
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_PHOTO_REQUEST = 2
    private lateinit var photoURI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_property)

         idT = findViewById<TextView>(R.id.idpropertytext)

        descripcion = findViewById(R.id.desc_editP)
        direccion = findViewById(R.id.direccion_editP)
        habitacion = findViewById(R.id.hab_editP)
        ba = findViewById(R.id.ba_editP)
        nombre = findViewById(R.id.nombreEdit)
        btnconfig = findViewById(R.id.btnAddProperty)
        precio = findViewById(R.id.preciotext)
        alquiler = findViewById(R.id.alquilertext)
        venta = findViewById(R.id.ventatext)

        image = findViewById(R.id.imageCproperty)
        foto = findViewById(R.id.fotoCproperty)
        galeria = findViewById(R.id.albunCproperty)

                 //Setup
        val bundle = intent.extras
        id = bundle?.getString("DOCUMENT_ID") ?: ""
         var tipo :String?

        idT.text = id
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
                imageUri = photoURI // Actualiza imageUri aquí
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        galeria.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, EditProfile.PICK_PHOTO_REQUEST)
        }
                // Recuperar los valores del documento
        db.collection("property").document(id).get().addOnSuccessListener { document ->
            if (document != null) {
                descripcion.setText(document.getString("descripcion"))
                direccion.setText(document.getString("direccion"))
                habitacion.setText(document.getString("habitaciones"))
                ba.setText(document.getString("baños"))
                nombre.setText(document.getString("asesorNombreItem"))
                precio.setText(document.getString("precio"))
                storage = FirebaseStorage.getInstance()

                tipo = document.getString("tipo")
                downloadImageFromFirebase(id)

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

        btnconfig.setOnClickListener {
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
                .addOnSuccessListener {
                    Log.d(TAG, "Documento actualizado con éxito!")
                    // Aquí es donde se llama a la función uploadImageToFirebase
                    uploadImageToFirebase(id)
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error al actualizar el documento", e) }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            imageUri = data?.data
        }
        if (imageUri == null) {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        } else {
            // Aquí puedes establecer la imagen seleccionada en tu ImageView
            image.setImageURI(imageUri)
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            photoURI = Uri.fromFile(this)
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
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_PHOTO_REQUEST = 2
    }
}
