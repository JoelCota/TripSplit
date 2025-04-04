package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.repository.GrupoRepository

class UnirseGrupoFragment : Fragment() {

    private lateinit var editCodigoGrupo: EditText
    private val grupoRepository = GrupoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_unirse_grupo, container, false)

        editCodigoGrupo = rootView.findViewById(R.id.editCodigoGrupo)

        val btnUnirseGrupo = rootView.findViewById<Button>(R.id.btnUnirseGrupo)
        btnUnirseGrupo.setOnClickListener {
            val codigoGrupo = editCodigoGrupo.text.toString().trim()

            if (codigoGrupo.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    // Llamada al repositorio para unirse al grupo
                    grupoRepository.unirseAGrupo(codigoGrupo, userId) { success, message ->
                        if (success) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, GruposFragment())
                                .addToBackStack(null)
                                .commit()
                        } else {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No se ha iniciado sesión", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Por favor ingresa un código de grupo", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }
}
