package org.itson.tripsplit.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.Usuario

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        val txtLogin : TextView = findViewById(R.id.txtLogin)
        val edtName : EditText = findViewById(R.id.edtName)
        val edtEmail : EditText = findViewById(R.id.edtEmail)
        val edtPass : EditText = findViewById(R.id.edtPass)
        val edtConfirmPass : EditText = findViewById(R.id.edtConfirmPass)
        val btnRegister : Button = findViewById(R.id.btnRegister)

        txtLogin.paint.isUnderlineText = true

        txtLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            if (edtEmail.text.isEmpty()
                || edtPass.text.isEmpty()
                || edtConfirmPass.text.isEmpty()
                || edtName.text.isEmpty()) {
                Toast.makeText(
                    baseContext,
                    "Todos los campos deben estar llenos.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!edtPass.text.toString().equals(edtConfirmPass.text.toString())) {
                Toast.makeText(
                    baseContext,
                    "Las contraseñas no coinciden.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signIn(
                    edtEmail.text.toString().trim(),
                    edtName.text.toString(),
                    edtPass.text.toString())
            }
        }

        var isPasswordVisible = false
        edtPass.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtPass.compoundDrawables[2]
                if (drawableEnd != null) {
                    val bounds = drawableEnd.bounds
                    val x = event.x.toInt()
                    val drawableX = edtPass.width - edtPass.paddingEnd - bounds.width()
                    if (x >= drawableX) {
                        // Cambiar visibilidad
                        isPasswordVisible = !isPasswordVisible
                        if (isPasswordVisible) {
                            edtPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            edtPass.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.ic_eye_open, 0)
                        } else {
                            edtPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            edtPass.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.ic_eye_closed, 0)
                        }
                        // Mantener cursor al final
                        edtPass.setSelection(edtPass.text.length)
                        // Recuperar formato original
                        edtPass.textSize = 17f
                        edtPass.typeface = ResourcesCompat.getFont(this, R.font.inter)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        var isPassConfirmVisible = false
        edtConfirmPass.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtConfirmPass.compoundDrawables[2]
                if (drawableEnd != null) {
                    val bounds = drawableEnd.bounds
                    val x = event.x.toInt()
                    val drawableX = edtConfirmPass.width - edtConfirmPass.paddingEnd - bounds.width()
                    if (x >= drawableX) {
                        // Cambiar visibilidad
                        isPassConfirmVisible = !isPassConfirmVisible
                        if (isPassConfirmVisible) {
                            edtConfirmPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            edtConfirmPass.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.ic_eye_open, 0)
                        } else {
                            edtConfirmPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            edtConfirmPass.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.ic_eye_closed, 0)
                        }
                        // Mantener cursor al final
                        edtConfirmPass.setSelection(edtConfirmPass.text.length)
                        // Recuperar formato original
                        edtConfirmPass.textSize = 17f
                        edtConfirmPass.typeface = ResourcesCompat.getFont(this, R.font.inter)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

    }

    fun signIn(email: String, name: String, password: String) {
        Log.d("INFO", "email: ${email}, password: ${password}")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid : String = auth.currentUser?.uid!!
                    val imagenURL: String = ""
                    val user = Usuario(
                        uid,
                        name,
                        email,
                        imagenURL
                    )
                    val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)

                    userRef.setValue(user).addOnSuccessListener {
                        Toast.makeText(
                            baseContext,
                            "Usuario registrado.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } .addOnFailureListener { e ->
                        Log.w("ERROR", "El registro falló.", task.exception)
                        Toast.makeText(
                            baseContext,
                            "El registro falló.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.w("ERROR", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "El registro falló.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }

}