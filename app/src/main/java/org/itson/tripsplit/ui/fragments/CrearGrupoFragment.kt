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
import org.itson.tripsplit.databinding.FragmentCrearGrupoBinding

class CrearGrupoFragment : Fragment() {

    private val grupoRepository = GrupoRepository()
    private lateinit var editGroupName: EditText
    private lateinit var btnCrearGrupo: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_crear_grupo, container, false)

        editGroupName= rootView.findViewById(R.id.edtGroupName)
        btnCrearGrupo= rootView.findViewById(R.id.btnCrearGrupo)
        btnCrearGrupo.setOnClickListener {
            val nombreGrupo = editGroupName.text.toString()
            if (nombreGrupo.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                grupoRepository.crearGrupo(nombreGrupo, userId) { success ->
                    if (success) {
                        Toast.makeText(context, "Grupo creado", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, GruposFragment())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Toast.makeText(context, "Error al crear grupo", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Ingrese un nombre", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }
}
