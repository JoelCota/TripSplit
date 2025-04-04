package org.itson.tripsplit.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.User
import org.itson.tripsplit.data.repository.GrupoRepository
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.ui.fragments.GruposFragment
import org.itson.tripsplit.ui.fragments.SinGruposFragment
import org.itson.tripsplit.ui.fragments.CuentaFragment

class MainActivity : AppCompatActivity() {
    private var currentUser: User? = null
    private val userRepository = UserRepository()
    private val grupoRepository = GrupoRepository()

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
                  verificarGrupos()
                    true
                }
                R.id.nav_cuenta -> {
                    replaceFragment(CuentaFragment())
                    true
                }
                else -> false
            }
        }

        verificarGrupos()
    }

    private fun replaceFragment(fragment: Fragment){
        // Cargar el fragmento en el FrameLayout
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun verificarGrupos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("DEBUG", "Verificando grupos para usuario: $userId")

        grupoRepository.getUserGroups(userId) { grupos ->
            Log.d("DEBUG", "Cantidad de grupos obtenidos: ${grupos.size}")

            if (grupos.isNotEmpty()) {
                Log.d("DEBUG", "Mostrando GruposFragment")
                replaceFragment(GruposFragment())
            } else {
                Log.d("DEBUG", "Mostrando SinGruposFragment")
                replaceFragment(SinGruposFragment())
            }
        }
    }

    fun loadUserData() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        // Usar el UserRepository para obtener los datos del usuario
        userRepository.getUser(uid) { user ->
            if (user != null) {
                // Mostrar un mensaje de bienvenida si el usuario es encontrado
                Toast.makeText(baseContext, "Bienvenido/a: " + user.name, Toast.LENGTH_SHORT).show()
                currentUser = user

                Log.d("INFO", "Sesión iniciada: " + user.toString())

            } else {
                Log.w("ERROR", "No se encontraron los datos del usuario: " + uid)
                Toast.makeText(baseContext, "No se encontraron los datos del usuario.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
