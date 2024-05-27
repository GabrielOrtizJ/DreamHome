package com.gabrieldavidortizj.dreamhome.user

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class EditProfile : AppCompatActivity() {
    private lateinit var providert: TextView
    private lateinit var emailt: TextView
    private lateinit var deleteb: TextView
    private lateinit var saveb: TextView
    private lateinit var getb: TextView
    private lateinit var home: TextView
    private lateinit var nombre: EditText
    private lateinit var direccion: EditText
    private lateinit var telefono: EditText
    private lateinit var estandarm: RadioButton
    private lateinit var asesorm: RadioButton
    private lateinit var foto: TextView
    private lateinit var galeria: TextView
    private lateinit var image: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null
    private lateinit var photoURI: Uri

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
        direccion = findViewById(R.id.addressTextText)
        telefono = findViewById(R.id.phoneTextText)
        estandarm = findViewById(R.id.radioEstandar)
        asesorm = findViewById(R.id.radioAsesor)
        galeria = findViewById(R.id.galeriaperfil)
        foto = findViewById(R.id.camaraperfil)
        image = findViewById(R.id.imagePerfil)
        // Obteniendo el email y el provider del Intent
        val email = intent.getStringExtra("email")
        val provider = intent.getStringExtra("provider")
        setup(email ?: "", provider ?: "")

        foto.setOnClickListener {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
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

        home.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setup(email: String, provider: String) {
        // Configurando las vistas con los valores obtenidos
        emailt.text = email
        providert.text = provider
        saveb.setOnClickListener {
            var tipo: String?
            if (estandarm.isChecked) {
                tipo = "estandar"
            } else {
                tipo = "asesor"
            }
            db.collection("user").document(email).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Si el documento existe, actualizamos los campos necesarios
                    val updates = hashMapOf(
                        "provider" to provider,
                        "nombre" to nombre.text.toString(),
                        "address" to direccion.text.toString(),
                        "phone" to telefono.text.toString(),
                        "tipo" to tipo,
                        "idU" to email
                    )
                    db.collection("user").document(email).update(updates as Map<String, Any>).addOnSuccessListener {
                        // Limpiamos los campos y mostramos un Toast
                        nombre.text.clear()
                        direccion.text.clear()
                        telefono.text.clear()
                        Toast.makeText(this, "Los cambios se han realizado correctamente", Toast.LENGTH_SHORT).show()
                        uploadImageToFirebase(email) //Subimos la imagen a Firebase Storage
                    }
                } else {
                    // Si el documento no existe, creamos uno nuevo
                    val newUser = hashMapOf(
                        "provider" to provider,
                        "nombre" to nombre.text.toString(),
                        "address" to direccion.text.toString(),
                        "phone" to telefono.text.toString(),
                        "tipo" to tipo,
                        "idU" to email,
                        "favoriteProperties" to mutableListOf<String>()
                    )
                    db.collection("user").document(email).set(newUser).addOnSuccessListener {
                        // Limpiamos los campos y mostramos un Toast
                        nombre.text.clear()
                        direccion.text.clear()
                        telefono.text.clear()
                        Toast.makeText(this, "Los cambios se han realizado correctamente", Toast.LENGTH_SHORT).show()
                        uploadImageToFirebase(email) // <Subimos la imagen a Firebase Storage
                    }
                }
            }
        }

    getb.setOnClickListener {
            var tipo: String?
            db.collection("user").document(email).get().addOnSuccessListener {
                direccion.setText(it.get("address") as? String ?: "")
                telefono.setText(it.get("phone") as? String ?: "")
                nombre.setText(it.get("nombre") as? String ?: "")
                tipo = it.get("tipo") as? String
                if (tipo.equals("asesor")) {
                    asesorm.isChecked = true
                } else {
                    estandarm.isChecked = true
                }
            }
        downloadImageFromFirebase(email)
    }
        deleteb.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro de que quieres eliminar tus datos?")
                .setPositiveButton("Sí") { _, _ ->
                    // Si el usuario confirma, se eliminan los datos
                    db.collection("user").document(email).delete()

                    // Aquí eliminamos la imagen de Firebase Storage
                    val ref = storage.reference.child("images/$email")
                    ref.delete().addOnSuccessListener {
                        Toast.makeText(this, "Imagen eliminada correctamente", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
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

    private fun uploadImageToFirebase(userId: String) {
        if (imageUri != null) {
            val ref = storage.reference.child("images/$userId")
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

    private fun downloadImageFromFirebase(userId: String) {
        val ref = storage.reference.child("images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            val url = uri.toString()
            Picasso.get().load(url).into(image)
        }.addOnFailureListener {
            Toast.makeText(this, "Error al descargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_PHOTO_REQUEST = 2
    }
}
