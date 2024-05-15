package com.gabrieldavidortizj.dreamhome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        val direccion: TextView = itemView.findViewById(R.id.direccionItem)
        val habitaciones: TextView = itemView.findViewById(R.id.habItem)
        val baños: TextView = itemView.findViewById(R.id.bañosItem)
        val descripcion: TextView = itemView.findViewById(R.id.descripcionItem)
        val asesorNombreItem: TextView = itemView.findViewById(R.id.asesorNombreItem)
        val precio: TextView = itemView.findViewById(R.id.precio)
        val tipo: TextView = itemView.findViewById(R.id.tipo)
        val idP: TextView = itemView.findViewById(R.id.idP)
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

    }

    override fun getItemCount(): Int {
        return  mList.size
    }


}