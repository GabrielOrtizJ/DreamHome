package com.gabrieldavidortizj.dreamhome.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.gabrieldavidortizj.dreamhome.R
import org.w3c.dom.Text

class mailPersonas : AppCompatActivity() {
    private lateinit var email : String
    private lateinit var emailText : EditText
    private lateinit var tituloText : EditText
    private lateinit var asuntoText : EditText
    private lateinit var sendbtn : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail_personas)
            var bundle=intent.extras
        email = bundle?.getString("email") ?: ""

        emailText =  findViewById(R.id.receptorMail)
        tituloText = findViewById(R.id.tituloMail)
        asuntoText = findViewById(R.id.asuntoMail)
        sendbtn =    findViewById(R.id.sendMail)



        if(!email.isEmpty()){
            emailText.text =  Editable.Factory.getInstance().newEditable(email)
        }

        sendbtn.setOnClickListener{
            if(emailText.text.isEmpty() || tituloText.text.isEmpty() || asuntoText.text.isEmpty()){
                Toast.makeText(this, "Por favor, rellene todos los campos.", Toast.LENGTH_SHORT).show()
            } else {
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(emailText.text.toString()))
                    putExtra(Intent.EXTRA_SUBJECT, "DreamHome  " + tituloText.text)
                    putExtra(Intent.EXTRA_TEXT, asuntoText.text)
                }
                try {
                    startActivity(Intent.createChooser(i, "Enviar correo..."))
                } catch (ex: Exception) {
                    Toast.makeText(this, "No hay clientes de correo instalados.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
