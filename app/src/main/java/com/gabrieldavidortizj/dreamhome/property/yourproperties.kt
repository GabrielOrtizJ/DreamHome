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
import com.gabrieldavidortizj.dreamhome.PropertyAdapter
import com.gabrieldavidortizj.dreamhome.PropertyData
import com.gabrieldavidortizj.dreamhome.R
import com.gabrieldavidortizj.dreamhome.auth.signIn
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class yourproperties : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private  var mList = ArrayList<PropertyData>()
    private lateinit var adapter : PropertyAdapter
    private val db = FirebaseFirestore.getInstance()
    private val propertiesCollection = db.collection("property")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yourproperties)

        recyclerView = findViewById(R.id.recyclerViews)
        searchView = findViewById(R.id.searchViewYourProperties)
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

    private fun showPopup(view: View, documentId: String?) {
        // Crear una instancia de PopupMenu
        val popup = PopupMenu(this, view)
        // Inflar el menú con las opciones
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.popup_menu, popup.menu)

        // Configurar un listener para manejar clicks en las opciones del menú
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_show -> {
                    val intent = Intent(this, signIn::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Mostrar", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.option_edit -> {
                    val intent = Intent(this,EditProperty::class.java).apply {
                        putExtra("DOCUMENT_ID", documentId) // Pasa el ID del documento como un extra
                    }
                    startActivity(intent)
                    // Manejar la opción "Editar"
                    Toast.makeText(this, "Editar", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.option_delete -> {
                    // Manejar la opción "Eliminar"
                    Toast.makeText(this, "Eliminar", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Mostrar el menú
        popup.show()
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
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}
