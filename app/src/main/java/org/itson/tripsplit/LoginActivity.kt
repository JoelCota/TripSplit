package org.itson.tripsplit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txtForgotPass: TextView = findViewById(R.id.txtForgotPass)
        val txtRegister: TextView = findViewById(R.id.txtRegister)
        val edtEmail: EditText = findViewById(R.id.edtEmail)
        val edtPass: EditText = findViewById(R.id.edtPass)
        val btnLogin: Button = findViewById(R.id.btnLogin)

        txtForgotPass.paint.isUnderlineText = true
        txtRegister.paint.isUnderlineText = true

        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            if (edtEmail.text.isEmpty() || edtPass.text.isEmpty()) {
                Toast.makeText(
                    baseContext,
                    "Todos los campos deben estar llenos.",
                    Toast.LENGTH_SHORT
                ).show()
            }  else {
                login(edtEmail.text.toString(), edtPass.text.toString())
            }
        }
    }

    fun goToMain(user: FirebaseUser) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user.email)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    goToMain(user!!)
                } else {
                    Toast.makeText(
                        baseContext,
                        "Usuario y/o contraseña equivocados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}