package com.gabrieldavidortizj.dreamhome.property

import android.content.ContentValues.TAG
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class properties : AppCompatActivity() {
    private lateinit var email : String
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private  var mList = ArrayList<PropertyData>()
    private lateinit var adapter : PropertyAdapter
    private val db = FirebaseFirestore.getInstance()
    private val propertiesCollection = db.collection("property")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_properties)
        val bundle=intent.extras
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchViewProperties)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        addDataToList()
        email = bundle?.getString("email") ?: ""

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
                Toast.makeText(this,"NO Data found",Toast.LENGTH_SHORT).show()
            }else{
                    adapter.setFilteredList(filteredList)
            }
        }
    }
    private fun showPopup(view: View, documentId: String?) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.popup_menuproperties, popup.menu)

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
                R.id.option_favorite -> {
                    // Obtén el ID del usuario actualmente autenticado
                    val userId = email

                    if (userId != null && documentId != null) {
                        // Actualiza la lista de propiedades favoritas del usuario
                        val userRef = db.collection("user").document(userId)
                        val updates = hashMapOf<String, Any>(
                            "favoriteProperties" to FieldValue.arrayUnion(documentId)
                        )

                        userRef.update(updates)
                            .addOnSuccessListener { Log.d(TAG, "Usuario actualizado con éxito!")
                                Toast.makeText(this, "Añadido a favoritos ", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e -> Log.w(TAG, "Error al actualizar el usuario", e)
                                Toast.makeText(this, "no se a podido añadir a favoritos debes identificarte antes para hacer esto" , Toast.LENGTH_SHORT).show()
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
        propertiesCollection
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val property = document.toObject(PropertyData::class.java)
                    mList.add(property)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

}


