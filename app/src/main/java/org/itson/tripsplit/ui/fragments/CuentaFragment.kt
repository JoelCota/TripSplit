package org.itson.tripsplit.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.ui.activities.LoginActivity

class CuentaFragment : Fragment() {

    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_cuenta, container, false)

        userRepository = UserRepository()

        val btnLogout: Button = rootView.findViewById(R.id.btnCerrarSesion)
        btnLogout.setOnClickListener {
            userRepository.cerrarSesion()

            // Volver a LoginActivity y limpiar el back stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNombre: TextView = view.findViewById(R.id.txtNombreUsuario)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            userRepository.getUser(userId) { user ->
                if (user != null) {
                    txtNombre.text  = "Hola, ${user.nombre}"
                } else {
                    txtNombre.text = "Usuario no encontrado"
                }
            }
        } else {
            txtNombre.text = "Usuario no autenticado"
        }
    }
}
