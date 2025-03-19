package org.itson.tripsplit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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

        val user : String? = intent.extras!!.getString("user")
        if (user!=null) {
            Toast.makeText(
                baseContext,
                "Sesión iniciada: "+user,
                Toast.LENGTH_SHORT
            ).show()
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
}
