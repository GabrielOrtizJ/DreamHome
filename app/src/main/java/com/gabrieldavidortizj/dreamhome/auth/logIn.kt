package com.gabrieldavidortizj.dreamhome.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.gabrieldavidortizj.dreamhome.HomeActivity
import com.gabrieldavidortizj.dreamhome.ProviderType
import com.gabrieldavidortizj.dreamhome.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

class logIn : AppCompatActivity() {

    private val GOOGLE_SING_IN = 100

    private lateinit var emailLogIn: EditText
    private lateinit var pwdLogIn: EditText
    private lateinit var logInb :TextView
    private lateinit var goolelogInb :TextView
    private lateinit var logInLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        emailLogIn = findViewById<EditText>(R.id.logInEmailTextView)
        pwdLogIn = findViewById<EditText>(R.id.logInPwdTextView)
        logInb = findViewById<TextView>(R.id.logInButton)
        goolelogInb = findViewById<TextView>(R.id.logInGoogleButton)
        logInLayout = findViewById<LinearLayout>(R.id.authLogInlinearLayout)
        setup()
        session()
        notification()
    }
    override fun onStart(){
        super.onStart()
        logInLayout.visibility= View.VISIBLE
    }
    private fun session(){
        val sharedPreferences = getSharedPreferences(getString(R.string.prefs_file) , Context.MODE_PRIVATE)
        val prefs = sharedPreferences
        val email= prefs.getString("email",null)
        val provider  = prefs.getString("provider",null)

        if(email != null && provider!= null ){
            logInLayout.visibility = View.INVISIBLE
            showHome(email,ProviderType.valueOf(provider))
        }
    }
    private fun notification() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            task.result?.let { token ->
                println("Este es el token del dispositivo: $token")
            }
        }
        //temas
        FirebaseMessaging.getInstance().subscribeToTopic("pisos")
        //Recuperar informacion
        val url = intent.getStringExtra("url")
        url?.let {
            println("Ha llegado la informacion en un push: ${it}")
        }
    }
    private fun setup(){
        logInb.setOnClickListener {
            if(emailLogIn.text.isNotEmpty() && pwdLogIn.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(emailLogIn.text.toString(),
                        pwdLogIn.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        }else{
                            showAlert()
                        }
                   }
            }
        }
        goolelogInb.setOnClickListener{
            //configuracion
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent,GOOGLE_SING_IN)
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("error")
        builder.setMessage("se ha producido un error autenticando el usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }
    private fun showHome(email: String,provider: ProviderType){

        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== GOOGLE_SING_IN ){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(account.email?:"",ProviderType.GOOGLE)
                        }else{
                            showAlert()
                        }
                    }
                }
            }catch (e: ApiException){
                showAlert()
                Toast.makeText(this, "¡Error en la autenticación con Google!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}