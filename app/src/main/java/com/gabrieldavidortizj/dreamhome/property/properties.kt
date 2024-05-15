package com.gabrieldavidortizj.dreamhome.property

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gabrieldavidortizj.dreamhome.PropertyAdapter
import com.gabrieldavidortizj.dreamhome.PropertyData
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class properties : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private  var mList = ArrayList<PropertyData>()
    private lateinit var adapter : PropertyAdapter
    private val db = FirebaseFirestore.getInstance()
    private val propertiesCollection = db.collection("property")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_properties)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchViewProperties)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        addDataToList()

        adapter = PropertyAdapter(mList) { _, _ -> }
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


