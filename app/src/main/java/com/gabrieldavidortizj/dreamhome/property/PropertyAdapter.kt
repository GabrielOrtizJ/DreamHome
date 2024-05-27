package com.gabrieldavidortizj.dreamhome.property

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class PropertyAdapter(
    var mList: List<PropertyData>,
    private val showMenu: Boolean = false,
    private val longClickListener: (View, String?) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    inner class PropertyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        init {
            if (showMenu) {
                itemView.setOnLongClickListener { v ->
                    val property = mList[adapterPosition]
                    longClickListener.invoke(v, property.idP)
                    true
                }
            }
        }
        val direccion: TextView = itemView.findViewById(R.id.nombre)
        val habitaciones: TextView = itemView.findViewById(R.id.address)
        val baños: TextView = itemView.findViewById(R.id.tlf)
        val descripcion: TextView = itemView.findViewById(R.id.descripcionItem)
        val asesorNombreItem: TextView = itemView.findViewById(R.id.asesorNombreItem)
        val precio: TextView = itemView.findViewById(R.id.precio)
        val tipo: TextView = itemView.findViewById(R.id.tipo)
        val idP: TextView = itemView.findViewById(R.id.idP)
        var image: ImageView = itemView.findViewById(R.id.imagePropertylist)

    }

    fun setFilteredList(mList: List<PropertyData>){
        this.mList = mList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_property,parent,false)
        return PropertyViewHolder(view)
    }
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = mList[position]
        holder.direccion.text = property.direccion
        holder.habitaciones.text = property.habitaciones
        holder.baños.text = property.baños
        holder.descripcion.text = property.descripcion
        holder.asesorNombreItem.text = property.asesorNombreItem
        holder.precio.text = property.precio
        holder.tipo.text = property.tipo
        holder.idP.text = property.idP
        val userId = mList[position].idP
        downloadImageFromFirebase(userId, holder.image)
    }

    override fun getItemCount(): Int {
        return  mList.size
    }
    private fun downloadImageFromFirebase(userId: String, imageView: ImageView) {
        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child("propertyImages/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            val url = uri.toString()
            Picasso.get().load(url).into(imageView)
        }.addOnFailureListener {
            // Puedes manejar el error aquí, por ejemplo, estableciendo una imagen predeterminada
            // imageView.setImageResource(R.drawable.default_image)
        }
    }
}