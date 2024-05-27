package com.gabrieldavidortizj.dreamhome.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class UserAdapter(
    private var userList: List<user>,
    private val showMenu: Boolean = false,
    private val longClickListener: (View, String?) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            if (showMenu) {
                itemView.setOnLongClickListener { v ->
                    val user = userList[adapterPosition]
                    longClickListener.invoke(v, user.idU)
                    true
                }
            }
        }
        val nombre: TextView = itemView.findViewById(R.id.nombre)
        val address: TextView = itemView.findViewById(R.id.address)
        var tipo : TextView = itemView.findViewById(R.id.tipo)
        var tlf : TextView = itemView.findViewById(R.id.tlf)
        var image: ImageView = itemView.findViewById(R.id.imageUserlist)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val currentUser = userList[position]
        holder.nombre.text = currentUser.nombre
        holder.address.text = currentUser.address
        holder.tipo.text = currentUser.tipo
        holder.tlf.text = currentUser.phone
        val userId = userList[position].idU
        downloadImageFromFirebase(userId, holder.image)
    }

    override fun getItemCount() = userList.size

    fun setFilteredList(mList: List<user>){
        this.userList = mList
        notifyDataSetChanged()
    }
    private fun downloadImageFromFirebase(userId: String, imageView: ImageView) {
        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child("images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            val url = uri.toString()
            Picasso.get().load(url).into(imageView)
        }.addOnFailureListener {
            // Puedes manejar el error aquí, por ejemplo, estableciendo una imagen predeterminada
            // imageView.setImageResource(R.drawable.default_image)
        }
    }
}

