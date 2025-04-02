package org.itson.tripsplit.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.itson.tripsplit.R
import org.itson.tripsplit.ui.fragments.CuentaFragment
import org.itson.tripsplit.ui.fragments.GruposFragment
import org.itson.tripsplit.ui.fragments.NuevoGastoFragment
import org.itson.tripsplit.data.model.User

class MainActivity : AppCompatActivity() {
    private var currentUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Cargar datos del usuario
        loadUserData()

        // Encuentra el BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Configura el listener para el BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_grupos -> {
                    // Muestra el fragmento Grupos
                    replaceFragment(GruposFragment())
                    true
                }
                R.id.nav_nuevo_gasto -> {
                    replaceFragment(NuevoGastoFragment())
                    true
                }
                R.id.nav_cuenta -> {
                    replaceFragment(CuentaFragment())
                    true
                }
                else -> false
            }
        }

        // Cargar el fragmento por defecto (Grupos) al iniciar la app
        if (savedInstanceState == null) {
            replaceFragment(GruposFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        // Cargar el fragmento en el FrameLayout
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null) // Opcional, para permitir volver al fragmento anterior
        transaction.commit()
    }

    fun loadUserData() {
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()

        val uid = auth.currentUser?.uid ?: return

        val userRef = database.getReference("Users").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    val user = User (
                        uid,
                        email,
                        name
                    )

                    Toast.makeText(
                        baseContext,
                        "Bienvenido/a: "+user.name,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("INFO", "Sesión iniciada: "+user.toString())

                    currentUser = user
                } else {

                    Log.w("ERROR", "No se encontraron los datos del usuario: "+uid, null)
                    Toast.makeText(
                        baseContext,
                        "No se encontraron los datos del usuario.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ERROR", "Error al leer datos del usuario: "+uid, error.toException())
                Toast.makeText(
                    baseContext,
                    "Error al leer datos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
