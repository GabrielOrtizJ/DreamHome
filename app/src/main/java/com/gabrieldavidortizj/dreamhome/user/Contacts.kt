package com.gabrieldavidortizj.dreamhome.user

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
import com.gabrieldavidortizj.dreamhome.property.EditProperty
import com.google.firebase.firestore.FirebaseFirestore
 import java.util.Locale

class Contacts : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private  var mList = ArrayList<user>()
    private lateinit var adapter : UserAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("user")
    private lateinit var email : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        recyclerView = findViewById(R.id.recyclerViewContacts)
        searchView = findViewById(R.id.searchViewContacts)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val bundle=intent.extras
        email = bundle?.getString("email") ?: ""

        addDataToList()

        adapter = UserAdapter(mList, true) { view, documentId ->
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
        inflater.inflate(R.menu.popup_contacts, popup.menu)

        // Configurar un listener para manejar clicks en las opciones del menú
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_show -> {
                    val intent = Intent(this, viewContacts::class.java).apply {
                        putExtra("DOCUMENT_ID", documentId) // Pasa el ID del documento como un extra
                    }
                    startActivity(intent)
                    Toast.makeText(this, "Mostrar", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.correo -> {
                    val intent = Intent(this, mailPersonas::class.java).apply {
                        putExtra("email", documentId) // Pasa el ID del documento como un extra
                    }
                    startActivity(intent)
                    // Manejar la opción "Editar"
                    Toast.makeText(this, "Redactar correo a este contacto", Toast.LENGTH_SHORT).show()
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
            val filteredList = ArrayList<user>()
            for(i in mList){
                if(i.nombre.lowercase(Locale.ROOT).contains(query)){
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
        userCollection
            .whereEqualTo("tipo", "asesor")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(user ::class.java)
                    mList.add(user)
                }
                 adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}


