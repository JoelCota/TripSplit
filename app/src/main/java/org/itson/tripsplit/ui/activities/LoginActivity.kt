package org.itson.tripsplit.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.itson.tripsplit.R
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.ui.fragments.CrearGrupoFragment

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

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

        val userRepository = UserRepository()

        btnLogin.setOnClickListener {
            if (edtEmail.text.isEmpty() || edtPass.text.isEmpty()) {
                Toast.makeText(this, "Todos los campos deben estar llenos.", Toast.LENGTH_SHORT).show()
            } else {
                userRepository.login(
                    edtEmail.text.toString().trim(),
                    edtPass.text.toString(),
                    onSuccess = { user -> goToMain(user) },
                    onError = { error -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show() }
                )
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
                        edtPass.textSize = 18f
                        edtPass.typeface = ResourcesCompat.getFont(this, R.font.inter)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    fun goToMain(user: FirebaseUser) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user.uid)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}