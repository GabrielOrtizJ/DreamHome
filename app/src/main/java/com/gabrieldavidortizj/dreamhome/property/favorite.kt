package com.gabrieldavidortizj.dreamhome.property

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class favorite : AppCompatActivity() {
    private lateinit var email : String
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private  var mList = ArrayList<PropertyData>()
    private lateinit var adapter : PropertyAdapter
    private val db = FirebaseFirestore.getInstance()
    private val propertiesCollection = db.collection("property")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        val bundle=intent.extras
        email = bundle?.getString("email") ?: ""

        recyclerView = findViewById(R.id.recyclerViewfavorite)
        searchView = findViewById(R.id.searchViewfavorite)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        addDataToList()

        adapter = PropertyAdapter(mList, true) { view, documentId ->
            showPopup(view, documentId)
        }
        recyclerView.adapter = adapter
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterList(newText)
                }
                return true
            }

        } )
    }
    private fun filterList(query: String){
        if(query != null){
            val filteredList = ArrayList<PropertyData>()
            for(i in mList){
                if(i.direccion.lowercase(Locale.ROOT).contains(query)){
                    filteredList.add(i)
                }
            }
            if(filteredList.isEmpty()){
                Toast.makeText(this,"NO Data found", Toast.LENGTH_SHORT).show()
            }else{
                adapter.setFilteredList(filteredList)
            }
        }
    }
    private fun showPopup(view: View, documentId: String?) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.popupfavorite, popup.menu)

        // Configurar un listener para manejar clicks en las opciones del menú
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_show -> {
                    val intent = Intent(this, viewProperty::class.java).apply {
                        putExtra("DOCUMENT_ID", documentId) // Pasa el ID del documento como un extra
                    }
                    startActivity(intent)
                    Toast.makeText(this, "Mostrar", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.eliminar_favorite -> {
                    // Obtén el ID del usuario actualmente autenticado
                    val userId = email

                    if (userId != null && documentId != null) {
                        // Actualiza la lista de propiedades favoritas del usuario
                        val userRef = db.collection("user").document(userId)
                        val updates = hashMapOf<String, Any>(
                            "favoriteProperties" to FieldValue.arrayRemove(documentId)
                        )
                        userRef.update(updates)
                            .addOnSuccessListener { Log.d(ContentValues.TAG, "Usuario actualizado con éxito!")
                                Toast.makeText(this, "Eliminado de favoritos ", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error al actualizar el usuario", e)
                                Toast.makeText(this, "No se ha podido eliminar de favoritos, debes identificarte antes para hacer esto" , Toast.LENGTH_SHORT).show()
                            }
                    }

                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun addDataToList() {
        val userRef = db.collection("user").document(email)
        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val favoriteList = document["favoriteProperties"] as List<String>
                // Ahora usa esta lista para obtener solo esas propiedades
                favoriteList.forEach { id ->
                    propertiesCollection.document(id).get().addOnSuccessListener { propertyDocument ->
                        val property = propertyDocument.toObject(PropertyData::class.java)
                        if (property != null) {
                            mList.add(property)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            } else {
                Log.d(ContentValues.TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "get failed with ", exception)
        }
    }

}

