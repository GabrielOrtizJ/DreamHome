package com.gabrieldavidortizj.dreamhome.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.gabrieldavidortizj.dreamhome.HomeActivity
import com.gabrieldavidortizj.dreamhome.ProviderType
import com.gabrieldavidortizj.dreamhome.R
import com.google.firebase.auth.FirebaseAuth

class signIn : AppCompatActivity() {
    private lateinit var signInbutto : TextView
    private lateinit var emailsignInEditTex: EditText
    private lateinit var pwdsignInEditTex : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        signInbutto = findViewById<TextView>(R.id.sigInButton )
        emailsignInEditTex = findViewById<EditText>(R.id.signInEmailTextView)
        pwdsignInEditTex = findViewById<EditText>(R.id.signInPwdTextView)

        //Setup
        setup()
    }
    private fun setup(){
        signInbutto.setOnClickListener {

            if(emailsignInEditTex.text.isNotEmpty() && pwdsignInEditTex.text.isNotEmpty()){
                if(pwdsignInEditTex.text.length >= 6) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailsignInEditTex.text.toString(),
                        pwdsignInEditTex.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(it.result?.user?.email ?: "",ProviderType.BASIC)
                        }else{
                            showAlert()
                        }
                    }
                } else {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("error")
        builder.setMessage("se ha producido un error autenticando el usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog :AlertDialog = builder.create()
        dialog.show()
    }
    private fun showHome(email: String,provider: ProviderType){

        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homeIntent)

    }

}